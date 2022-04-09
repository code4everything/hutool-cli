package org.code4everything.hutool.unix

import java.io.File
import java.util.Date
import org.code4everything.hutool.Hutool
import org.unix4j.builder.DefaultUnix4jCommandBuilder
import org.unix4j.option.Option
import org.unix4j.unix.cat.CatOption
import org.unix4j.unix.cat.CatOptions
import org.unix4j.unix.find.FindOption
import org.unix4j.unix.find.FindOptions
import org.unix4j.unix.grep.GrepOption
import org.unix4j.unix.grep.GrepOptions
import org.unix4j.unix.head.HeadOption
import org.unix4j.unix.head.HeadOptions
import org.unix4j.unix.ls.LsOption
import org.unix4j.unix.ls.LsOptions
import org.unix4j.unix.sed.SedOption
import org.unix4j.unix.sed.SedOptions
import org.unix4j.unix.sort.SortOption
import org.unix4j.unix.sort.SortOptions
import org.unix4j.unix.tail.TailOption
import org.unix4j.unix.tail.TailOptions
import org.unix4j.unix.uniq.UniqOption
import org.unix4j.unix.uniq.UniqOptions
import org.unix4j.unix.wc.WcOption
import org.unix4j.unix.wc.WcOptions

/**
 * not supported: cut, echo, xargs
 */
class HuUnix4jCommandBuilder : DefaultUnix4jCommandBuilder() {

    override fun toString(): String = super.toStringResult()

    override fun cd(path: String): HuUnix4jCommandBuilder {
        super.cd(path)
        return this
    }

    override fun from(lines: Iterable<String>): HuUnix4jCommandBuilder {
        super.from(lines)
        return this
    }

    fun from(string: String): HuUnix4jCommandBuilder {
        super.fromString(string)
        return this
    }

    fun from(file: File): HuUnix4jCommandBuilder {
        super.fromFile(file)
        return this
    }

    fun wc(args: Array<Any>): HuUnix4jCommandBuilder {
        val paramOption = ParamOption(args)
        val wcOptions = WcOptions.Default(*paramOption.getOptions(WcOption.values()))

        if (paramOption.params.isEmpty()) {
            super.wc(wcOptions)
        } else {
            super.wc(wcOptions, paramOption.paramStrArr)
        }

        return this
    }

    fun uniq(args: Array<Any>): HuUnix4jCommandBuilder {
        val paramOption = ParamOption(args)
        val uniqOptions = UniqOptions.Default(*paramOption.getOptions(UniqOption.values()))

        if (paramOption.params.isEmpty()) {
            super.uniq(uniqOptions)
        } else {
            super.uniq(uniqOptions, paramOption.params.first().toString())
        }

        return this
    }

    fun sort(args: Array<Any>): HuUnix4jCommandBuilder {
        val paramOption = ParamOption(args)
        val sortOptions = SortOptions.Default(*paramOption.getOptions(SortOption.values()))

        if (paramOption.params.isEmpty()) {
            super.sort(sortOptions)
        } else {
            super.sort(sortOptions, *paramOption.paramStrArr)
        }

        return this
    }

    fun sed(args: Array<Any>): HuUnix4jCommandBuilder {
        val paramOption = ParamOption(args)
        val sedOptions = SedOptions.Default(*paramOption.getOptions(SedOption.values()))

        if (paramOption.params.size < 2) {
            Hutool.debugOutput("sed missing argument, skipped")
        } else {
            val params = paramOption.paramStrArr
            super.sed(sedOptions, params[0], params[1])
        }

        return this
    }

    fun ls(args: Array<Any>): HuUnix4jCommandBuilder {
        val paramOption = ParamOption(args)
        val lsOptions = LsOptions.Default(*paramOption.getOptions(LsOption.values()))

        if (paramOption.params.isEmpty()) {
            super.ls(lsOptions)
        } else {
            super.ls(lsOptions, *paramOption.paramStrArr)
        }

        return this
    }

