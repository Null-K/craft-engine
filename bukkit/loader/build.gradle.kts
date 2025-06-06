plugins {
    id("com.gradleup.shadow") version "9.0.0-beta11"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

repositories {
    maven("https://jitpack.io/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.momirealms.net/releases/")
    mavenCentral()
}

dependencies {
    // Platform
    compileOnly("io.papermc.paper:paper-api:${rootProject.properties["paper_version"]}-R0.1-SNAPSHOT")

    implementation(project(":shared"))
    implementation(project(":core"))
    implementation(project(":bukkit"))
    implementation(project(":bukkit:legacy"))
    implementation(project(":bukkit:compatibility"))

    implementation("net.kyori:adventure-platform-bukkit:${rootProject.properties["adventure_platform_version"]}")
    implementation("com.saicone.rtag:rtag-item:${rootProject.properties["rtag_version"]}")
    implementation("net.momirealms:sparrow-util:${rootProject.properties["sparrow_util_version"]}")
    implementation("com.github.Xiao-MoMi:AntiGriefLib:${rootProject.properties["anti_grief_version"]}")
    implementation("net.momirealms:craft-engine-nms-helper:${rootProject.properties["nms_helper_version"]}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
    dependsOn(tasks.clean)
}

bukkit {
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    main = "net.momirealms.craftengine.bukkit.BukkitBootstrap"
    version = rootProject.properties["project_version"] as String
    name = "CraftEngine"
    apiVersion = "1.20"
    authors = listOf("XiaoMoMi")
    contributors = listOf("jhqwqmc", "iqtesterrr")
    softDepend = listOf("PlaceholderAPI", "WorldEdit", "FastAsyncWorldEdit")
    foliaSupported = true
}

artifacts {
    archives(tasks.shadowJar)
}

tasks {
    shadowJar {
        archiveFileName = "${rootProject.name}-plugin-${rootProject.properties["project_version"]}.jar"
        destinationDirectory.set(file("$rootDir/target"))
        relocate("net.kyori", "net.momirealms.craftengine.libraries")
        relocate("net.momirealms.sparrow.nbt", "net.momirealms.craftengine.libraries.nbt")
        relocate("net.momirealms.antigrieflib", "net.momirealms.craftengine.libraries.antigrieflib")
        relocate("com.saicone.rtag", "net.momirealms.craftengine.libraries.tag")
        relocate("org.incendo", "net.momirealms.craftengine.libraries")
        relocate("dev.dejvokep", "net.momirealms.craftengine.libraries")
        relocate("org.apache.commons.io", "net.momirealms.craftengine.libraries.commons.io")
        relocate("org.bstats", "net.momirealms.craftengine.libraries.bstats")
        relocate("com.github.benmanes.caffeine", "net.momirealms.craftengine.libraries.caffeine")
        relocate("net.objecthunter.exp4j", "net.momirealms.craftengine.libraries.exp4j")
        relocate("net.bytebuddy", "net.momirealms.craftengine.libraries.bytebuddy")
        relocate("org.yaml.snakeyaml", "net.momirealms.craftengine.libraries.snakeyaml")
        relocate("org.ahocorasick", "net.momirealms.craftengine.libraries.ahocorasick")
        relocate("net.jpountz", "net.momirealms.craftengine.libraries.jpountz")
    }
}
