package cn.hjw.dev.mall.types.annotations;

import java.lang.annotation.*;

/**
 * 动态配置中心-服务标识注解
 */
@Target(ElementType.FIELD) // 注解作用于字段
@Retention(RetentionPolicy.RUNTIME) // 注解在运行时可用
@Documented
public @interface DCCValue {

    String value() default "";
}
