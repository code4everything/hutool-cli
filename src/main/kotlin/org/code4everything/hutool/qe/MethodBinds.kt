package org.code4everything.hutool.qe

import cn.hutool.core.swing.clipboard.ClipboardUtil
import com.ql.util.express.ExpressRunner
import com.ql.util.express.Operator
import org.code4everything.hutool.QLE

class MethodBinds(private val runner: ExpressRunner) {

    fun importPackage() {
        val expressPackage = runner.rootExpressPackage
        expressPackage.addPackage("org.code4everything.hutool")
        expressPackage.addPackage("org.code4everything.hutool.converter")
        expressPackage.addPackage("com.alibaba.fastjson")
        expressPackage.addPackage("cn.hutool.core.util")
        expressPackage.addPackage("cn.hutool.core.collection")
        expressPackage.addPackage("cn.hutool.core.date")
        expressPackage.addPackage("cn.hutool.core.io")
        expressPackage.addPackage("cn.hutool.core.lang")
    }

    fun bindStaticMethods() {
        val clz = QLE::class.java
        runner.addFunctionOfClassMethod("cmd", clz, "cmd", arrayOf(String::class.java), null)
        runner.addFunctionOfClassMethod("nullto", clz, "nullTo", Array(2) { Any::class.java }, null)
        runner.addFunctionOfClassMethod("list", clz, "list", arrayOf(Array::class.java), null)
        runner.addFunctionOfClassMethod("run", clz, "run", arrayOf(Array<String>::class.java), null)

        runner.addFunctionOfClassMethod("clipboard", ClipboardUtil::class.java, "getStr", emptyArray(), null)
    }

    fun bindListMethods() {
        runner.addClassMethod("join", List::class.java, object : Operator() {
            override fun executeInner(list: Array<out Any>?): Any {
                val vars = (list?.first() as List<*>?) ?: emptyList<Any>()
                val sep = (list?.last() as CharSequence?) ?: ""
                return vars.joinToString(sep)
            }
        })
    }

    fun bindStringMethods() {
        runner.addClassMethod("lower", CharSequence::class.java, object : Operator() {
            override fun executeInner(list: Array<out Any>?): Any {
                return (list?.first() as CharSequence?)?.toString()?.lowercase() ?: ""
            }
        })
        runner.addClassMethod("upper", CharSequence::class.java, object : Operator() {
            override fun executeInner(list: Array<out Any>?): Any {
                return (list?.first() as CharSequence?)?.toString()?.uppercase() ?: ""
            }
        })
    }
}
