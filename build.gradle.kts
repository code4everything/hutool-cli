import org.jetbrains.kotlin.konan.file.bufferedReader

plugins {
    java
    `maven-publish`
    id("org.jetbrains.kotlin.jvm") version "1.6.0-RC"
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
    mavenCentral()
}

val jcommanderVersion = "1.81"
val hutoolVersion = "5.7.16"
val oshiVersion = "5.8.5"
val emojiVersion = "5.1.1"
val zxingVersion = "3.4.1"
val pinyinVersion = "2.5.1"
val fastjsonVersion = "1.2.78"
val javassistVersion = "3.28.0-GA"
val junitVersion = "4.13.2"
val chalkVersion = "1.0.2"
val qlExpressVersion = "3.2.6"

dependencies {
    implementation("com.beust:jcommander:$jcommanderVersion")
    implementation("cn.hutool:hutool-core:$hutoolVersion")
    implementation("cn.hutool:hutool-system:$hutoolVersion")
    implementation("com.github.oshi:oshi-core:$oshiVersion")
    implementation("cn.hutool:hutool-crypto:$hutoolVersion")
    implementation("cn.hutool:hutool-http:$hutoolVersion")
    implementation("cn.hutool:hutool-script:$hutoolVersion")
    implementation("cn.hutool:hutool-extra:$hutoolVersion")
    implementation("com.vdurmont:emoji-java:$emojiVersion")
    implementation("com.google.zxing:core:$zxingVersion")
    implementation("com.belerweb:pinyin4j:$pinyinVersion")
    implementation("com.alibaba:fastjson:$fastjsonVersion")
    implementation("org.javassist:javassist:$javassistVersion")
    implementation("com.github.tomas-langer:chalk:$chalkVersion")
    implementation("com.alibaba:QLExpress:$qlExpressVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.bouncycastle:bcprov-jdk15on:1.69")

    testImplementation("junit:junit:$junitVersion")
}

val group = "org.code4everything"
val version = "1.4"
val hutoolCliVersion = version
val description = "hutool-cli"
java.sourceCompatibility = JavaVersion.VERSION_1_8

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

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
}

val isWin = System.getProperty("os.name").toLowerCase().contains("windows")

tasks.build {
    doFirst {
        val stream = Runtime.getRuntime().exec("git pull").inputStream
        val reader = bufferedReader(stream)
        while (true) {
            val line: String? = reader.readLine()
            if (line == null) break else println(line)
        }
    }

    doLast {
        copy {
            from("./build/libs/hutool.jar")
            into("./hutool")
        }
    }
}

tasks.register("pack") {
    group = "build"
    description = "Clean and build a executable jar, then move to './hutool' folder."
    dependsOn("clean", "build")
}

tasks.register("install") {
    group = "build"
    description = "Execute pack task, then build './src/main/go/hutool.go'."
    dependsOn("pack")

    exec {
        workingDir("./src/main/go")
        if (isWin) {
            commandLine("cmd", "/c", "go build hutool.go")
        } else {
            commandLine("bash", "-c", "go build hutool.go")
        }
    }

    copy {
        from("./src/main/go")
        into("./hutool/bin")
        exclude("hutool.go")
        rename("hutool", "hu")
    }

    delete("./src/main/go/hutool.exe")
    delete("./src/main/go/hutool")
}

val platforms = listOf("windows", "linux", "darwin")

for (i in platforms.indices) {
    tasks.register("release$i") {
        val osName = platforms[i]
        group = "build"
        description = "Only build go for $osName platform."
        if (isWin) {
            return@register
        }

        exec {
            workingDir("./src/main/go")
            commandLine("bash", "-c", "CGO_ENABLED=0 GOOS=${osName} GOARCH=amd64 go build hutool.go")
        }

        copy {
            from("./src/main/go")
            into("./hutool/bin/")
            exclude("hutool.go")
            val newName = if (osName == "darwin") "hu-mac" else "hu"
            rename("hutool", newName)
        }

        delete("./src/main/go/hutool.exe")
        delete("./src/main/go/hutool")
    }
}

tasks.register("release", type = Zip::class) {
    group = "build"
    description = "Build jar and go, and zip it to a publishable zip."
    dependsOn("pack", "release0", "release1", "release2")
    archiveFileName.set("hu-${hutoolCliVersion}.zip")
    destinationDirectory.set(File("."))
    from("./hutool")
    exclude("method")
    exclude("*.json")
}

tasks.compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
