plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.0.0-beta11"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.16"
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.spongepowered.org/maven/")
}

dependencies {
    implementation(project(":shared"))
    remapper("net.fabricmc:tiny-remapper:${rootProject.properties["tiny_remapper_version"]}:fat")
    paperweightDevelopmentBundle("io.papermc.paper:dev-bundle:1.20.1-R0.1-SNAPSHOT")
    compileOnly("space.vectrix.ignite:ignite-api:${rootProject.properties["ignite_version"]}")
    compileOnly("net.fabricmc:sponge-mixin:${rootProject.properties["mixin_version"]}")
    compileOnly("io.github.llamalad7:mixinextras-common:${rootProject.properties["mixinextras_version"]}")
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

artifacts {
    archives(tasks.reobfJar)
}

tasks {
    reobfJar {
        outputJar.set(file("$rootDir/target/${rootProject.name}-ignite-mod-${rootProject.properties["project_version"]}+mc1.20.1-reobf.jar"))
    }
}
