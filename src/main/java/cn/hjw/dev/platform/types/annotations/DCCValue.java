package cn.hjw.dev.platform.types.annotations;

import java.lang.annotation.*;

/**
 * 动态配置中心-服务标识注解
 */
@Target(ElementType.FIELD) // 表明该注解只能用于字段
@Retention(RetentionPolicy.RUNTIME) // 注解在运行时仍然可用，通过反射可以读取
@Documented // 表明该注解将包含在 Javadoc 中
public @interface DCCValue {

    /**
     * 配置键与默认值，格式：key:defaultValue
     * 例如：downgradeSwitch:0
     */
    String value() default "";

    /**
     * 配置描述（用于前端可视化展示）
     * 例如：降级开关 (0-关 1-开)
     */
    String description() default "";
}
