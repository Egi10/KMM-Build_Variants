import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.kotlin.gradle.plugin.cocoapods.KotlinCocoapodsPlugin
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import java.util.regex.Pattern

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.buildConfig)
}

configureBuildKonfigFlavorFromAndroidTasks()
configureBuildKonfigFlavorFromIOSTasks()

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../iosBuildVariantsApp/Podfile")
        framework {
            baseName = "shared"
            isStatic = true
        }

        xcodeConfigurationToNativeBuildType["Staging"] = NativeBuildType.DEBUG
        xcodeConfigurationToNativeBuildType["UAT"] = NativeBuildType.DEBUG
        xcodeConfigurationToNativeBuildType["ProdDebug"] = NativeBuildType.DEBUG
        xcodeConfigurationToNativeBuildType["ProdRealise"] = NativeBuildType.RELEASE
    }
    
    sourceSets {
        commonMain.dependencies {
            //put your multiplatform dependencies here
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "id.buaja.kmm_buildvariants"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

buildkonfig {
    packageName = "id.buaja.kmm_buildvariants"

    /**
     * Nilai Default BuildKonfig
     *
     * Blok kode ini digunakan untuk mendefinisikan nilai default untuk field BuildKonfig.
     * Nilai default ini akan digunakan jika nilai BuildKonfig tidak ditentukan
     * di file gradle.properties atau flavor yang ditentukan tidak ada.
     *
     * Contohnya, jika Anda mengatur buildkonfig.flavor=NotFound
     * di file gradle.properties, nilai default untuk field name akan digunakan.
     * Hal ini karena flavor NotFound tidak ada di project Anda.
     */
    defaultConfigs {
        buildConfigField(STRING, "name", "value")
    }

    // flavor is passed as a first argument of defaultConfigs
    defaultConfigs("staging") {
        buildConfigField(STRING, "name", "stagingValue")
    }
    defaultConfigs("UAT") {
        buildConfigField(STRING, "name", "UATValue")
    }
    defaultConfigs("prod") {
        buildConfigField(STRING, "name", "prodValue")
    }
}

fun configureBuildKonfigFlavorFromAndroidTasks() {
    val projectProperties = project.gradle.startParameter.projectProperties

    if (projectProperties.containsKey("buildkonfig.flavor")) {
        // prefer cli parameter
        println("buildkonfig.flavor=${projectProperties["buildkonfig.flavor"]}")
        return
    }

    /**
     * androidBuildVariantsApp = samakan dengan nama project Android
     */
    val pattern = "^:androidBuildVariantsApp:(assemble|test|bundle|extractApksFor)(\\w+)(Release|Debug)(|UnitTest)\$"
    val runningTasks = project.gradle.startParameter.taskNames

    val matchingTasks = runningTasks.find {
        it.matches(pattern.toRegex())
    }
    if (matchingTasks == null) {
        println("matchingTasks.flavor= NULL")
        return
    }

    val matcher = Pattern.compile(pattern)
        .matcher(matchingTasks)

    if (!matcher.find()) {
        println("matcher.flavor=NOT FOUND")
        return
    }

    val flavor = matcher.group(2)
    val buildkonfigFlavor = when(flavor) {
        "Staging" -> "staging"
        "Uat" -> "uat"
        else -> "prod"
    }

    println("presentation-base:buildkonfig.flavor=${buildkonfigFlavor}")
    project.setProperty("buildkonfig.flavor", buildkonfigFlavor)
}

fun configureBuildKonfigFlavorFromIOSTasks() {
    val property = project.findProperty(KotlinCocoapodsPlugin.CONFIGURATION_PROPERTY)

    property?.let {
        val flavor = when(it.toString()) {
            "Staging" -> "staging"
            "UAT" -> "uat"
            else -> "prod"
        }

        project.setProperty("buildkonfig.flavor", flavor)
    }
}