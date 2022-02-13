package org.code4everything.hutool

/// 公积金贷款额度计算
object AccumulationFund {

    /// 成都公积金贷款额度计算，表达式：1000*12,2000*12
    /// 公式说明：连续缴纳公积金，假设当前为2022年01月，2020年每月缴纳1000元，2021年每月缴纳了2000元，表达式为：1000*12,2000*12
    @JvmStatic
    fun calcChengdu(expression: String): Int {
        val amounts = ArrayList<Int>()
        expression.split(",").forEach {
            val tokens = it.split("*")
            val amount = tokens[0].toInt()
            val months = if (tokens.size > 1) tokens[1].toInt() else 1
            amounts.addAll(List(months) { amount })
        }
        var sum = 0
        amounts.forEachIndexed { idx, e ->
            sum += (amounts.size - idx) * e
        }
        return (sum * 0.9).toInt()
    }
}
