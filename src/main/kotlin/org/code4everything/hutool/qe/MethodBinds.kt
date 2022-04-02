package org.code4everything.hutool.qe

import cn.hutool.core.swing.clipboard.ClipboardUtil
import cn.hutool.core.util.StrUtil
import com.alibaba.fastjson.JSONObject
import com.ql.util.express.ExpressRunner
import com.ql.util.express.Operator
import java.util.stream.Collectors
import org.code4everything.hutool.QLE
import org.code4everything.hutool.converter.JsonObjectConverter

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
            override fun executeInner(list: Array<*>?): Any {
                val vars = (list?.first() as List<*>?) ?: emptyList<Any>()
                val sep = (list?.last() as CharSequence?) ?: ""
                return vars.joinToString(sep)
            }
        })
        runner.addClassMethod("min", List::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): Any? {
                return reduceList(list) { t1, t2 -> if (t2 < t1) t2 else t1 }
            }
        })
        runner.addClassMethod("max", List::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): Any? {
                return reduceList(list) { t1, t2 -> if (t2 > t1) t2 else t1 }
            }
        })
        runner.addClassMethod("sum", List::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): Any {
                val holdList = (list?.first() as List<*>?) ?: emptyList<Any>()
                return sumList(holdList)
            }
        })
        runner.addClassMethod("avg", List::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): Any {
                val holdList = (list?.first() as List<*>?) ?: emptyList<Any>()
                return sumList(holdList) / holdList.size
            }
        })
    }

    fun bindStringMethods() {
        runner.addClassMethod("lower", CharSequence::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): Any {
                return (list?.first() as CharSequence?)?.toString()?.lowercase() ?: ""
            }
        })
        runner.addClassMethod("upper", CharSequence::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): Any {
                return (list?.first() as CharSequence?)?.toString()?.uppercase() ?: ""
            }
        })
        runner.addClassMethod("int", CharSequence::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): Any {
                return (list?.first() as CharSequence?)?.toString()?.toInt() ?: 0
            }
        })
        runner.addClassMethod("long", CharSequence::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): Any {
                return (list?.first() as CharSequence?)?.toString()?.toLong() ?: 0
            }
        })
        runner.addClassMethod("double", CharSequence::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): Any {
                return (list?.first() as CharSequence?)?.toString()?.toDouble() ?: 0.0
            }
        })
        runner.addClassMethod("tojson", CharSequence::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): Any {
                val value = (list?.first() as CharSequence?)?.toString() ?: ""
                return JsonObjectConverter(JSONObject::class.java).string2Object(value) ?: JSONObject()
            }
        })
        runner.addClassMethod("strip", CharSequence::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): Any {
                val value = (list?.first() as String?) ?: ""
                val fix = (list?.last() as CharSequence?) ?: ""
                return StrUtil.strip(value, fix)
            }
        })
    }

    private fun sumList(list: List<*>): Double {
        return list.stream().mapToDouble {
            if (it == null) {
                return@mapToDouble 0.0
            }
            if (it is Number) {
                return@mapToDouble it.toDouble()
            }
            try {
                return@mapToDouble it.toString().toDouble()
            } catch (e: Exception) {
                return@mapToDouble 0.0
            }
        }.sum()
    }

    @Suppress("UNCHECKED_CAST")
    private fun reduceList(list: Array<*>?, compare: (Comparable<Any>, Comparable<Any>) -> Comparable<Any>): Any? {
        val holdList = (list?.first() as List<*>?) ?: emptyList<Any>()
        val compareList = holdList.stream().map { if (it is Comparable<*>) it as Comparable<Any> else null }.filter { it != null }.collect(Collectors.toList())
        if (compareList.isNotEmpty()) {
            var result = compareList.first()
            compareList.stream().skip(1).forEach {
                result = compare(result!!, it!!)
            }
            return result
        }
        return holdList.getOrNull(0)
    }
}
