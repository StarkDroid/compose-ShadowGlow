plugins {
    id("com.android.library")
    id("com.vanniktech.maven.publish") version "0.34.0"
    id("signing")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "me.trishiraj.shadowglow"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        compose = true
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(groupId = "me.trishiraj", artifactId = "shadowglow", version = "1.0.0")

    pom {
        name = "ShadowGlow"
        description = "A highly customisable Jetpack Compose library for various drop shadow and Glow effects with plug & play modifier."
        inceptionYear = "2025"
        url = "https://github.com/StarkDroid/compose-ShadowGlow"
        licenses {
            license {
                name = "The Apache Software License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "trishiraj"
                name = "Trishiraj"
                url = "https://github.com/StarkDroid"
                email = "trishiraj.247@gmail.com"
            }
        }
        scm {
            connection = "scm:git:git://github.com/StarkDroid/compose-ShadowGlow.git"
            developerConnection = "scm:git:ssh://github.com/StarkDroid/compose-ShadowGlow.git"
            url = "https://github.com/StarkDroid/compose-ShadowGlow"
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
}
