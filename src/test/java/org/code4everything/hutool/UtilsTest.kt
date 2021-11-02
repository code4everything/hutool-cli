package org.code4everything.hutool

import cn.hutool.core.util.RandomUtil
import javassist.ClassPool
import org.junit.Test

/**
 * @author pantao
 * @since 2021/7/2
 */
class UtilsTest {

    @Test
    fun getMethodFullInfo() {
        ClassPool.getDefault().run { getCtClass(RandomUtil::class.java.name) }.run {
            var longClazz = classPool[Long::class.javaPrimitiveType!!.name]
            getDeclaredMethod("randomLong", Array(2) { longClazz })
        }.let { Utils.getMethodFullInfo(it, null) }.also(::println)
    }
}
