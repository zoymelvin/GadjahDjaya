pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // Tambahkan JitPack
        maven { url = uri("https://dl.bintray.com/themidtrans/maven") }
    }
}

rootProject.name = "GadjahDjaya"
include(":app")
