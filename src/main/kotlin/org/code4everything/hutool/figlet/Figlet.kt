package org.code4everything.hutool.figlet

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import com.github.lalyos.jfiglet.FigletFont
import java.io.File
import org.code4everything.hutool.Hutool

object Figlet {

    @JvmStatic
    fun ascii(txt: String, font: String): String {
        val flf = StrUtil.addSuffixIfNot(font, ".flf")
        var file = FileUtil.file(Hutool.homeDir, "fonts", "figlet", flf)
        if (!FileUtil.exist(file)) {
            file = FileUtil.file(Hutool.HUTOOL_USER_HOME, "fonts", "figlet", flf)
        }
        if (!FileUtil.exist(file)) {
            val remoteFont = "http://www.figlet.org/fontdb.cgi"
            val fontHome = Hutool.HUTOOL_USER_HOME + File.separator + "fonts" + File.separator + "figlet" + File.separator
            return "font not found: $flf, please download it from '$remoteFont', then move it into '$fontHome'"
        }
        return FigletFont.convertOneLine(file, txt)
    }
}
