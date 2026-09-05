# Fotobra GPS

Aplicación Android nativa escrita desde cero en Kotlin para fotografía de terreno con marca de fecha, hora, ubicación y GPS.

## Tecnología

- Kotlin
- Android nativo
- CameraX
- Google Play Services Location
- MediaStore
- ExifInterface
- AndroidX Preferences
- RecyclerView

## Funciones incluidas

- cámara trasera y frontal;
- CameraX;
- flash/linterna;
- fecha y hora en vivo;
- GPS real;
- precisión GPS;
- dirección aproximada mediante Geocoder;
- nota/proyecto/frente;
- vista previa del sello sobre la cámara;
- sello permanente aplicado a la fotografía;
- marca Fotobra GPS;
- fotografía original opcional;
- galería Fotobra GPS;
- configuración de campos visibles;
- funcionamiento local;
- guardado en `Pictures/Fotobra GPS`.

## Diseño del sello

El sello utiliza un diseño propio Fotobra GPS:

- hora grande inferior izquierda;
- separador vertical amarillo;
- fecha y día;
- dirección;
- latitud y longitud;
- precisión GPS;
- nota;
- bloque Fotobra GPS superior derecho.

## GitHub Actions

El proyecto incluye:

`.github/workflows/build-apk.yml`

En GitHub:

1. Actions
2. Build Fotobra GPS APK
3. Run workflow
4. Esperar verde
5. Descargar Artifact `Fotobra-GPS-APK`

El APK de prueba se compila como `debug`, por lo que viene firmado automáticamente y se puede instalar sin configurar una clave privada propia.

## Importante

Esta es una implementación limpia y original. No contiene código fuente, logos, tipografías ni recursos extraídos de aplicaciones de terceros.


## Compatibilidad Android actualizada

Esta versión usa:
- `compileSdk = 36`
- `targetSdk = 35`
- Android Gradle Plugin `8.10.1`
- Gradle `8.11.1`
- Java `17`
- CameraX `1.6.2`

El cambio a compileSdk 36 es necesario porque CameraX 1.6.2 requiere compilar contra Android API 36 o superior.


## Revisión integral v7

Se agregó `scripts/preflight.py`, que GitHub ejecuta antes de compilar y revisa:

- versiones críticas de Gradle/Kotlin/Android;
- XML;
- Manifest;
- Activities;
- IDs usados por ViewBinding;
- delimitadores básicos Kotlin;
- marcadores TODO/FIXME;
- configuración de CameraX y GPS.

También se reforzó el funcionamiento:
- GPS con timeout de 10 segundos;
- fallback a última ubicación disponible;
- geocodificación fuera del hilo principal;
- bloqueo del botón durante una captura;
- validación real de flash;
- permisos de almacenamiento para Android 7–9;
- galería protegida si no existe visor externo.


## Stack 2026

Esta rama fue actualizada a la plataforma Android vigente en 2026:
AGP 9.4.0, Gradle 9.6.0, Kotlin 2.4.10 con Kotlin integrado,
compile/target SDK 36, CameraX 1.6.2 y Google Play Services Location 21.4.0.

GitHub Actions también fue actualizado a runtimes Node 24.
