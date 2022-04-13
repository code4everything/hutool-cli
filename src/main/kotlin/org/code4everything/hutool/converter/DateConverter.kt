package org.code4everything.hutool.converter

import cn.hutool.core.date.DateField
import cn.hutool.core.date.DatePattern
import cn.hutool.core.date.DateTime
import cn.hutool.core.date.DateUtil
import cn.hutool.core.math.Calculator
import cn.hutool.core.util.ReUtil
import cn.hutool.core.util.ReflectUtil
import java.util.Date
import org.code4everything.hutool.Converter
import org.code4everything.hutool.Hutool

class DateConverter : Converter<DateTime> {

    override fun string2Object(string: String): DateTime {
        var idx = string.indexOf("+")
        if (idx > 0) {
            val dateTime = parseDate(string.substring(0, idx))
            val offsetStr = string.substring(idx + 1)
            return getOffsetDate(dateTime, offsetStr)
        }

        idx = string.indexOf(">")
        if (idx > 0) {
            val dateTime = parseDate(string.substring(0, idx))
            val methodName = endMethodMap[string.substring(idx + 1)] ?: "date"
            return ReflectUtil.getPublicMethod(DateUtil::class.java, methodName, Date::class.java).invoke(null, dateTime) as DateTime
        }

        idx = string.indexOf("<")
        if (idx > 0) {
            val dateTime = parseDate(string.substring(0, idx))
            val methodName = beginMethodMap[string.substring(idx + 1)] ?: "date"
            return ReflectUtil.getPublicMethod(DateUtil::class.java, methodName, Date::class.java).invoke(null, dateTime) as DateTime
        }

        return parseDate(string)
    }

    fun getOffsetDate(dateTime: DateTime, offsetStr: String): DateTime {
        for ((k, v) in offsetMap) {
            if (offsetStr.endsWith(k)) {
                val offset = Calculator.conversion(offsetStr.removeSuffix(k)).toInt()
                return dateTime.offset(v, offset)
            }
        }
        return dateTime.offset(DateField.MILLISECOND, Calculator.conversion(offsetStr).toInt())
    }

    private fun parseDate(string: String): DateTime {
        if (string.length > 2) {
            val timestamp = string.removeSuffix("ms")
            if (ReUtil.isMatch("[0-9]+", timestamp)) {
                return DateUtil.date(timestamp.toLong())
            }
        }

        return (when (string) {
            "now" -> DateUtil.date()
            "today" -> DateUtil.beginOfDay(DateUtil.date())
            else -> null
        } ?: run {
            for ((k, v) in offsetMap) {
                if (string.endsWith(k)) {
                    return DateTime(0).offset(v, string.removeSuffix(k).toInt())
                }
            }

            when (string.length) {
                2 -> DateUtil.format(DateUtil.date(), "yyyy-MM-") + string

                5 -> {
                    // check separator
                    when (string[2]) {
                        '-' -> DateUtil.format(DateUtil.date(), "yyyy-") + string
                        ':' -> DateUtil.format(DateUtil.date(), "yyyy-MM-dd ") + string
                        else -> string
                    }
                }

                else -> string
            }.let { DateUtil.parse(it.replace('T', ' '), *patterns) }
        })
    }

    override fun object2String(any: Any?): String {
        return if (any is Date) {
            Hutool.simpleDateFormat.format(any) + "  ms:" + any.time
        } else ""
    }

    companion object {

        @JvmStatic
        private val patterns: Array<String> by lazy {
            ReflectUtil.getFields(DatePattern::class.java).filter { it.name.endsWith("_PATTERN") }.map { ReflectUtil.getStaticFieldValue(it).toString() }.toTypedArray()
        }

        @JvmStatic
        private val offsetMap: MutableMap<String, DateField> by lazy {
            HashMap<String, DateField>(16).apply {
                put("ms", DateField.MILLISECOND)
                put("s", DateField.SECOND)
                put("sec", DateField.SECOND)
                put("min", DateField.MINUTE)
                put("h", DateField.HOUR)
                put("hour", DateField.HOUR)
                put("d", DateField.DAY_OF_YEAR)
                put("day", DateField.DAY_OF_YEAR)
                put("w", DateField.WEEK_OF_YEAR)
                put("week", DateField.WEEK_OF_YEAR)
                put("m", DateField.MONTH)
                put("mon", DateField.MONTH)
                put("month", DateField.MONTH)
                put("y", DateField.YEAR)
                put("year", DateField.YEAR)
            }
        }

        @JvmStatic
        private val endMethodMap: MutableMap<String, String> by lazy {
            HashMap<String, String>(16).apply {
                put("s", "endOfSecond")
                put("sec", "endOfSecond")
                put("min", "endOfMinute")
                put("h", "endOfHour")
                put("hour", "endOfHour")
                put("d", "endOfDay")
                put("day", "endOfDay")
                put("w", "endOfWeek")
                put("week", "endOfWeek")
                put("m", "endOfMonth")
                put("mon", "endOfMonth")
                put("month", "endOfMonth")
                put("y", "endOfYear")
                put("year", "endOfYear")
            }
        }

        @JvmStatic
        private val beginMethodMap: MutableMap<String, String> by lazy {
            HashMap<String, String>(16).apply {
                put("s", "beginOfSecond")
                put("sec", "beginOfSecond")
                put("min", "beginOfMinute")
                put("h", "beginOfHour")
                put("hour", "beginOfHour")
                put("d", "beginOfDay")
                put("day", "beginOfDay")
                put("w", "beginOfWeek")
                put("week", "beginOfWeek")
                put("m", "beginOfMonth")
                put("mon", "beginOfMonth")
                put("month", "beginOfMonth")
                put("y", "beginOfYear")
                put("year", "beginOfYear")
            }
        }
    }
}
