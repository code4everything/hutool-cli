package org.code4everything.hutool.figlet

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import com.github.lalyos.jfiglet.FigletFont
import java.io.File
import org.code4everything.hutool.HelpInfo
import org.code4everything.hutool.Hutool
import org.code4everything.hutool.IOConverter
import org.code4everything.hutool.converter.LineSepConverter

/// official figlet: http://www.figlet.org/
/// java figlet implementation: https://github.com/lalyos/jfiglet
object Figlet {

    private val fontHome by lazy { listOf(Hutool.HUTOOL_USER_HOME, "fonts", "figlet").joinToString(File.separator) }

    @JvmStatic
    @HelpInfo(helps = [], callbackMethodName = "help")
    fun ascii(txt: String, font: String): String {
        val flf = StrUtil.addSuffixIfNot(font, ".flf")
        var file = FileUtil.file(Hutool.homeDir, "fonts", "figlet", flf)
        if (!FileUtil.exist(file)) {
            file = FileUtil.file(Hutool.HUTOOL_USER_HOME, "fonts", "figlet", flf)
        }
        if (!FileUtil.exist(file)) {
            val remoteFont = "http://www.figlet.org/fontdb.cgi"
            return "font not found: $flf, please download it from '$remoteFont', then move it into '$fontHome'"
        }
        return FigletFont.convertOneLine(file, txt)
    }

    @JvmStatic
    @IOConverter(LineSepConverter::class)
    fun help(): List<String> = listOf(
        "example: 'banner-text'", "",
        "param1: the text to generate",
        "param2: the customize font name", "",
        "figlet font list: http://www.figlet.org/fontdb.cgi",
        "use customize font: download source font to '$fontHome'"
    )
}
