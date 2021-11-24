package org.code4everything.hutool

import cn.hutool.core.date.DateField
import cn.hutool.core.date.DateTime
import cn.hutool.core.date.DateUtil
import cn.hutool.core.text.StrJoiner
import cn.hutool.core.util.StrUtil
import com.github.tomaslanger.chalk.Ansi
import org.code4everything.hutool.converter.ArrayConverter

class HuCalendar(yearMonth: String?) {

    private val currMonth: Int
    private val currYear: Int
    private val currDay: Int

    private var month = 0
    private var beginDate: DateTime

    val calenderStr: String
        get() = if (month < 0) {
            val joiner = StrJoiner.of("\n\n")
            for (i in 0..11) {
                joiner.append(getCalendarStr(DateUtil.offsetMonth(beginDate, i)))
            }
            joiner.toString()
        } else {
            getCalendarStr(beginDate)
        }

    private fun getCalendarStr(begin: DateTime): String {
        var week = begin.dayOfWeek()
        week = (if (week == 1) 8 else week) - 2
        var line: Array<String>? = null

        val result: MutableList<String> = ArrayList()
        result.add("${" ".repeat(6)}${DateUtil.format(begin, "yyyy-MM")}${" ".repeat(7)}")
        result.add("Mo Tu We Th Fr Sa Su")
        val end = DateUtil.endOfMonth(begin).dayOfMonth()
        val isCurrentMonth = begin.getField(DateField.YEAR) == currYear && begin.getField(DateField.MONTH) == currMonth

        for (start in 1..end) {
            if (line == null) {
                line = Array(7) { " ".repeat(2) }
            }
            val isCurrentDay = isCurrentMonth && start == currDay
            var dayStr = ""
            if (isCurrentDay) {
                dayStr += Ansi.Color.YELLOW.start
            }

            dayStr += start.toString().padStart(2, ' ')
            if (isCurrentDay) {
                dayStr += Ansi.Color.YELLOW.end
            }

            line[week] = dayStr
            if (week >= 6) {
                result.add(StrJoiner.of(" ").append(line).toString())
                line = null
                week = 0
            } else {
                week++
            }
        }

        if (line == null) {
            result.add(StrJoiner.of(" ").append(line).toString())
        }
        return StrJoiner.of("\n").append<Any>(result).toString()
    }

    companion object {

        @JvmStatic
        fun calendar(@IOConverter(ArrayConverter::class) yearMonths: Array<String>?): String {
            if (Utils.isArrayEmpty(yearMonths)) {
                return HuCalendar(null).calenderStr
            }

            val joiner = StrJoiner.of("\n\n")
            var previousYear = DateUtil.format(DateUtil.date(), "yyyy")
            yearMonths!!.forEach {
                val len = StrUtil.length(it)
                if (len == 0) {
                    return@forEach
                }

                val yearMonth = if (len < 3) previousYear + it.padStart(2, '0') else it
                previousYear = yearMonth.substring(0, 4)
                joiner.append(HuCalendar(yearMonth).calenderStr)

            }

            return joiner.toString()
        }
    }

    init {
        val now = DateUtil.date()
        beginDate = DateUtil.beginOfMonth(now)
        currYear = beginDate.getField(DateField.YEAR)
        currMonth = beginDate.getField(DateField.MONTH)
        currDay = now.getField(DateField.DAY_OF_MONTH)

        beginDate = when (StrUtil.length(yearMonth)) {
            4 -> DateUtil.parse(yearMonth, "yyyy").also { month = -1 }
            6 -> DateUtil.parse(yearMonth, "yyyyMM")
            7 -> DateUtil.parse(yearMonth, "yyyy-MM")
            else -> beginDate
        }

        if (month >= 0) {
            month = beginDate.getField(DateField.MONTH)
        }
    }
}
