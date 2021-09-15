plugins {
    kotlin("multiplatform") version "1.5.30"
    kotlin("plugin.serialization") version "1.5.30"
//    kotlin("native.cocoapods") version "1.5.21"
    id("maven-publish")
}
group = "com.blackstone"
version = "0.1.0-SNAPSHOT"

repositories {
    gradlePluginPortal()
    google()
    maven ("https://dl.bintray.com/korlibs/korlibs" )
    mavenCentral()
}
kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    val iosTarget: (String, org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget.() -> Unit) -> org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget =
        if (System.getenv("SDK_NAME")?.startsWith("iphoneos") == true)
            ::iosArm64
        else
            ::iosX64

    iosTarget("ios") {}

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("net.mamoe.yamlkt:yamlkt:0.10.0")
                implementation("com.soywiz.korlibs.krypto:krypto:2.2.0")
                implementation( "com.benasher44:uuid:0.2.2")
                implementation("com.soywiz.korlibs.klock:klock:2.2.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0-RC")
            }

        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting
//        {
////            dependencies {
////                implementation("com.soywiz.korlibs.klock:klock-jvm:$klock_version")
////            }
//        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        val iosMain by getting
        val iosTest by getting
    }
}