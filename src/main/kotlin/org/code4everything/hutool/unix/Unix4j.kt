package org.code4everything.hutool.unix

import org.code4everything.hutool.HelpInfo
import org.code4everything.hutool.Hutool

object Unix4j {

    @JvmStatic
    @HelpInfo(helps = [
        "use unix command on windows, macos, linux",
        "support: cd, from, wc, uniq, sort, sed, ls, tail, head, grep, cat, find"
    ])
    fun unix(): HuUnix4jCommandBuilder {
        val builder = HuUnix4jCommandBuilder().cd(Hutool.ARG.workDir)
        val cmds = arrayOf("cd", "from", "wc", "uniq", "sort", "sed", "ls", "tail", "head", "grep", "cat", "find")
        val hasNumberCmds = arrayOf("find", "head", "tail")
        var previousCmd = "from"
        val args = ArrayList<Any>()

        Hutool.ARG.params.forEach {
            if (cmds.contains(it)) {
                pip(builder, previousCmd, args)
                args.clear()
                previousCmd = it
            } else {
                if (hasNumberCmds.contains(previousCmd)) {
                    try {
                        args.add(it.toLong())
                    } catch (e: Exception) {
                        args.add(if (it.length == 1) it[0] else it)
                    }
                } else {
                    args.add(if (it.length == 1) it[0] else it)
                }
            }
        }

        return pip(builder, previousCmd, args)
    }

    @JvmStatic
    private fun pip(builder: HuUnix4jCommandBuilder, cmd: String, args: ArrayList<Any>): HuUnix4jCommandBuilder {
        when (cmd) {
            "cd" -> builder.cd(args[0].toString())
            "from" -> if (args.isNotEmpty()) builder.fromFile(args[0].toString())
            "wc" -> builder.wc(args.toTypedArray())
            "uniq" -> builder.uniq(args.toTypedArray())
            "sort" -> builder.sort(args.toTypedArray())
            "sed" -> builder.sed(args.toTypedArray())
            "grep" -> builder.grep(args.toTypedArray())
            "ls" -> builder.ls(args.toTypedArray())
            "tail" -> builder.tail(args.toTypedArray())
            "head" -> builder.head(args.toTypedArray())
            "cat" -> builder.cat(args.toTypedArray())
            "find" -> builder.find(args.toTypedArray())
        }
        return builder
    }
}
