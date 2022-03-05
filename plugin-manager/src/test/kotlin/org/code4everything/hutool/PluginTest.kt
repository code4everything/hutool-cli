package org.code4everything.hutool

import org.junit.Test

class PluginTest {

    @Test
    fun run() {
        Hutool.test2("plugin uninstall plugin1 plugin2")
        Hutool.test2("plugin install plugin1 plugin2")
        Hutool.test2("plugin list")
    }
}
