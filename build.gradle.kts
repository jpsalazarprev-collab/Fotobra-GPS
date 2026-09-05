buildscript {
    dependencies {
        // AGP 9.x usa Kotlin integrado. Se fija KGP 2.4.10 para usar
        // el compilador estable vigente en 2026.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.10")
    }
}

plugins {
    id("com.android.application") version "9.4.0" apply false
}
