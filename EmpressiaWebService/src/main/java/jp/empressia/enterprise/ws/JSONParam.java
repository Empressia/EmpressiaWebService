package jp.empressia.enterprise.ws;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * WebServiceでJSONの値をバインドするためのアノテーションです。
 * @author すふぃあ
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JSONParam {

    /** 目的の値までの名前の配列です。 */
    String[] value() default {};

}
