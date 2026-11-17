# Design

## Context

Ver `proposal.md` para la motivación. Restricciones técnicas del dispositivo objetivo
(AI PONT SA, Android 14, mt5867, Mali-G31, **1.4 GB de RAM**, 1280x720) que condicionan
las decisiones:

- La RAM es el recurso escaso: 1.4 GB totales y el launcher de Google TV ya consume la
  mayor parte de lo libre. Cualquier dependencia pesada (Play Services, Material, Leanback)
  se paga en RAM.
- La GPU es una **Mali-G31**, que implementa OpenGL ES 3.2 pero **no Vulkan**. Android 14
  detectará la ausencia de Vulkan y usará SkiaGL por su cuenta; fijarlo de forma
  explícita evita surprises y deja constancia de la intención.
- No hay root: solo `adb shell` (uid 2000). Se pueden tocar `settings global`, propiedades
  `debug.*` y el `HOME` activo, pero no apps de sistema ni el partición `/system`.
- El panel ya es 1280x720 @ 60 Hz. No hay ganancia posible en resolución; la fluidez se
  gana en CPU/GPU por proceso y en latencia de respuesta.
- 4 GB de disco con 1.2 GB libres: el APK debe ser pequeño.

## Goals / Non-Goals

**Goals:**
- APK por debajo de 300 KB, sin dependencias que tiren de Play Services.
- Pantalla de inicio visible en menos de 1 s y scroll sin tirones con 4 núcleos ARMv7.
- Que todo lo que se toque en el dispositivo sea reversible con un comando.
- Código publicable en GitHub: licencia, README, sin dependencias proprietarias.

**Non-Goals:**
- No es una tienda de apps ni un explorador de archivos.
- No se implementa búsqueda de apps por texto (el mando no tiene teclado cómodo); se
  compensa con apps destacadas y orden alfabético.
- No se tocan apps de sistema ni se hace root.
- No se implementan widgets, notificaciones custom ni pantalla de detalle por app.

## Decisions

### 1. Kotlin + AndroidX mínimo, sin Material ni Leanback

Se usa **Kotlin** con solo dos dependencias de AndroidX: `androidx.recyclerview` (para un
scroll fluido con recycle) y `androidx.core:core-ktx`. El tema es del framework
(`android:Theme.Material.NoActionBar`), no de AppCompat.

- **Por qué**: AppCompat y Material heredan de `AppCompatActivity` y cargan una jerarquía
  de vistas y recursos que en 1.4 GB se notan. Leanback (`androidx.leanback`) está pensado
  paraTV y arrastra `play-services` en algunos caminos.
- **Alternativas descartadas**: *Leanback Launcher* (correcta pero pesada y con diseño
 Horizontal, justo lo que se quiere evitar); *Appium/Atvify* (APK de terceros, sin control
  del diseño); *un launcher de F-Droid* (no da el control fino de tipografía y foco).

### 2. Una sola Activity con RecyclerView vertical

`MainActivity` es la actividad HOME. Un `RecyclerView` con `LinearLayoutManager` vertical
sostiene la lista. El fondo es una `ImageView` a pantalla completa detrás.

- **Por qué**: un `RecyclerView` con `setHasFixedSize(true)` y alturas fijas recyclea
  vistas sin relayout, que es lo que evita los tirones en un ARMv7 de 4 núcleos.
- **Alternativa descartada**: `ListView` (más simple pero peor回收 y sin control fino del
  foco); *Compose* (más slick pero más RAM y más complejidad de build para un solo
  recycler).

### 3. El foco se escala con `ViewPropertyAnimator`, no con animaciones de sistema

El escalado del elemento enfocado se anima en la propia vista con
`ViewPropertyAnimator` (escala 1.0 → 1.18, alpha y un desplazamiento de color), con una
duración corta (140 ms).

- **Por qué**: el foco se prepara en el `onFocusChange`, que ocurre en el hilo de UI; la
  animación por hardware no requiere wakelock ni bitmap extra.
- **Consecuencia de diseño**: las escalas de animación del sistema se ponen a 0 en la
  fase de rendimiento (spec `device-performance-tuning`), y el launcher aporta su propia
  animación de foco, que es la que el usuario ve. El resultado es un launcher que responde
  al instante sin perder la sensación de "vivo".

### 4. Fondo: wallpaper del sistema + fondo por defecto en la app

La imagen de montaña se instala como **wallpaper del sistema** con `WallpaperManager`
(permiso normal `SET_WALLPAPER`, sin root) y además se usa como fondo por defecto de la
propia pantalla de inicio. Los ajustes ofrecen "montañas" o "sólido oscuro".

- **Por qué**: poner el wallpaper del sistema hace que la pantalla de inicio no tenga
  un salto visual al cambiar de app, y el launcher puede seguir funcionando aunque el
  wallpaper del sistema cambie después.
- **Alternativa descartada**: guardar el PNG en `assets` y pintarlo siempre (funciona, pero
  duplica la imagen en el APK y no se benefits del hecho de que ya está en el sistema).

### 5. Persistencia en `SharedPreferences`, sin permisos

Apps destacadas (un `Set<String>` de nombres de paquete), fondo elegido y nivel de
tipografía se guardan en las `SharedPreferences` de la app. Sin permiso de almacenamiento:
no hace falta para nada.

- **Por qué**: cero permisos, cero superficie de ataque, y las preferencias sobreviven a
  reinicios sin ningún servicio extra.

