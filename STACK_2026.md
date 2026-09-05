# Fotobra GPS — Stack actualizado 2026

Actualización realizada para usar versiones estables vigentes a septiembre de 2026.

## Build

- Android Gradle Plugin: 9.4.0
- Gradle: 9.6.0
- JDK: 17
- Kotlin Gradle Plugin: 2.4.10
- Kotlin: integrado con AGP 9
- compileSdk: 37.0
- targetSdk: 37
- Build Tools: 37.0.0
- Android Platform: API 37.0 (Android 17)

## Librerías

- AndroidX Core KTX: 1.19.0
- AppCompat: 1.8.0
- Activity KTX: 1.13.0
- Lifecycle Runtime KTX: 2.11.0
- Material Components: 1.14.0
- CameraX: 1.6.2
- Google Play Services Location: 21.4.0
- RecyclerView: 1.4.0
- Preference KTX: 1.2.1
- ExifInterface: 1.4.2

Preference y ExifInterface se mantienen en sus últimas versiones estables disponibles,
aunque esas versiones no fueron publicadas en 2026. No se usan betas ni alphas solo para
forzar una fecha de publicación.

## GitHub Actions

- actions/checkout@v6
- actions/setup-java@v6
- gradle/actions/setup-gradle@v6
- actions/upload-artifact@v7

Estas acciones usan runtimes modernos y eliminan la dependencia anterior de Node 20.

## Aplicación

- versionCode: 6
- versionName: 1.1.0
- APK generado: Fotobra-GPS-2026.apk


## Corrección API 37

AndroidX Core 1.19.0 requiere compileSdk 37 o superior.
Por eso esta versión compila y apunta a Android 17 / API 37.

Se mantiene Build Tools 36.0.0 porque es la versión predeterminada/recomendada
para Android Gradle Plugin 9.4.0.


## Paquete sdkmanager correcto para Android 17

GitHub instala:
- `platforms;android-37.0`
- `build-tools;37.0.0`

No usar `platforms;android-37`, porque ese identificador no está publicado
con ese nombre en el repositorio consultado por sdkmanager.