    fun tail(args: Array<Any>): HuUnix4jCommandBuilder {
        val paramOption = ParamOption(args)
        val headOptions = TailOptions.Default(*paramOption.getOptions(TailOption.values()))

        val count = getCount(paramOption.params)
        if (paramOption.params.isEmpty()) {
            super.tail(headOptions, count)
        } else {
            super.tail(headOptions, count, *paramOption.paramStrArr)
        }

        return this
    }

    fun head(args: Array<Any>): HuUnix4jCommandBuilder {
        val paramOption = ParamOption(args)
        val headOptions = HeadOptions.Default(*paramOption.getOptions(HeadOption.values()))

        val count = getCount(paramOption.params)
        if (paramOption.params.isEmpty()) {
            super.head(headOptions, count)
        } else {
            super.head(headOptions, count, *paramOption.paramStrArr)
        }

        return this
    }

    fun grep(args: Array<Any>): HuUnix4jCommandBuilder {
        val paramOption = ParamOption(args)
        val grepOptions = GrepOptions.Default(*paramOption.getOptions(GrepOption.values()))
        val regex = paramOption.params.removeFirst().toString()

        if (paramOption.params.isEmpty()) {
            super.grep(grepOptions, regex)
        } else {
            super.grep(grepOptions, regex, *paramOption.paramStrArr)
        }

        return this
    }

    fun cat(args: Array<Any>): HuUnix4jCommandBuilder {
        val paramOption = ParamOption(args)
        val catOptions = paramOption.getOptions(CatOption.values())

        if (paramOption.params.isEmpty()) {
            super.cat(CatOptions.Default(*catOptions))
        } else {
            super.cat(CatOptions.Default(*catOptions), *paramOption.paramStrArr)
        }

        return this
    }

    fun find(args: Array<Any>): HuUnix4jCommandBuilder {
        val paramOption = ParamOption(args)
        val findOptions = FindOptions.Default(*paramOption.getOptions(FindOption.values()))
        val params = paramOption.params

        lateinit var v1: Any
        if (params.size == 1) {
            v1 = params[0]
            when (v1) {
                is Number -> {
                    super.find(findOptions, v1.toLong())
                }
                is Date -> {
                    super.find(findOptions, v1)
                }
                else -> {
                    super.find(findOptions, v1.toString())
                }
            }
        }

        lateinit var v2: Any
        if (params.size == 2) {
            v2 = params[1]
            if (v1 is Number) {
                super.find(findOptions, v1.toLong(), v2.toString())
            } else if (v1 is Date) {
                super.find(findOptions, v1, v2.toString())
            } else if (v2 is Number) {
                super.find(findOptions, v1.toString(), v2.toLong())
            } else if (v2 is Date) {
                super.find(findOptions, v1.toString(), v2)
            } else {
                super.find(findOptions, v1.toString(), v2.toString())
            }
        }

        lateinit var v3: Any
        if (params.size == 3) {
            v3 = params[2]
            if (v2 is Number) {
                super.find(findOptions, v1.toString(), v2.toLong(), v3.toString())
            } else if (v2 is Date) {
                super.find(findOptions, v1.toString(), v2, v3.toString())
            }
        }

        lateinit var v4: Any
        if (params.size > 3) {
            v4 = params[3]
            super.find(findOptions, v1.toString(), (v2 as Number).toLong(), v3 as Date, v4.toString())
        }

        return this
    }

    private fun getCount(params: ArrayList<Any>): Long {
        if (params.isEmpty()) {
            return 50
        }

        var value = params.first()
        if (value is Number) {
            params.removeFirst()
            return value.toLong()
        }

        value = params.last()
        if (value is Number) {
            params.removeLast()
            return value.toLong()
        }

        return 50
    }

    internal class ParamOption(args: Array<Any>) {
        val options = ArrayList<Char>()
        val params = ArrayList<Any>()

        init {
            args.forEach {
                if (it is Char) {
                    options.add(it)
                } else {
                    params.add(it)
                }
            }
        }

        val paramStrArr: Array<String> get() = params.map { it.toString() }.toTypedArray()

        internal inline fun <reified T : Option> getOptions(values: Array<T>): Array<T> {
            return values.filter { options.contains(it.acronym()) }.toTypedArray()
        }
    }
}
