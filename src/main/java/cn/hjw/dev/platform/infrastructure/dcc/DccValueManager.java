package cn.hjw.dev.platform.infrastructure.dcc;

import cn.hjw.dev.platform.types.annotations.DCCValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Slf4j
public class DccValueManager implements BeanPostProcessor {

    /**
     * 配置前缀
     */
    private static final String BASE_CONFIG_PATH = "group_buy_market_dcc_";

    /**
     * Redisson 客户端实例，用于与 Redis 进行交互，使用redis的发布订阅功能实现动态配置更新
     */
    @Resource
    private RedissonClient redissonClient;

    /**
     * 在容器启动时，收集所有的带有 @DCCValue 注解的 Bean 实例，保存到 dccObjGroup 中，供监听器使用
     */
    private final Map<String, Object> dccObjGroup = new ConcurrentHashMap<>();

    /**
     * 配置元信息存储，用于描述每个动态配置项的元数据，如描述信息、当前值等
     */
    private final Map<String, DccMetaInfo> dccMetaInfoMap = new ConcurrentHashMap<>();

    /**
     * 接收事件更新配置
     */
    public void refresh(String attribute, String value) {

        String key = BASE_CONFIG_PATH.concat(attribute);
        Object objBean = dccObjGroup.get(key);

        if (null == objBean) {
            log.warn("DCC 未找到目标 Bean，Key:{}", key);
            return;
        }
        try {
            Class<?> objBeanClass = objBean.getClass();
            if (AopUtils.isAopProxy(objBeanClass)) {
                objBeanClass = AopUtils.getTargetClass(objBeanClass);
            }

            Field field = objBeanClass.getDeclaredField(attribute);
            field.setAccessible(true);
            field.set(objBean, value);
            field.setAccessible(false);

            // 更新元数据中的当前值
            if (dccMetaInfoMap.containsKey(attribute)) {
                dccMetaInfoMap.get(attribute).setCurrentValue(value);
            }
            log.info("DCC 动态属性更新成功: {} -> {}", attribute, value);
        } catch (Exception e) {
            log.error("DCC 动态属性更新失败", e);
        }
    }


    /**
     * 获取所有配置项
     */
    public Map<String, DccMetaInfo> getAllConfigs() {
        return dccMetaInfoMap;
    }


    /**
     * Bean 初始化后置处理器，会处理所有的 Bean 实例，检查每个字段是否有 @DCCValue 注解
     * @param bean spring IOC 容器中的实例化对象
     * @param beanName 实例化对象的名称
     * @return
     * @throws BeansException
     */
    @SneakyThrows
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        /**
         * 注意；增加 AOP 代理后，获得类的方式要通过 AopProxyUtils.getTargetClass(bean);
         * 不能直接 bean.class 因为代理后类的结构发生变化，这样不能获得到自己的自定义注解了。
         */
        Class<?> targetBeanClass = bean.getClass();
        Object targetBeanObject = bean;
        if (AopUtils.isAopProxy(bean)) {
            // 是代理对象，获取目标类和目标对象
            targetBeanClass = AopUtils.getTargetClass(bean); // 获取目标类
            // 兼容原型Bean：若getSingletonTarget返回null，直接用代理对象
            targetBeanObject = AopProxyUtils.getSingletonTarget(bean) != null // getSingletonTarget只对单例Bean有效，非单例Bean会返回null
                    ? AopProxyUtils.getSingletonTarget(bean)
                    : bean;
        }

        // 获取类的所有字段
        Field[] fields = targetBeanClass.getDeclaredFields();

        for (Field field : fields) {
            // 遍历字段，判断是否有 @DCCValue 注解
            if (!field.isAnnotationPresent(DCCValue.class)) {
                // 如果没有注解则跳过
                continue;
            }
            field.setAccessible(true);
            DCCValue dccValue = field.getAnnotation(DCCValue.class); // 获取注解
            field.setAccessible(false);
            String value = dccValue.value(); // 获取注解的值
            if (StringUtils.isBlank(value))
                continue; // 如果注解值为空则跳过,表示没有启用

            // 按照':'分割，前半部分为配置Key，后半部分为默认值
            String[] splits = value.split(":");
            String attribute = splits[0];
            String defaultValue = splits.length == 2 ? splits[1] : "";
            String configKey = BASE_CONFIG_PATH.concat(attribute); // 配置Key = 前缀 + 注解中配置的Key

            // 设置值
            String finalValue = defaultValue;
            try {
                // 判断redis中是否有配置Key，不存在则创建，存在则获取最新值
                RBucket<String> bucket = redissonClient.getBucket(configKey);
                if (!bucket.isExists()) {
                    // 不存在则创建
                    bucket.set(defaultValue);
                    log.info("DCC 初始化 Redis 配置: {} = {}", configKey, defaultValue);
                } else {
                    // 存在则获取最新值
                    finalValue = bucket.get();
                    log.info("DCC 加载 Redis 配置: {} = {}", configKey, finalValue);
                }
            } catch (Exception e) {
                log.error("DCC 连接 Redis 失败，使用默认值", e);
            }
            // 2. 初始赋值
            try {
                field.setAccessible(true);
                field.set(targetBeanObject, finalValue);
                field.setAccessible(false);
            } catch (Exception e) {
                log.error("DCC Bean 初始赋值失败", e);
            }
            // 3. 注册管理,用于后续动态更新
            dccObjGroup.put(configKey, targetBeanObject);

            // 4. 注册元数据
            dccMetaInfoMap.put(attribute, new DccMetaInfo(attribute, finalValue, dccValue.description()));
        }
        return bean;
    }

    @Data
    @AllArgsConstructor
    public static class DccMetaInfo {
        private String key; // 配置键
        private String currentValue; // 当前值
        private String description; // 配置描述
    }
}
