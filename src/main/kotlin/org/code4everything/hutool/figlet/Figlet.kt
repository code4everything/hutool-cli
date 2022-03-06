package org.code4everything.hutool.figlet

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import com.github.lalyos.jfiglet.FigletFont
import org.code4everything.hutool.Hutool

object Figlet {

    @JvmStatic
    fun ascii(txt: String, font: String): String {
        val flf = StrUtil.addSuffixIfNot(font, ".flf")
        var file = FileUtil.file(Hutool.homeDir, "fonts", "figlet", flf)
        if (!FileUtil.exist(file)) {
            file = FileUtil.file(Hutool.HUTOOL_USER_HOME, "fonts", "figlet", flf)
        }
        return FigletFont.convertOneLine(file, txt)
    }
}
