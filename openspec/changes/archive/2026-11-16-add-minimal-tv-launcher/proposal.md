# Proposal

## Why

La TV del usuario (AI PONT SA, Android 14, MediaTek mt5867, **1.4 GB de RAM**) usa el
launcher de Google TV (`com.google.android.apps.tv.launcherx`), que está pensado para
equipos con mucha más memoria: en esta TV compite constantemente por la RAM y la
interfaz se siente pesada. Además, su pantalla de inicio (rejilla horizontal de apps +
tarjetas de recomendaciones) no encaja con el uso real del usuario, que quiere un
menú vertical, sobrio y con tipografía.

Se reemplaza ese launcher por uno propio, mínimo (~150 KB, sin trackers ni anuncios) y
se ajustan los parámetros del sistema que afectan a la fluidez. Todo el proceso es
reversible: el launcher original sigue instalado y se guarda un script de restauración.

## What Changes

- **Nuevo launcher para Android TV**, escrito en Kotlin, sin dependencias de terceros:
  - pantalla de inicio con **menú vertical**, navegación con D-pad (arriba/abajo),
    el elemento enfocado **se agranda** y se resalta;
  - **tipografía cuidada** como elemento principal de la UI (el icono es secundario);
  - **apps destacadas** en la parte superior de la lista;
  - entrada de **Configuración** al final de la lista;
  - **fondo de montaña** (imagen propia, generada para el proyecto) como wallpaper;
  - textos de la UI **en español**.
- **Ajustes del launcher** (pantalla propia, alcanzable desde el final de la lista):
  elegir apps destacadas, elegir o restablecer el fondo, ajustar tamaño de tipografía.
- **Ajuste de rendimiento del dispositivo** vía ADB (Android 14, sin root):
  animaciones del sistema al mínimo, forzado del renderizador **SkiaGL** (la GPU es una
  Mali-G31, sin Vulkan), limpieza de procesos en segundo plano y restricciones de apps
  en background.
- **Script de reversión** `restaurar.sh` con los valores originales de cada ajuste y un
  comando para volver al launcher de Google TV.
- **Publicación en GitHub** del proyecto como repositorio open source.

## Capabilities

### New Capabilities
- `launcher-home-screen`: comportamiento visible de la pantalla de inicio (composición de
  la lista vertical, foco y escalado, tipografía, apps destacadas, wallpaper, navegación
  con mando).
- `launcher-settings`: comportamiento de la pantalla de ajustes del launcher (selección de
  apps destacadas, elección del fondo, escala de tipografía) y su persistencia.
- `device-performance-tuning`: ajustes de sistema aplicados vía ADB para mejorar la
  fluidez en la TV objetivo, y su reversibilidad.

### Modified Capabilities
<!-- Ninguna: es el primer change del proyecto. -->

## Impact

- **Nuevo repositorio**: `~/dev/personal/minimal-tv-launcher` (proyecto Android vacío,
  se inicializa con Gradle + AGP).
- **Dispositivo**: se instala una app nueva en la TV y se cambia la actividad HOME con
  `cmd package set-home-activity`. Se modifican `settings global` (escalas de animación)
  y `debug.hwui.renderer`. Sin root, sin cambios en apps de sistema.
- **Rollback**: `restaurar.sh` devuelve los `settings` originales y el launcher previo.
- **GitHub**: repositorio nuevo con licencia open source, `README` en inglés.
