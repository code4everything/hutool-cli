plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.0-RC"
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    val hutoolVersion = "5.7.22"
    dependencies {
        apply(plugin = "org.jetbrains.kotlin.jvm")
        implementation("com.beust:jcommander:1.82")
        implementation("cn.hutool:hutool-core:$hutoolVersion")
        implementation("cn.hutool:hutool-system:$hutoolVersion")
        implementation("cn.hutool:hutool-crypto:$hutoolVersion")
        implementation("cn.hutool:hutool-http:$hutoolVersion")
        implementation("cn.hutool:hutool-extra:$hutoolVersion")
        implementation("com.vdurmont:emoji-java:5.1.1")
        implementation("com.google.zxing:core:3.4.1")
        implementation("com.belerweb:pinyin4j:2.5.1")
        implementation("com.alibaba:fastjson:1.2.80")
        implementation("org.javassist:javassist:3.28.0-GA")
        implementation("com.github.tomas-langer:chalk:1.0.2")
        implementation("com.alibaba:QLExpress:3.2.7")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("org.bouncycastle:bcprov-jdk15on:1.70")
        implementation("com.github.lalyos:jfiglet:0.0.8")
        implementation("net.thisptr:jackson-jq:1.0.0-preview.20210928")
        implementation("org.unix4j:unix4j-command:0.6")
        testImplementation("junit:junit:4.13.2")
    }
}

val group = "org.code4everything"
val version = "1.6"
val description = "hutool-cli"

tasks.jar {
    archiveFileName.set("hutool.jar")

    manifest {
        attributes(mapOf("Manifest-Version" to "1.0", "Main-Class" to "org.code4everything.hutool.Hutool"))
    }

    from(configurations.runtimeClasspath.get().files.map { if (it.isDirectory) it else zipTree(it) })

    exclude("META-INF/*.RSA")
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/AL2.0")
    exclude("META-INF/LGPL2.1")
    exclude("META-INF/LICENSE")
    exclude("META-INF/LICENSE.txt")
    exclude("META-INF/NOTICE.txt")
    exclude("META-INF/versions/9/module-info.class")
    exclude("META-INF/NOTICE")
    exclude("slant.flf")
    exclude("standard.flf")
    exclude("module-info.class")
}

tasks.build {
    doLast {
        copy {
            from("./build/libs/hutool.jar")
            into("./hutool")
        }
    }
}

tasks.compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.javaParameters = true
}

tasks.compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
