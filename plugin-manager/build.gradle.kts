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

tasks.register("pack") {
    description = "Clean and build jar, then move to '../hutool/plugins/' folder."
    dependsOn("clean", "build")
}

tasks.compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
