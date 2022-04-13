package org.code4everything.hutool.unix

import org.code4everything.hutool.HelpInfo
import org.code4everything.hutool.Hutool

object Unix4j {

    @JvmStatic
    @HelpInfo(helps = [
        "use unix command on windows, macos, linux",
        "support: cd, from, wc, uniq, sort, sed, ls, tail, head, grep, cat, find", "",
        "wc options:", "l: count lines", "w: count words", "m: count chars", "",
        "uniq options:", "c: count times", "d: only duplicate", "u: only unique", "g: global line", "",
        "ls options:", "a: all files", "h: readable size", "l: long format", "R: recurse sub dir", "r: reverse order", "t: sort by recently modified", "",
        "tail options:", "c: count use char, not line", "q: ignore header when multi files", "s: count from start", "",
        "head options:", "c: count use char, not line", "q: ignore header when multi files", "",
        "cat options:", "n: print line number", "b: print line number ignore empty line", "s: squeeze empty lines", "",
        "grep options:", "i: ignore case", "v: invert match", "F: use fixed string, not regex",
        "n: print line number", "c: print matched line count", "l: print matched file", "x: match whole line", "",
        "sort options: ", "c: check order, no output", "m: merge only", "u: unique", "b: ignore leading blanks",
        "d: dictionary order", "f: ignore case", "n: numeric sort", "g: general numeric sort",
        "h: readable size numeric sort", "m: month sort", "v: version sort", "r: reverse result", "",
        "sed options: ", "n: quiet", "g: global", "p: print matched lines", "l: print line number",
        "I: ignore case", "s: substitute", "a: append to matched line", "i: insert to matched line",
        "c: change  matched line", "d: delete matched line", "y: translate", "",
        "find options: ", "d: only dir", "f: only file", "l: only symbolic link", "x: others file", "r: regex pattern",
        "i: ignore case", "n: time after", "o: time before", "c: specified create time", "a: specified access time",
        "m: specified modify time", "z: print full file name",
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
