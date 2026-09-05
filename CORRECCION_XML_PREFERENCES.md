# Corrección de resources XML

El build anterior ya superó el error de CameraX/API 36.

El nuevo fallo estaba en:

`app/src/main/res/xml/preferences.xml`

Se había usado:

`android:useSimpleSummaryProvider="true"`

Ese atributo no pertenece al framework Android. Pertenece a AndroidX Preference.

Se corrigió a:

- namespace `xmlns:app="http://schemas.android.com/apk/res-auto"`
- atributo `app:useSimpleSummaryProvider="true"`

También se hizo una revisión preventiva:
- todos los XML se comprobaron como XML bien formado;
- CameraX usa `setSurfaceProvider(...)` explícito;
- ya no se consulta el EditText desde el callback de cámara en segundo plano;
- se mantiene compileSdk 36, targetSdk 35, AGP 8.10.1 y Gradle 8.11.1;
- versión de app incrementada a 1.0.1 (versionCode 2).