### 6. Descubrimiento de apps por `PackageManager`, filtrado

Las apps se listan con `PackageManager.queryIntentActivities` sobre
`Intent.ACTION_MAIN` + `CATEGORY_LAUNCHER`, se filtran las de sistema que no son
lanzadores (Ajustes, la propia app) y se ordenan por etiqueta en español. Las que tienen
`android:enabled=false` o no son lanzadoras quedan fuera.

- **Por qué**: no hace falta permissão `QUERY_ALL_PACKAGES`; `CATEGORY_LAUNCHER` es
  visible para cualquier app sin permisos especiales en Android 11+.

### 7. Sin permiso `INTERNET`

La app no declara `INTERNET`. La pantalla de inicio no necesita red: las apps instaladas
se leen del sistema y las destacadas se guardan localmente.

- **Por qué**: el requisito de arranque sin red del spec se cumple por construcción, y
  además hace imposible que el launcher envíe nada a ningún sitio.

### 8. Fase de rendimiento: solo ajustes reversibles

El script `optimizar.sh` aplica, en este orden:

1. Guarda los valores actuales en `backup/valores-originales.txt` (solo la primera vez).
2. `settings put global window_animation_scale 0`, `transition_animation_scale 0`,
   `animator_duration_scale 0`.
3. `setprop debug.hwui.renderer skiagl` y reinicio de `com.android.systemui` para que los
   procesos nuevos lean la propiedad.
4. `am kill-all` para limpiar procesos en background.
5. `cmd package set-home-activity` para activar el launcher.

- **Por qué 0 y no 0.5**: el objetivo declarado es "más fluido", no "más bonito". Con 0 la
  respuesta es inmediata; la única animación que queda es la del foco, que la aporta el
  propio launcher.
- **Alternativas descartadas**: *bajar la resolución* (ya es 720p, no hay margen);
  *desinstalar apps* (iQIYI y MGSTV ocupan poco y el usuario puede quererlas: se deja la
  decisión abierta, no se aplica por defecto); *`fixed-performance-mode`* (requiere
  permiso que `shell` no tiene en la mayoría de dispositivos: se prueba y, si falla, se
  documenta sin romper nada).

### 9. `restaurar.sh` como parte del repo

Script en bash que lee `backup/valores-originales.txt`, restaura cada `settings global`,
borra `debug.hwui.renderer`, vuelve a activar el launcher de Google TV y puede ejecutarse
las veces que haga falta.

- **Por qué**: vive en el repo, versionado y revisable, no en un chat. El usuario puede
  auditar exactamente qué se toca antes de ejecutarlo.

### 10. Paquete `dev.minimaltv.launcher`, namespace estable

- **Por qué**: un namespace propio y corto, con prefijo `dev.` (sin dominio de
  organización, así que no ata el proyecto a un dominio concreto) y versión explícita en
  el Gradle. Si el proyecto pasa a GitHub, el namespace no impide nada.

## Risks / Trade-offs

- **[El launcher de Google TV sigue instalado y ocupa RAM en background]** → Mitigación:
  el script de optimización añade su proceso a la lista de apps restringidas en
  background (`am set-inactive`) y el propio launcher ofrece la opción de desactivarlo
  desde Ajustes. Verificar que `set-inactive` funciona sin root en esta build; si no,
  se documenta y se deja el launcher activo.
- **[El focus scalado puede perder precisión con apps de terceros]** → Mitigación: la
  navegación es puramente vertical con D-pad; no hay rejilla, así que no hay que elegir
  entre ejes.
- **[Cambiar `HOME` deja al usuario sin launcher si la app se desinstala]** → Mitigación:
  Android vuelve al launcher previo automáticamente al desinstalar; además `restaurar.sh`
  está en el repo y documentado en el README.
- **[Escribir `debug.hwui.renderer` con `setprop` desde `shell` no es persistente entre
  reinicios]** → Mitigación: se aplica en cada ejecución del script y se documenta que
  el ajuste de renderizador es de sesión, mientras que las escalas de animación sí
  persisten.
- **[Un APK propio pierde funciones del launcher de Google (voz, recomendaciones, búsqueda)]**
  → Mitigación: es explícito y aceptado; las apps destacadas y el orden alfabético cubren
  el caso de uso. La búsqueda por voz no está en el alcance.
- **[Gradle/AGP con JDK 25 sin precedente]** → Mitigación: versiones fijadas en el
  wrapper (`gradle-wrapper.properties` con distribución y checksum) para que el build sea
  reproducible en cualquier máquina.

## Migration Plan

1. Compilar el APK en modo `debug` y verificar que arranca en el dispositivo vía
   `adb install` + `adb shell am start`.
2. Ejecutar `optimizar.sh` (guarda los valores originales antes de tocar nada).
3. Activar el launcher con `cmd package set-home-activity` y reiniciar la interfaz.
4. Verificar en la TV: pantalla de inicio, foco, apps, Configuración, arranque < 1 s.
5. Instalar la versión `release` firmada y desinstalar la de `debug`.

**Rollback:** `bash herramientas/restaurar.sh` → restaura ajustes y launcher previo.

## Open Questions

Ninguna bloqueante. Si al medir el arranque la pantalla de inicio supera el segundo, se
puede actuar sobre `SharedPreferences` y el número de vistas recycleadas sin tocar los
specs ni la estructura.
