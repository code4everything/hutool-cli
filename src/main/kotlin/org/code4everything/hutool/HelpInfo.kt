package org.code4everything.hutool

import java.lang.annotation.Inherited

@MustBeDocumented
@Inherited
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FUNCTION,
)
annotation class HelpInfo(

    val helps: Array<String>,

    val callbackMethodName: String = "",
)
