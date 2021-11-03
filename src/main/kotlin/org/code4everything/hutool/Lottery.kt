package org.code4everything.hutool

import cn.hutool.core.util.RandomUtil
import cn.hutool.core.util.StrUtil
import java.util.LinkedList
import java.util.StringJoiner

object Lottery {

    @JvmStatic
    fun lottery(): String {
        val baseList = LinkedList(List(33) { i -> i })
        val joiner = StringJoiner(" ")
        Array(6) { 0 }.map {
            val idx = RandomUtil.randomInt(0, baseList.size)
            baseList.removeAt(idx)
        }.stream().sorted(Comparator.naturalOrder()).forEach { joiner.add(StrUtil.padPre(it.toString(), 2, '0')) }
        var last = StrUtil.padPre((RandomUtil.randomInt(0, 16) + 1).toString(), 2, '0')
        return "$joiner | $last"
    }
}
