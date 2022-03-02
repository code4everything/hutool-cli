package org.code4everything.hutool

import com.alibaba.fastjson.util.TypeUtils
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

// 输入输出转换器
@MustBeDocumented
@Inherited
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
annotation class IOConverter(
    // 使用自定义转换器
    val value: KClass<out Converter<*>> = WithoutConverter::class,

    // 当className不为空，使用className转换器
    val className: String = "",
) {

    class WithoutConverter(private val objType: Class<*>) : Converter<Any> {

        override fun string2Object(string: String): Any = TypeUtils.cast(string, objType, null)

        override fun object2String(any: Any?): String = any.toString()
    }
}
