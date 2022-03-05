package org.code4everything.hutool

import cn.hutool.core.io.FileUtil
import cn.hutool.core.lang.JarClassLoader
import cn.hutool.core.util.ReflectUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.http.HttpUtil
import java.io.File
import java.lang.reflect.Modifier
import kotlin.streams.toList
import org.code4everything.hutool.converter.FileConverter
import org.code4everything.hutool.converter.LineSepConverter

object PluginEntry {

    private var pluginHome = Hutool.homeDir + File.separator + "plugins"

    @JvmStatic
    @IOConverter(LineSepConverter::class)
    fun run(): List<String> {
        val params = Hutool.ARG.params
        if (params.isEmpty()) {
            return listOf("actions: install, uninstall, list")
        }

        val action = params[0]
        val newParams = params.stream().skip(1).toList()
        return when (action) {
            "install" -> install(newParams)
            "uninstall" -> uninstall(newParams)
            "list" -> list()
            else -> listOf("unsupported action")
        }
    }

    private fun list(): List<String> {
        val plugins = FileUtil.ls(pluginHome)
        return listOf("installed plugins: " + plugins.joinToString(", ") { it.name.removeSuffix(".jar") })
    }

    private fun uninstall(plugins: List<String>): List<String> {
        val failed = hashSetOf<String>()
        val success = arrayListOf<String>()
        plugins.forEach {
            val name = it.removeSuffix(".jar")
            val del = FileUtil.del(pluginHome + File.separator + name + ".jar")
            if (del) success.add(name) else failed.add(name)
        }

        return ArrayList<String>(2).apply {
            if (failed.isNotEmpty()) {
                add("uninstall failed: " + failed.joinToString(", "))
            }
            if (success.isNotEmpty()) {
                add("uninstalled plugins: " + success.joinToString(", "))
            }
        }
    }

    private fun install(plugins: List<String>): List<String> {
        val fileConverter = FileConverter()
        val res = arrayListOf<String>()
        var emptyLine = false

        plugins.forEach {
            if (emptyLine) {
                res.add("")
            }

            emptyLine = true
            val name = it.removeSuffix(".jar")
            res.add("install plugin: $name")
            val plugin = if (it.startsWith("http://") || it.startsWith("https://")) {
                HttpUtil.downloadFileFromUrl(it, fileConverter.string2Object("~/Downloads"))
            } else fileConverter.string2Object(StrUtil.addSuffixIfNot(it, ".jar"))

            if (!FileUtil.exist(plugin)) {
                res.add("install failed: '${plugin.name}' file not exists")
                return@forEach
            }
            if (!plugin.name.endsWith("jar")) {
                res.add("install failed: '${plugin.absolutePath}' is not a jar file")
                return@forEach
            }

            val classLoader = JarClassLoader()
            classLoader.addJar(plugin)

            try {
                val entryClass = classLoader.loadClass(Hutool.PLUGIN_NAME)
                val method = ReflectUtil.getMethod(entryClass, false, "run")
                if (method == null) {
                    res.add("install failed: method 'run()' not found in entry class")
                    return@forEach
                }
                if (!Modifier.isStatic(method.modifiers) || !Modifier.isPublic(method.modifiers)) {
                    res.add("install failed: 'run()' is not a public static method")
                    return@forEach
                }
                if (method.returnType == Void.TYPE) {
                    res.add("install failed: method no result returned")
                    return@forEach
                }
            } catch (e: Exception) {
                res.add("install failed: '${Hutool.PLUGIN_NAME}' entry class not found")
                return@forEach
            }

            FileUtil.copy(plugin, File(pluginHome + File.separator + plugin.name), true)
            res.add("install success: $name")
        }

        return res
    }
}
