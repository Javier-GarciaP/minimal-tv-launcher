# Tasks

## 1. Andamiaje del proyecto

- [x] 1.1 Crear la estructura Gradle (settings, build raíz, módulo `app`, `gradle.properties`,
      wrapper con distribución fijada) y verificar que `./gradlew tasks` lista las tareas
      del módulo
- [x] 1.2 Declarar en `app/build.gradle` el SDK (compile/target 34, min 21), Kotlin, las dos
      dependencias AndroidX mínimas y el namespace `dev.minimaltv.launcher`; verificar que
      `./gradlew :app:dependencies` resuelve sin conflictos
- [x] 1.3 Declarar el `HomeActivity` como actividad HOME y launcher en el manifiesto, sin
      permiso `INTERNET`; verificar con `aapt dump badging` que el paquete y las
      actividades son correctos y que no aparece `android.permission.INTERNET`
- [x] 1.4 Copiar el fondo de montaña al proyecto y comprobar que el APK resultante pesa
      menos de 300 KB con `./gradlew :app:assembleDebug` y `ls -l`

## 2. Pantalla de inicio

- [x] 2.1 Montar la jerarquía de vistas (fondo a pantalla completa + `RecyclerView` vertical)
      y verificar que la lista aparece en el borde izquierdo con el fondo detrás
- [x] 2.2 Implementar la carga de apps por `PackageManager` (filtro `CATEGORY_LAUNCHER`,
      exclusión de apps de sistema no lanzadoras, orden alfabético por etiqueta) y
      verificar que la lista contiene las apps instaladas y no las de sistema que no
      arrancan
- [x] 2.3 Implementar el escalado del elemento enfocado con `ViewPropertyAnimator` y
      verificar que al navegar con las flechas el foco se agranda y la posición anterior
      vuelve a su tamaño
- [x] 2.4 Implementar la escala de tipografía con tres niveles y verificar que el cambio
      se aplica de inmediato al volver de los ajustes
- [x] 2.5 Instalar el fondo como wallpaper del sistema con `WallpaperManager` y verificar
      que la pantalla de inicio muestra la imagen de montañas por defecto
- [x] 2.6 Añadir tests unitarios de la lógica de filtrado y orden de apps y verificar que
      `./gradlew :app:testDebugUnitTest` pasa

## 3. Apps destacadas

- [x] 3.1 Implementar la persistencia del conjunto de apps destacadas en
      `SharedPreferences` y verificar que sobrevive a la muerte del proceso
- [x] 3.2 Mostrar las apps destacadas agrupadas al principio de la lista y verificar que
      la lista no muestra un grupo vacío cuando no hay ninguna marcada
- [x] 3.3 Añadir tests unitarios del orden final de la lista (destacadas primero,
      alfabéticas después) y verificar que pasan

## 4. Pantalla de ajustes

- [x] 4.1 Implementar la pantalla de ajustes con la lista de apps ordenadas
      alfabéticamente y el estado de destacado de cada una; verificar que se puede marcar
      y desmarcar y que el cambio se refleja en la pantalla de inicio
- [x] 4.2 Implementar la elección de fondo (montañas / sólido oscuro) y el
      restablecimiento del fondo original; verificar que ambos cambian el fondo
- [x] 4.3 Implementar el acceso a los ajustes desde la entrada **Configuración** del final
      de la lista y verificar que se abre la pantalla de ajustes
- [x] 4.4 Añadir tests unitarios de la persistencia de preferencias y verificar que
      `SharedPreferences` sobrevive a la reinstanciación del objeto de preferencias

## 5. Optimización del dispositivo

- [x] 5.1 Escribir `herramientas/optimizar.sh`: guarda los valores originales en
      `backup/valores-originales.txt` solo la primera vez, pone las tres escalas de
      animación a 0, fija `debug.hwui.renderer skiagl` y reinicia `com.android.systemui`;
      verificar que el script termina sin error y que el fichero de valores se crea
- [x] 5.2 Añadir al script la limpieza de procesos en background y el cambio de actividad
      HOME; verificar que `cmd package resolve-activity` devuelve la nueva actividad
- [x] 5.3 Escribir `herramientas/restaurar.sh`: restaura cada valor desde
      `backup/valores-originales.txt`, borra `debug.hwui.renderer` y vuelve a activar el
      launcher previo; verificar que se ejecuta dos veces seguidas sin error
- [x] 5.4 Ejecutar `optimizar.sh` en la TV y verificar con `settings get global` que las
      tres escalas de animación valen 0 y con `getprop debug.hwui.renderer` que vale
      `skiagl`

## 6. Verificación en el dispositivo

- [x] 6.1 Instalar el APK de depuración en la TV y verificar que la pantalla de inicio
      aparece, que el foco navega con las flechas y que las apps arrancan
- [x] 6.2 Medir el tiempo hasta que la pantalla de inicio es visible tras volver de otra
      app y verificar que es menor de un segundo
- [x] 6.3 Verificar que la pantalla de inicio funciona con la red del dispositivo
      desconectada
- [x] 6.4 Verificar que `restaurar.sh` devuelve la TV al launcher de Google TV y que los
      valores de `settings global` vuelven a los originales

## 7. Documentación y publicación

- [x] 7.1 Escribir el `README.md` en inglés con qué es, cómo compilar, cómo instalar en
      una TV por ADB, qué hace `optimizar.sh` y cómo revertir los cambios; verificar que
      los comandos que aparecen se ejecutan tal cual
- [x] 7.2 Añadir `LICENSE` (MIT), `.gitignore` y `CONTRIBUTING.md`; verificar que
      `git status` no muestra artefactos de build
- [x] 7.3 Actualizar `MEMORY.md` con el estado final y el siguiente paso, y crear la nota
      del proyecto en el cerebro del usuario

## Workflow follow-up

- Archivar el change `add-minimal-tv-launcher` cuando la implementación esté revisada.
- Publicar el repositorio en GitHub y comprobar que el build se puede repetir desde una
  clonación limpia.
