package org.code4everything.hutool

import cn.hutool.core.io.FileUtil
import cn.hutool.core.lang.JarClassLoader
import cn.hutool.core.util.ReflectUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.http.HttpUtil
import java.io.File
import java.lang.reflect.Modifier
import org.code4everything.hutool.converter.FileConverter

object Plugins {

    const val CLASS_NAME = "org.code4everything.hutool.PluginEntry"

    @JvmStatic
    fun install(name: String): String {
        val pluginHome = Hutool.homeDir + File.separator + "plugins"
        if ("--uninstall" == Hutool.ARG.params.getOrNull(1)) {
            FileUtil.del(pluginHome + File.separator + StrUtil.addSuffixIfNot(name, ".jar"))
            return "$name uninstalled success"
        }

        val fileConverter = FileConverter()
        val plugin = if (name.startsWith("http://") || name.startsWith("https://")) {
            HttpUtil.downloadFileFromUrl(name, fileConverter.string2Object("~/Downloads"))
        } else fileConverter.string2Object(name)

        if (!FileUtil.exist(plugin)) {
            return "install failed: file not exists"
        }
        if (!plugin.name.endsWith("jar")) {
            return "install failed: plugin is not a jar file"
        }

        val classLoader = JarClassLoader()
        classLoader.addJar(plugin)
        try {
            val entryClass = classLoader.loadClass(CLASS_NAME)
            val method = (ReflectUtil.getMethod(entryClass, false, "run")
                ?: return "install failed: method 'run()' not found in entry class")
            if (!Modifier.isStatic(method.modifiers) || !Modifier.isPublic(method.modifiers)) {
                return "install failed: 'run()' is not a public static method"
            }
            if (method.returnType == Void.TYPE) {
                return "install failed: method no result returned"
            }
        } catch (e: Exception) {
            return "install failed: '$CLASS_NAME' entry class not found"
        }

        FileUtil.copy(plugin, File(pluginHome + File.separator + plugin.name), true)
        return "plugin install success: " + plugin.name.substring(0, plugin.name.length - 4)
    }
}
