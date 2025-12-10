package cn.hjw.dev.mall.application.config;

import cn.hjw.dev.mall.types.annotations.DCCValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 基于 Redis 实现动态配置中心
 * @create 2025-01-03 15:35
 */
@Slf4j
@Configuration // 声明为配置类
public class DCCValueBeanFactory implements BeanPostProcessor {

    private static final String BASE_CONFIG_PATH = "group_buy_market_dcc_";

    private final RedissonClient redissonClient;

    private final Map<String, Object> dccObjGroup = new HashMap<>();

    public DCCValueBeanFactory(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Bean("dccTopic")
    public RTopic dccRedisTopicListener(RedissonClient redissonClient) {
        RTopic topic = redissonClient.getTopic("group_buy_market_dcc"); // 在redis中创建主题名为 group_buy_market_dcc 的频道
        topic.addListener(String.class, (charSequence, s) -> {

            String[] split = s.split("="); // 按照=分割，前半部分为配置Key，后半部分为配置值

            // 获取值
            String attribute = split[0];
            String key = BASE_CONFIG_PATH.concat(attribute);
            String value = split[1];

            // 设置值
            RBucket<String> bucket = redissonClient.getBucket(key);
            boolean exists = bucket.isExists();
            if (!exists) return;
            bucket.set(value); // 更新 Redis 中的值

            Object objBean = dccObjGroup.get(key);
            if (null == objBean) return;

            Class<?> objBeanClass = objBean.getClass();
            // 检查 objBeanClass 是否是 AOP 代理对象
            if (AopUtils.isAopProxy(objBeanClass)) {
                // 获取代理对象的目标对象
                objBeanClass = AopUtils.getTargetClass(objBeanClass);
            }
            try {
                // 1. getDeclaredField 方法用于获取指定类中声明的所有字段，包括私有字段、受保护字段和公共字段。
                // 2. getField 方法用于获取指定类中的公共字段，即只能获取到公共访问修饰符（public）的字段。
                Field field = objBeanClass.getDeclaredField(attribute);
                field.setAccessible(true);
                field.set(objBean, value);
                field.setAccessible(false);

                log.info("DCC 节点监听，动态设置值 {} {}", key, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return topic;
    }

    /**
     * Bean 初始化后置处理器
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 注意；增加 AOP 代理后，获得类的方式要通过 AopProxyUtils.getTargetClass(bean); 不能直接 bean.class 因为代理后类的结构发生变化，这样不能获得到自己的自定义注解了。
        Class<?> targetBeanClass = bean.getClass(); // 获取IOC容器中实例化对象的类
        Object targetBeanObject = bean; // 获取IOC容器中的对应类的实例化对象
        if (AopUtils.isAopProxy(bean)) { // 判断是否是代理对象
            targetBeanClass = AopUtils.getTargetClass(bean);
            targetBeanObject = AopProxyUtils.getSingletonTarget(bean);
        }

        Field[] fields = targetBeanClass.getDeclaredFields(); // 获取类的所有字段
        for (Field field : fields) { // 遍历字段，判断是否有 @DCCValue 注解
            if (!field.isAnnotationPresent(DCCValue.class)) {
                continue;
            } // 如果没有注解则跳过

            DCCValue dccValue = field.getAnnotation(DCCValue.class); // 获取注解

            String value = dccValue.value(); // 获取注解的值
            if (StringUtils.isBlank(value)) { // 如果值为空则抛出异常
                throw new RuntimeException(field.getName() + " @DCCValue is not config value config case 「isSwitch/isSwitch:1」");
            }

            String[] splits = value.split("="); // 按照=分割，前半部分为配置Key，后半部分为默认值
            String configKey = BASE_CONFIG_PATH.concat(splits[0]); // 配置Key = 前缀 + 注解中配置的Key：group_buy_market_dcc_key
            String configDefaultValue = splits.length == 2 ? splits[1] : null;

            // 设置值
            String setValue = configDefaultValue;

            try {
                // 如果为空则抛出异常
                if (StringUtils.isBlank(configDefaultValue)) {
                    throw new RuntimeException("DCC Key:" + configKey + " defaultValue is blank, please check config!");
                }

                // Redis 操作，判断配置Key是否存在，不存在则创建，存在则获取最新值
                RBucket<String> bucket = redissonClient.getBucket(configKey);
                boolean exists = bucket.isExists();
                if (!exists) {
                    bucket.set(configDefaultValue); // 不存在则创建
                    log.info("DCC Key:{} not exists, set default value:{}", configKey);
                } else {
                    setValue = bucket.get(); // 存在则获取最新值
                    log.info("DCC Key:{} exists, get value:{}", configKey, setValue);
                }

                field.setAccessible(true); // 设置私有属性可访问
                field.set(targetBeanObject, setValue); // 设置目标对象的字段值
                field.setAccessible(false); // 恢复私有属性访问权限
            } catch (Exception e) {
                throw new RuntimeException("DCC Key:" + configKey + " set value error!", e);
            }

            dccObjGroup.put(configKey, targetBeanObject); // 保存对象实例，供监听器使用
        }

        return bean;
    }

}
