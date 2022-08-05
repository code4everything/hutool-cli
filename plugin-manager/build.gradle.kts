val group = "org.code4everything"
val version = "1.6"
val hutoolCliVersion = version
val description = "hutool command line plugin manager"

dependencies {
    implementation(rootProject)
}

tasks.jar {
    archiveFileName.set("plugin.jar")
    exclude("META-INF")
}

tasks.build {
    doLast {
        copy {
            from("./build/libs/plugin.jar")
            into("../hutool/plugins/")
        }
    }
}

tasks.compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
