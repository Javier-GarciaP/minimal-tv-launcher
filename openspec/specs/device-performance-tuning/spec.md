# device-performance-tuning Specification

## Purpose
Define los ajustes de sistema que se aplican en la TV objetivo para mejorar la fluidez y
la respuesta de la interfaz, y el procedimiento que permite deshacerlos por completo.

## Requirements

### Requirement: Animaciones del sistema al mínimo
Las escalas de animación del sistema (ventana, transición y animador) SHALL quedar
fijadas en 0 para que las transiciones de interfaz sean instantáneas.

#### Scenario: Lectura del valor aplicado
- **WHEN** se consulta el valor de `window_animation_scale`
- **THEN** su valor es 0

#### Scenario: Respuesta de la interfaz
- **WHEN** el usuario abre y cierra una app
- **THEN** la transición se produce de inmediato, sin animación intermedia

### Requirement: Renderizador de gráficos forzado a OpenGL
El renderizador de la capa de interfaz SHALL fijarse en SkiaGL, porque la GPU del
dispositivo (Mali-G31) no implementa Vulkan. La propiedad SHALL fijarse antes de que
arranque la interfaz de usuario.

#### Scenario: Valor de la propiedad
- **WHEN** se consulta `debug.hwui.renderer`
- **THEN** su valor es `skiagl`

#### Scenario: Estabilidad del sistema
- **WHEN** la interfaz se reinicia con la propiedad aplicada
- **THEN** la interfaz arranca sin errores de Vulkan y sigue respondiendo

### Requirement: Limpieza de procesos en segundo plano
El procedimiento SHALL terminar los procesos de apps en background y SHALL poder
reaplicarse en cualquier momento sin dejar el dispositivo inestable.

#### Scenario: Limpieza tras instalar el launcher
- **WHEN** el procedimiento se ejecuta tras la instalación
- **THEN** la lista de procesos en background se reduce y la pantalla de inicio sigue
  respondiendo

### Requirement: Restauración completa
El proyecto SHALL incluir un script `restaurar.sh` que devuelva cada valor modificado a
su estado original y que vuelva a activar el launcher previo. El script SHALL registrar
los valores originales la primera vez que se ejecuta la optimización.

#### Scenario: Restaurar tras optimizar
- **WHEN** el usuario ejecuta `restaurar.sh`
- **THEN** las escalas de animación vuelven a su valor original y el launcher de Google TV
  vuelve a ser la pantalla de inicio

#### Scenario: Ejecutar la restauración dos veces
- **WHEN** el usuario ejecuta `restaurar.sh` por segunda vez
- **THEN** el script termina sin errores y vuelve a dejar el dispositivo en un estado
  coherente

### Requirement: Optimización idempotente
El procedimiento de optimización SHALL poder ejecutarse varias veces sin efectos
acumulativos ni errores.

#### Scenario: Segunda ejecución
- **WHEN** el procedimiento se ejecuta por segunda vez
- **THEN** los valores finales son los mismos y no se reporta ningún error
