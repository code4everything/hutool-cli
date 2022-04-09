package org.code4everything.hutool.qe

import com.ql.util.express.ExpressRunner
import org.code4everything.hutool.Hutool
import org.code4everything.hutool.unix.HuUnix4jCommandBuilder

object UnixCmdBinds {

    fun getCmdBuilder(runner: ExpressRunner): HuUnix4jCommandBuilder {
        val builder = HuUnix4jCommandBuilder().cd(Hutool.ARG.workDir)
        runner.addFunctionOfServiceMethod("cat", builder, "cat", arrayOf(Array::class.java), null)
        runner.addFunctionOfServiceMethod("find", builder, "find", arrayOf(Array::class.java), null)
        runner.addFunctionOfServiceMethod("grep", builder, "grep", arrayOf(Array::class.java), null)
        runner.addFunctionOfServiceMethod("head", builder, "head", arrayOf(Array::class.java), null)
        runner.addFunctionOfServiceMethod("ls", builder, "ls", arrayOf(Array::class.java), null)
        runner.addFunctionOfServiceMethod("sed", builder, "sed", arrayOf(Array::class.java), null)
        runner.addFunctionOfServiceMethod("sort", builder, "sort", arrayOf(Array::class.java), null)
        runner.addFunctionOfServiceMethod("tail", builder, "tail", arrayOf(Array::class.java), null)
        runner.addFunctionOfServiceMethod("uniq", builder, "uniq", arrayOf(Array::class.java), null)
        runner.addFunctionOfServiceMethod("wc", builder, "wc", arrayOf(Array::class.java), null)
        return builder
    }
}
