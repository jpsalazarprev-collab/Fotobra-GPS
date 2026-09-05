# Corrección Kotlin 2.3 / Google Play Services Location

## Error observado

El build llegó correctamente hasta `:app:compileDebugKotlin`, pero falló porque
`play-services-location:21.4.0` contiene metadata de Kotlin 2.3.0 y el proyecto
todavía compilaba con Kotlin 2.0.21.

Mensaje clave:

`Module was compiled with an incompatible version of Kotlin.`
`binary version of its metadata is 2.3.0, expected version is 2.0.0`

## Corrección aplicada

- Kotlin Gradle Plugin: `2.3.20`
- Android Gradle Plugin: `8.10.1`
- Gradle: `8.11.1`
- Java: `17`
- compileSdk: `36`
- targetSdk: `35`
- Google Play Services Location: `21.4.0`
- CameraX: `1.6.2`

Kotlin 2.3.20 es compatible con Gradle 8.11.1 y AGP 8.10.1.

La app queda como versión:
- versionCode: 3
- versionName: 1.0.2
