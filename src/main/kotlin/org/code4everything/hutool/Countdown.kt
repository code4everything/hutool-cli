package org.code4everything.hutool

import cn.hutool.core.date.DateTime
import cn.hutool.core.util.NumberUtil
import org.code4everything.hutool.converter.DateConverter

object Countdown {

    @JvmStatic
    @HelpInfo(helps = [
        "calculate time count down", "",
        "example: '1632' 'min'", "",
        "param1: the time expression, like '2022-03-20 16:25', or timestamp like '1647764793'",
        "param2: the timestamp unit, if param1 is a timestamp, the unit is required"
    ])
    fun datetime(@IOConverter deadline: String, unit: String): String {
        val dateConverter = DateConverter()
        val count: Long = if (NumberUtil.isNumber(deadline)) {
            val dateTime = DateTime(0)
            dateConverter.getOffsetDate(dateTime, deadline + unit).time
        } else {
            val dateTime = dateConverter.string2Object(deadline)
            dateTime.time - System.currentTimeMillis()
        }

        if (count <= 0) {
            return "倒计时已过期"
        }

        val ms = count % 1000
        val sec = count / 1000 % 60
        val min = count / 1000 / 60 % 60
        val hour = count / 1000 / 60 / 60 % 24
        val day = count / 1000 / 60 / 60 / 24

        var res = ""
        var hasStart = day > 0
        val hasEnd = ms > 0

        if (hasStart) {
            res += day.toString() + "天"
        }
        if (hasStart && hasEnd || hour > 0) {
            hasStart = true
            res += hour.toString() + "小时"
        }
        if (hasStart && hasEnd || min > 0) {
            hasStart = true
            res += min.toString() + "分"
        }
        if (hasStart && hasEnd || sec > 0) {
            res += sec.toString() + "秒"
        }
        if (hasEnd) {
            res += ms.toString() + "毫秒"
        }

        return res
    }
}
