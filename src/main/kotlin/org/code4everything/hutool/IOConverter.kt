package org.code4everything.hutool

import com.alibaba.fastjson.util.TypeUtils
import java.lang.annotation.Documented
import java.lang.annotation.Inherited
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.Objects
import kotlin.reflect.KClass

// 输入输出转换器
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
annotation class IOConverter(
    // 使用自定义转换器
    val value: KClass<out Converter<*>> = WithoutConverter::class,

    // 当value是WithoutConverter类时，并且className不为空，那么解析className转换器
    val className: String = "",
) {

    class WithoutConverter(private val objType: Class<*>) : Converter<Any> {

        override fun string2Object(string: String): Any = TypeUtils.cast(string, objType, null)

        override fun object2String(any: Any?): String = Objects.toString(any)
    }
}
