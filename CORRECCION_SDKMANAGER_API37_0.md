# Corrección sdkmanager Android 17 / API 37.0

## Error observado

GitHub Actions falló con:

`Warning: Failed to find package 'platforms;android-37'`

La causa es el nombre del paquete publicado por sdkmanager.

## Nombre incorrecto

`platforms;android-37`

## Nombre correcto

`platforms;android-37.0`

También se usa:

`build-tools;37.0.0`

## Configuración Gradle

Se cambió `compileSdk = 37` por la sintaxis explícita:

```kotlin
compileSdk {
    version = release(37) {
        minorApiLevel = 0
    }
}

buildToolsVersion = "37.0.0"
```

Esto hace coincidir exactamente Gradle con la plataforma instalada:

- Android 17
- API 37.0
- Build Tools 37.0.0

Versión de la app:
- versionCode 8
- versionName 1.2.1
