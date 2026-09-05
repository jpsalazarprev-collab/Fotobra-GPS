# Corrección Kotlin 2.3.21 - compilerOptions DSL

## Error exacto

El build fallaba en:

`app/build.gradle.kts:34`

con:

`Using 'jvmTarget: String' is an error. Please migrate to the compilerOptions DSL.`

## Antes

```kotlin
kotlinOptions {
    jvmTarget = "17"
}
```

## Ahora

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}
```

Esto es lo correcto para Kotlin 2.3.21.

## Configuración mantenida

- Kotlin 2.3.21
- AGP 8.10.1
- Gradle 8.11.1
- Java 17
- compileSdk 36
- targetSdk 35
- CameraX 1.6.2
- Play Services Location 21.4.0

Versión Fotobra GPS:
- versionCode 5
- versionName 1.0.4
