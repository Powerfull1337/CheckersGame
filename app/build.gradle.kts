import java.util.Properties
import java.io.FileInputStream

plugins {
   alias(libs.plugins.android.application)
   alias(libs.plugins.kotlin.android)
   alias(libs.plugins.kotlin.compose)
   id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
}


val env = Properties()
val envFile = rootProject.file(".env")
if (envFile.exists()) {
   env.load(FileInputStream(envFile))
}

android {
   namespace = "com.example.checkersgame"
   compileSdk = 36

   defaultConfig {
      applicationId = "com.example.checkersgame"
      minSdk = 27
      targetSdk = 36
      versionCode = 1
      versionName = "1.0"

      testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


      val hostUrl = env.getProperty("HOST_URL") ?: "http://10.0.2.2:8080"

      buildConfigField("String", "HOST_URL", "\"$hostUrl\"")
   }

   buildTypes {
      release {
         isMinifyEnabled = false
         proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      }
   }
   compileOptions {
      sourceCompatibility = JavaVersion.VERSION_11
      targetCompatibility = JavaVersion.VERSION_11
   }
   kotlinOptions {
      jvmTarget = "11"
   }
   buildFeatures {
      compose = true

      buildConfig = true
   }
}

dependencies {
   implementation(libs.androidx.core.ktx)
   implementation(libs.androidx.lifecycle.runtime.ktx)
   implementation(libs.androidx.activity.compose)
   implementation(platform(libs.androidx.compose.bom))
   implementation(libs.androidx.compose.ui)
   implementation(libs.androidx.compose.ui.graphics)
   implementation(libs.androidx.compose.ui.tooling.preview)
   implementation(libs.androidx.compose.material3)
   testImplementation(libs.junit)
   androidTestImplementation(libs.androidx.junit)
   androidTestImplementation(libs.androidx.espresso.core)
   androidTestImplementation(platform(libs.androidx.compose.bom))
   androidTestImplementation(libs.androidx.compose.ui.test.junit4)
   debugImplementation(libs.androidx.compose.ui.tooling)
   debugImplementation(libs.androidx.compose.ui.test.manifest)
   implementation("io.ktor:ktor-client-auth:2.3.7")
   implementation("io.ktor:ktor-client-core:2.3.7")
   implementation("io.ktor:ktor-client-cio:2.3.7")
   implementation("io.ktor:ktor-client-websockets:2.3.7")
   implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
   implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
}