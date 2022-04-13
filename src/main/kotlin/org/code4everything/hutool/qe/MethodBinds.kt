package org.code4everything.hutool.qe

import cn.hutool.core.date.DateTime
import cn.hutool.core.util.StrUtil
import com.alibaba.fastjson.JSONObject
import com.ql.util.express.ExpressRunner
import com.ql.util.express.Operator
import java.io.File
import java.util.regex.Pattern
import java.util.stream.Collectors
import org.code4everything.hutool.converter.DateConverter
import org.code4everything.hutool.converter.FileConverter
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
        val clz = Helper::class.java
        runner.addFunctionOfClassMethod("cmd", clz, "cmd", arrayOf(String::class.java), null)
        runner.addFunctionOfClassMethod("nullto", clz, "nullTo", Array(2) { Any::class.java }, null)
        runner.addFunctionOfClassMethod("list", clz, "list", arrayOf(Array::class.java), null)
        runner.addFunctionOfClassMethod("run", clz, "run", arrayOf(Array<String>::class.java), null)
    }

    @Suppress("UNCHECKED_CAST")
    fun bind4List() {
        runner.addClassMethod("join", List::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): String {
                val vars = (list?.first() as List<*>?) ?: emptyList<Any>()
                val sep = list?.last()?.toString() ?: ""
                return vars.joinToString(sep)
            }
        })
        runner.addClassField("sort", List::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): List<*> {
                val holdList = (list?.first() as List<*>?) ?: emptyList<Any>()
                return holdList.stream().map { if (it is Comparable<*>) it as Comparable<Any> else null }.filter { it != null }.sorted().collect(Collectors.toList())
            }
        })
        runner.addClassField("min", List::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): Any? {
                return reduceList(list) { t1, t2 -> if (t2 < t1) t2 else t1 }
            }
        })
        runner.addClassField("max", List::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): Any? {
                return reduceList(list) { t1, t2 -> if (t2 > t1) t2 else t1 }
            }
        })
        runner.addClassField("sum", List::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): Double {
                val holdList = (list?.first() as List<*>?) ?: emptyList<Any>()
                return sumList(holdList)
            }
        })
        runner.addClassField("avg", List::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): Double {
                val holdList = (list?.first() as List<*>?) ?: emptyList<Any>()
                return sumList(holdList) / holdList.size
            }
        })
    }

    fun bind4Object() {
        runner.addClassField("str", Any::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): String {
                return list?.first()?.toString() ?: ""
            }
        })
    }

    fun bind4String() {
        runner.addClassField("lower", CharSequence::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): String {
                return list?.first()?.toString()?.lowercase() ?: ""
            }
        })
        runner.addClassField("upper", CharSequence::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): String {
                return list?.first()?.toString()?.uppercase() ?: ""
            }
        })
        runner.addClassField("trim", CharSequence::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): String {
                return list?.first()?.toString()?.trim() ?: ""
            }
        })
        runner.addClassField("int", CharSequence::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): Int {
                return list?.first()?.toString()?.toInt() ?: 0
            }
        })
        runner.addClassField("long", CharSequence::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): Long {
                return list?.first()?.toString()?.toLong() ?: 0L
            }
        })
        runner.addClassField("double", CharSequence::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): Double {
                return list?.first()?.toString()?.toDouble() ?: 0.0
            }
        })
        runner.addClassField("file", CharSequence::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): File {
                val value = list?.first()?.toString() ?: ""
                return FileConverter().string2Object(value)
            }
        })
        runner.addClassField("date", CharSequence::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): DateTime {
                val value = list?.first()?.toString() ?: ""
                return DateConverter().string2Object(value)
            }
        })
        runner.addClassField("json", CharSequence::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): Any {
                val value = list?.first()?.toString() ?: ""
                return JsonObjectConverter(JSONObject::class.java).string2Object(value) ?: JSONObject()
            }
        })
        runner.addClassField("pattern", CharSequence::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): Pattern {
                val value = list?.first()?.toString() ?: ""
                return Pattern.compile(value)
            }
        })
        runner.addClassMethod("strip", CharSequence::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): String {
                val value = (list?.first() as String?) ?: ""
                val fix = list?.last()?.toString() ?: ""
                return StrUtil.strip(value, fix)
            }
        })
        runner.addClassMethod("split", CharSequence::class.java, object : Operator() {
            override fun executeInner(list: Array<*>?): List<String> {
                val value = (list?.first() as String?) ?: ""
                val regex = list?.last()?.toString() ?: ""
                return value.split(regex)
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
