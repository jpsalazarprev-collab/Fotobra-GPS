# Corrección API 36 / CameraX 1.6.2

## Error observado

GitHub fallaba en `:app:checkDebugAarMetadata` porque las dependencias CameraX 1.6.2
requieren `compileSdk 36` o superior, mientras Fotobra GPS estaba configurado con API 35.

Además, Android Gradle Plugin 8.9.x tiene soporte oficial máximo hasta API 35.

## Corrección aplicada

- `compileSdk = 36`
- `targetSdk = 35` (se mantiene para no cambiar todavía comportamientos de ejecución)
- Android Gradle Plugin `8.10.1`
- Gradle `8.11.1`
- Java `17`
- CameraX `1.6.2`
- GitHub Actions instala:
  - `platforms;android-36`
  - `build-tools;35.0.0`

## Por qué no subí targetSdk a 36

`compileSdk` permite compilar con las APIs que exige CameraX.
`targetSdk` controla cambios de comportamiento en tiempo de ejecución.
No es necesario cambiar ambos al mismo tiempo para resolver este error.

## Próximo resultado esperado

El paso `checkDebugAarMetadata` ya no debe detenerse por CameraX/API 35.
Si aparece otro error, será posterior y distinto, lo cual permite seguir depurando
desde un punto más avanzado de la compilación.
