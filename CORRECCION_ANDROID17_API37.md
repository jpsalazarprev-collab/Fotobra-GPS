# Corrección Android 17 / API 37

El build anterior ya compilaba Kotlin correctamente, pero fallaba en:

`:app:checkDebugAarMetadata`

porque:

- `androidx.core:core:1.19.0`
- `androidx.core:core-ktx:1.19.0`

requieren `compileSdk 37` o superior.

Se actualizó:

- compileSdk: 37
- targetSdk: 37
- Android Platform instalada en GitHub: android-37
- AGP: 9.4.0
- Gradle: 9.6.0
- JDK: 17
- Kotlin: 2.4.10
- Build Tools: 36.0.0
- CameraX: 1.6.2
- AndroidX Core KTX: 1.19.0

También se reemplazaron constructores de Locale obsoletos por:

`Locale.forLanguageTag("es-CL")`

Versión de Fotobra GPS:
- versionCode: 7
- versionName: 1.2.0
