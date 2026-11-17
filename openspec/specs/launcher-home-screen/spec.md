# launcher-home-screen Specification

## Purpose
Define el comportamiento visible de la pantalla de inicio del launcher: cómo se compone
la lista de apps, cómo se comporta el foco con el mando, qué papel juega la tipografía y
cómo se presenta el fondo y las apps destacadas.

## Requirements

### Requirement: Lista vertical de aplicaciones
La pantalla de inicio SHALL presentar las aplicaciones en una única columna vertical,
en el borde izquierdo de la pantalla, con el nombre de cada app bajo su icono.

#### Scenario: Inicio con apps instaladas
- **WHEN** el usuario abre la pantalla de inicio y hay apps disponibles
- **THEN** aparece una columna vertical con esas apps, de arriba abajo

#### Scenario: Lista más larga que la pantalla
- **WHEN** hay más apps de las que caben en la altura de la pantalla
- **THEN** la lista se desplaza para mantener siempre visible el elemento enfocado

### Requirement: Elemento enfocado escalado
El elemento que tiene el foco SHALL aparecer agrandado respecto al resto y con un
tratamiento visual destacado. El foco SHALL moverse únicamente con las flechas
arriba y abajo del mando o con el scroll del ratón.

#### Scenario: El usuario baja por la lista
- **WHEN** el usuario pulsa la flecha abajo con una app enfocada
- **THEN** el foco pasa a la siguiente app y la anterior vuelve a su tamaño normal

#### Scenario: El usuario sube por la lista
- **WHEN** el usuario pulsa la flecha arriba con el foco en la primera app
- **THEN** el foco no se mueve y no se sale de la lista

### Requirement: Tipografía como elemento principal
El nombre de cada app SHALL mostrarse con un tamaño de fuente claramente superior al
del icono y con un peso que contraste sobre el fondo. La lista SHALL ser legible a
distancia de sofá.

#### Scenario: Lectura a distancia
- **WHEN** la pantalla de inicio está visible
- **THEN** el nombre de la app enfocada se lee sin esfuerzo a distancia de 3 metros

### Requirement: Apps destacadas en la parte superior
Las apps que el usuario marque como destacadas SHALL aparecer agrupadas en la parte
superior de la lista, encima del resto de aplicaciones. Cuando haya apps marcadas y
también sin marcar, SHALL haber un encabezado que las separe, para que ninguna app
parezca destacada por error.

#### Scenario: Hay apps destacadas marcadas
- **WHEN** el usuario ha marcado al menos una app como destacada
- **THEN** esas apps aparecen arriba en la lista, por encima de las demás

#### Scenario: El resto de las apps queda separado
- **WHEN** hay apps marcadas y apps sin marcar
- **THEN** las apps sin marcar aparecen bajo un encabezado propio, separado del grupo
  de las destacadas

#### Scenario: No hay ninguna app destacada
- **WHEN** el usuario no ha marcado ninguna app como destacada
- **THEN** la lista muestra solo las apps normales, sin grupo vacío arriba

### Requirement: Entrada de Configuración al final
La entrada **Configuración** SHALL aparecer siempre como último elemento de la lista,
después de todas las aplicaciones.

#### Scenario: Posición de la entrada
- **WHEN** el usuario recorre la lista hasta el final
- **THEN** el último elemento es Configuración

### Requirement: Fondo de montaña
La pantalla de inicio SHALL usar una imagen de fondo propia del proyecto (montañas con
niebla y un resplandor cálido en el horizonte) y SHALL mantener el texto legible
sobre ella.

#### Scenario: Fondo por defecto
- **WHEN** el usuario instala el launcher por primera vez
- **THEN** la pantalla de inicio muestra el fondo de montaña del proyecto

#### Scenario: Legibilidad del texto
- **WHEN** el texto de la lista se superpone a la parte más clara del fondo
- **THEN** el texto sigue siendo legible por contraste

### Requirement: Arranque rápido
La pantalla de inicio SHALL ser visible en menos de un segundo tras pulsar el botón de
inicio, y SHALL NOT consume red.

#### Scenario: Arranque tras estar en background
- **WHEN** el usuario vuelve a la pantalla de inicio tras pasar 30 segundos en otra app
- **THEN** la pantalla de inicio aparece en menos de un segundo

#### Scenario: Funcionamiento sin red
- **WHEN** el dispositivo no tiene conexión a internet
- **THEN** la pantalla de inicio carga igual y muestra todas las apps
