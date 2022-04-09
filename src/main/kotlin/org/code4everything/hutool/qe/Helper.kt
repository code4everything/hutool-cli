package org.code4everything.hutool.qe

import cn.hutool.core.util.RuntimeUtil
import java.util.concurrent.FutureTask
import org.code4everything.hutool.Hutool
import org.code4everything.hutool.MethodArg
import org.code4everything.hutool.QLE
import org.code4everything.hutool.Utils

object Helper {

    @JvmStatic
    fun run(array: Array<String>): Any {
        val workDir = Hutool.ARG.workDir
        val future: FutureTask<Any?> = FutureTask {
            Hutool.ARG = MethodArg()
            Hutool.ARG.workDir = workDir
            val result = Hutool.resolveCmd(array)
            QLE.RunResult(result, Hutool.result)
        }
        return Utils.syncRun(future, ClassLoader.getSystemClassLoader())!!
    }

    @JvmStatic
    fun cmd(cmd: String?): String {
        val result = RuntimeUtil.execForStr(cmd)
        return if (result?.isNotEmpty() == true) result.trim() else ""
    }

    @JvmStatic
    fun nullTo(v1: Any?, v2: Any): Any = v1 ?: v2

    @JvmStatic
    fun list(array: Array<*>): List<Any?> = array.toList()
}
