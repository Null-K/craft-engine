plugins {
    id("com.gradleup.shadow") version "9.0.0-beta11"
    id("maven-publish")
}

repositories {
    maven("https://jitpack.io/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.momirealms.net/releases/")
    mavenCentral()
}

dependencies {
    compileOnly(project(":core"))
    compileOnly(project(":shared"))
    compileOnly(project(":bukkit:compatibility"))
    compileOnly(project(":bukkit:legacy"))
    // Anti Grief
    compileOnly("com.github.Xiao-MoMi:AntiGriefLib:${rootProject.properties["anti_grief_version"]}")
    // NBT
    compileOnly("net.momirealms:sparrow-nbt:${rootProject.properties["sparrow_nbt_version"]}")
    compileOnly("net.momirealms:sparrow-util:${rootProject.properties["sparrow_util_version"]}")
    // NMS
    compileOnly("net.momirealms:craft-engine-nms-helper:${rootProject.properties["nms_helper_version"]}")
    // Platform
    compileOnly("io.papermc.paper:paper-api:${rootProject.properties["paper_version"]}-R0.1-SNAPSHOT")
    // OpenGL Math
    compileOnly("org.joml:joml:${rootProject.properties["joml_version"]}")
    // Gson
    compileOnly("com.google.code.gson:gson:${rootProject.properties["gson_version"]}")
    // Guava
    compileOnly("com.google.guava:guava:${rootProject.properties["guava_version"]}")
    // FastUtil
    compileOnly("it.unimi.dsi:fastutil:${rootProject.properties["fastutil_version"]}")
    // Netty
    compileOnly("io.netty:netty-all:${rootProject.properties["netty_version"]}")
    // ByteBuddy
    compileOnly("net.bytebuddy:byte-buddy:${rootProject.properties["byte_buddy_version"]}")
    // Command
    compileOnly("org.incendo:cloud-core:${rootProject.properties["cloud_core_version"]}")
    compileOnly("org.incendo:cloud-minecraft-extras:${rootProject.properties["cloud_minecraft_extras_version"]}")
    compileOnly("org.incendo:cloud-paper:${rootProject.properties["cloud_paper_version"]}")
    // YAML
    compileOnly(files("${rootProject.rootDir}/libs/boosted-yaml-${rootProject.properties["boosted_yaml_version"]}.jar"))
    // Item Tag
    compileOnly("com.saicone.rtag:rtag:${rootProject.properties["rtag_version"]}")
    compileOnly("com.saicone.rtag:rtag-item:${rootProject.properties["rtag_version"]}")
    // Adventure
    compileOnly("net.kyori:adventure-api:${rootProject.properties["adventure_bundle_version"]}")
    compileOnly("net.kyori:adventure-platform-bukkit:${rootProject.properties["adventure_platform_version"]}")
    compileOnly("net.kyori:adventure-text-minimessage:${rootProject.properties["adventure_bundle_version"]}")
    compileOnly("net.kyori:adventure-text-serializer-gson:${rootProject.properties["adventure_bundle_version"]}") {
        exclude("com.google.code.gson", "gson")
    }
    // Data Fixer Upper
    compileOnly("com.mojang:datafixerupper:${rootProject.properties["datafixerupper_version"]}")
    // BStats
    compileOnly("org.bstats:bstats-bukkit:${rootProject.properties["bstats_version"]}")
    // Aho-Corasick java implementation
    compileOnly("org.ahocorasick:ahocorasick:${rootProject.properties["ahocorasick_version"]}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
    dependsOn(tasks.clean)
}

artifacts {
    archives(tasks.shadowJar)
}

tasks {
    shadowJar {
        archiveClassifier = ""
        archiveFileName = "craft-engine-bukkit-${rootProject.properties["project_version"]}.jar"
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
    }
}

publishing {
    repositories {
        maven {
            url = uri("https://repo.momirealms.net/releases")
            credentials(PasswordCredentials::class) {
                username = System.getenv("REPO_USERNAME")
                password = System.getenv("REPO_PASSWORD")
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "net.momirealms"
            artifactId = "craft-engine-bukkit"
            version = rootProject.properties["project_version"].toString()
            artifact(tasks["sourcesJar"])
            from(components["shadow"])
            pom {
                name = "CraftEngine API"
                url = "https://github.com/Xiao-MoMi/craft-engine"
                licenses {
                    license {
                        name = "GNU General Public License v3.0"
                        url = "https://www.gnu.org/licenses/gpl-3.0.html"
                        distribution = "repo"
                    }
                }
            }
        }
    }
}
