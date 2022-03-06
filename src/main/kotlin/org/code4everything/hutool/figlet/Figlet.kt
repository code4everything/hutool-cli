package org.code4everything.hutool.figlet

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import com.github.lalyos.jfiglet.FigletFont
import org.code4everything.hutool.Hutool

object Figlet {

    @JvmStatic
    fun ascii(txt: String, font: String): String {
        val file = FileUtil.file(Hutool.homeDir, "fonts", "figlet", StrUtil.addSuffixIfNot(font, ".flf"))
        return FigletFont.convertOneLine(file, txt)
    }
}
