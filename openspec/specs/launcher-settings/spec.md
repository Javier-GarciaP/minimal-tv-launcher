# launcher-settings Specification

## Purpose
Define la pantalla de ajustes del launcher: cómo se eligen las apps destacadas, cómo se
cambia o restablece el fondo, cómo se ajusta el tamaño de la tipografía y cómo se
conservan esas preferencias entre reinicios.

## Requirements

### Requirement: Selección de apps destacadas
La pantalla de ajustes SHALL permitir marcar y desmarcar apps como destacadas. La lista
de apps candidatas SHALL estar ordenada alfabéticamente y SHALL mostrar el estado de
cada una.

#### Scenario: Marcar una app
- **WHEN** el usuario marca una app como destacada
- **THEN** la app queda marcada y pasa a la parte superior de la pantalla de inicio

#### Scenario: Desmarcar una app
- **WHEN** el usuario desmarca una app que era destacada
- **THEN** la app vuelve a su posición normal en la lista

### Requirement: Filtro de apps destacadas
Los ajustes SHALL permitir mostrar solo las apps destacadas en la pantalla de inicio,
ocultando el resto. El filtro SHALL estar desactivado por defecto y SHALL aplicarse de
inmediato.

#### Scenario: Activar el filtro
- **WHEN** el usuario activa "Mostrar solo las apps destacadas"
- **THEN** la pantalla de inicio muestra únicamente las apps marcadas y la entrada de
  configuración

#### Scenario: Desactivar el filtro
- **WHEN** el usuario desactiva el filtro
- **THEN** la pantalla de inicio vuelve a mostrar el resto de las apps

#### Scenario: El contador de apps marcadas está al día
- **WHEN** el usuario vuelve de la pantalla de apps destacadas a los ajustes
- **THEN** el número de apps marcadas que muestran los ajustes es el actual

### Requirement: Elección y restablecimiento del fondo
Los ajustes SHALL permitir elegir entre el fondo de montaña incluido y un fondo sólido
oscuro, y SHALL permitir restablecer el fondo de montaña original.

#### Scenario: Cambio a fondo sólido
- **WHEN** el usuario elige el fondo sólido
- **THEN** la pantalla de inicio deja de mostrar la imagen de montañas

#### Scenario: Restablecer el fondo original
- **WHEN** el usuario restablece el fondo tras haberlo cambiado
- **THEN** la pantalla de inicio vuelve a mostrar la imagen de montañas original

### Requirement: Escala de tipografía
Los ajustes SHALL permitir ajustar el tamaño de la tipografía de la lista en al menos
tres niveles (pequeño, medio, grande). El cambio SHALL aplicarse de inmediato.

#### Scenario: Aumento del tamaño de fuente
- **WHEN** el usuario sube un nivel el tamaño de fuente
- **THEN** los nombres de las apps se muestran más grandes de inmediato

### Requirement: Persistencia de preferencias
Las preferencias de apps destacadas, fondo y tipografía SHALL conservarse tras reiniciar
el dispositivo y SHALL NOT depender de permisos de almacenamiento.

#### Scenario: Reinicio del dispositivo
- **WHEN** el dispositivo se reinicia
- **THEN** las apps destacadas, el fondo y el nivel de tipografía siguen igual

#### Scenario: Comprobación de almacenamiento
- **WHEN** se inspeccionan los permisos de la app
- **THEN** la app no solicita permiso de almacenamiento externo

### Requirement: Acceso desde la pantalla de inicio
Los ajustes SHALL ser alcanzables desde la entrada **Configuración** del final de la
lista de la pantalla de inicio.

#### Scenario: Abrir los ajustes desde la lista
- **WHEN** el usuario enfoca Configuración y la activa
- **THEN** se abre la pantalla de ajustes del launcher
