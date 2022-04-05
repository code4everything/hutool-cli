package org.code4everything.hutool

import java.lang.annotation.Inherited

@MustBeDocumented
@Inherited
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.VALUE_PARAMETER,
)
annotation class Param(

    /**
     * alias for value
     */
    val name: String = "param",

    /**
     * alias for name
     */
    val value: String = "",

    val remark: String = "",
)
