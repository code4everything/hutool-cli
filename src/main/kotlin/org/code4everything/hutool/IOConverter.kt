package org.code4everything.hutool;

import com.alibaba.fastjson.util.TypeUtils;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;

/**
 * 输入输出转换器
 *
 * @author pantao
 * @since 2021/6/28
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD})
public @interface IOConverter {

    /**
     * 使用自定义转换器
     */
    Class<? extends Converter<?>> value() default WithoutConverter.class;

    /**
     * 当value是WithoutConverter类时，并且className不为空，那么解析className转换器
     */
    String className() default "";

    class WithoutConverter implements Converter<Object> {

        private final Class<?> objType;

        public WithoutConverter(Class<?> objType) {
            this.objType = objType;
        }

        @Override
        public Object string2Object(String string) {
            return TypeUtils.cast(string, objType, null);
        }

        @Override
        public String object2String(Object object) {
            return Objects.toString(object);
        }
    }
}
