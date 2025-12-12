# Changelog

Todos los cambios notables en este proyecto serán documentados en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.4.1][0.4.1] - 2025-12-12

### Añadido

- **Sistema de manejo de input de ratón (mouse)**
  - Nueva clase `MouseEventHandler` (singleton pattern)
  - Gestiona el estado de todos los botones del ratón
  - Array booleano `mouseButtonPressed[]` para tracking eficiente
  - Integración con callbacks de GLFW
- **Nueva clase `MouseButtonCallback`**
  - Extiende `GLFWMouseButtonCallback` de LWJGL
  - Procesa eventos de botones del ratón de GLFW
  - Actualiza automáticamente el estado en `MouseEventHandler`
- **API de consulta de botones del ratón**
  - `isMouseButtonPressed(int buttonCode)` - Verifica si un botón está presionado
  - `setMouseButtonPressed(int buttonCode, boolean status)` - Actualiza estado del botón
  - `getGlfwMouseButtonCallback()` - Obtiene el callback de GLFW
  - `close()` - Libera recursos del callback
- **Nuevo callback `inputCallback` en `GameTiming`**
  - Tipo `Runnable` para procesamiento de input
  - Separación clara: input → update → render
  - Llamado antes del loop de updates
  - Fallback automático a `Window.get().input()` si no se configura
- **Método `Window.input()`**
  - Procesa input de teclado y ratón
  - Detección de ESC para cerrar ventana
  - Ejemplo de detección de clic izquierdo con mensaje en consola
- **Validación robusta de botones del ratón**
  - Verificación de límites superior e inferior (>= 0 y < GLFW_MOUSE_BUTTON_LAST)
  - Previene ArrayIndexOutOfBoundsException
  - Retorna false para códigos de botón inválidos

### Cambiado

- **`Window.init()` actualizado**
  - Registra `MouseButtonCallback` en GLFW con `glfwSetMouseButtonCallback()`
  - Integración del sistema de input de ratón en la inicialización
- **`Window.close()` mejorado**
  - Llama a `MouseEventHandler.get().close()` para liberar recursos
  - Gestión completa de cleanup para teclado y ratón
- **`GameTiming.inputCallback()` usa `Runnable`**
  - Antes: `Consumer<Void>` (menos idiomático)
  - Ahora: `Runnable` (más estándar en Java)
  - Mejora semántica: callback sin parámetros ni retorno
- **`GameTiming.input()` privado**
  - Llama a `inputCallback.run()` si está configurado
  - Fallback a `Window.get().input()` si no hay callback
- **`Application.init()` configura `inputCallback`**
  - `GameTiming.get().inputCallback(() -> Window.get().input())`
  - Sintaxis más limpia con `Runnable`
- **Funcionalidad de ESC movida a `Window.input()`**
  - Antes: En `Application.run()` con campo `running`
  - Ahora: En `Window.input()` con `glfwSetWindowShouldClose()`
  - Mejor encapsulación del manejo de input
- Versión actualizada de 0.4.0 a 0.4.1

### Mejorado

- **Arquitectura del game loop**
  - Separación clara de responsabilidades: input → update → render
  - `inputCallback` se ejecuta una vez por frame antes de updates
  - Mejor organización del flujo de ejecución
- **Consistencia entre handlers**
  - `MouseEventHandler` sigue el mismo patrón que `KeyboardEventHandler`
  - Validaciones idénticas en ambos sistemas
  - APIs uniformes y predecibles

### Notas Técnicas

- **Filosofía de la v0.4.1**: Input básico de ratón simple y consistente
  - Sistema singleton para acceso global al estado del ratón
  - Patrón idéntico al de teclado para facilitar aprendizaje
  - API minimalista enfocada en botones del ratón
  - Base para futuras extensiones (posición, scroll)
- **Patrón de diseño**:
  - `MouseEventHandler` → Singleton que gestiona estado de botones
  - `MouseButtonCallback` → Puente entre GLFW y el handler
  - `GameTiming.inputCallback` → Hook para procesamiento de input
  - `Window.input()` → Lógica centralizada de input
- **Decisiones de implementación**:
  - Array booleano para máxima eficiencia (O(1))
  - Validación completa de buttonCode (límites superior e inferior)
  - Callback registrado en Window (consistencia con teclado)
  - `Runnable` en vez de `Consumer<Void>` (más idiomático)
  - Liberación explícita de recursos en close()
- **Limitaciones conocidas (a implementar en futuras versiones)**:
  - No tracking de posición del cursor (mouseX, mouseY)
  - No tracking de movimiento (deltaX, deltaY)
  - No soporte para scroll wheel
  - No modo captura de cursor (para cámaras FPS)
  - No resetea estado al perder foco de ventana
  - No constantes para botones comunes

### Ejemplo de Uso

**Verificar si un botón del ratón está presionado**:
```java
import org.lwjgl.glfw.GLFW;
import es.noa.rad.game.engine.event.MouseEventHandler;

// En Window.input() o método de input personalizado
if (MouseEventHandler.get().isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
    System.out.printf("Mouse Left Button Pressed.%n");
    // Disparar arma, seleccionar objeto, etc.
}

if (MouseEventHandler.get().isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
    System.out.printf("Mouse Right Button Pressed.%n");
    // Apuntar, menú contextual, etc.
}

if (MouseEventHandler.get().isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_MIDDLE)) {
    System.out.printf("Mouse Middle Button Pressed.%n");
}
```

**Implementación en Window.input()**:
```java
public void input() {
    // Cerrar con ESC
    if (KeyboardEventHandler.get().isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
        GLFW.glfwSetWindowShouldClose(this.glfwWindow, true);
    }
    
    // Detectar clic izquierdo
    if (MouseEventHandler.get().isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
        System.out.printf("Mouse Button Left Pressed.%n");
    }
}
```

**Configuración de inputCallback en Application.init()**:
```java
// Configurar callback de input
GameTiming.get().inputCallback(() -> Window.get().input());
```

**Integración en Window.init()**:
```java
// Registrar callback de botones del ratón
GLFW.glfwSetMouseButtonCallback(
    this.glfwWindow,
    MouseEventHandler.get().getGlfwMouseButtonCallback()
);
```

**Liberación de recursos en Window.close()**:
```java
// Liberar callbacks
KeyboardEventHandler.get().close();
MouseEventHandler.get().close();

// Destruir ventana
GLFW.glfwDestroyWindow(this.glfwWindow);
GLFW.glfwTerminate();
```

**Flujo del sistema de input en el game loop**:
```
┌─────────────────────────────────────────────┐
│  GameTiming.tick()                          │
│  ┌───────────────────────────────────────┐  │
│  │ 1. input()        ← inputCallback     │  │
│  │    └─> Window.input()                 │  │
│  │         ├─> Keyboard checks            │  │
│  │         └─> Mouse checks               │  │
│  │                                        │  │
│  │ 2. while (deltaTime >= 1.0)           │  │
│  │      update(fixedDeltaTime)           │  │
│  │                                        │  │
│  │ 3. render(alpha)                      │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

**Arquitectura de eventos del ratón**:
```
┌──────────────────────────────────────────────┐
│  Usuario presiona botón del ratón           │
└────────────────┬─────────────────────────────┘
                 ↓
┌──────────────────────────────────────────────┐
│  GLFW Window                                 │
│  glfwSetMouseButtonCallback()                │
└────────────────┬─────────────────────────────┘
                 ↓
┌──────────────────────────────────────────────┐
│  MouseButtonCallback.invoke()                │
│  ┌────────────────────────────────────────┐  │
│  │ MouseEventHandler.setMouseButtonPressed│  │
│  │   (_button, _action != GLFW_RELEASE)   │  │
│  └────────────────────────────────────────┘  │
└────────────────┬─────────────────────────────┘
                 ↓
┌──────────────────────────────────────────────┐
│  MouseEventHandler (Singleton)               │
│  ┌────────────────────────────────────────┐  │
│  │ boolean[] mouseButtonPressed           │  │
│  │ mouseButtonPressed[button] = status    │  │
│  └────────────────────────────────────────┘  │
└────────────────┬─────────────────────────────┘
                 ↓
┌──────────────────────────────────────────────┐
│  Window.input() / Game Logic                 │
│  ┌────────────────────────────────────────┐  │
│  │ if (isMouseButtonPressed(LEFT))        │  │
│  │   handleClick()                        │  │
│  └────────────────────────────────────────┘  │
└──────────────────────────────────────────────┘
```

## [0.4.0][0.4.0] - 2025-12-11

### Añadido

- **Sistema de manejo de input de teclado**
  - Nueva clase `KeyboardEventHandler` (singleton pattern)
  - Gestiona el estado de todas las teclas del teclado
  - Array booleano `keyPressed[]` para tracking eficiente
  - Integración con callbacks de GLFW
- **Nueva clase `KeyCallback`**
  - Extiende `GLFWKeyCallback` de LWJGL
  - Procesa eventos de teclado de GLFW
  - Actualiza automáticamente el estado en `KeyboardEventHandler`
  - Filtra teclas desconocidas (`GLFW_KEY_UNKNOWN`)
- **Paquete `es.noa.rad.game.engine.event`**
  - Estructura para sistema de eventos
  - Subpaquete `callback` para callbacks específicos
  - Preparado para futuros eventos (mouse, gamepad, etc.)
- **API de consulta de teclado**
  - `isKeyPressed(int keyCode)` - Verifica si una tecla está presionada
  - `setKeyPressed(int keyCode, boolean status)` - Actualiza estado de tecla
  - `getGlfwKeyCallback()` - Obtiene el callback de GLFW
  - `close()` - Libera recursos del callback
- **Funcionalidad ESC para salir**
  - Tecla ESCAPE cierra la aplicación
  - Implementado en el game loop de `Application`
  - Control manual con campo `running` en `Application`
- **Validación de keyCodes**
  - Verificación de límites superior e inferior (>= 0 y < GLFW_KEY_LAST)
  - Previene ArrayIndexOutOfBoundsException
  - Retorna false para códigos inválidos

### Cambiado

- **`Window.init()` actualizado**
  - Registra `KeyCallback` en GLFW con `glfwSetKeyCallback()`
  - Integración del sistema de input en la inicialización de ventana
- **`Window.close()` mejorado**
  - Llama a `KeyboardEventHandler.get().close()` para liberar recursos
  - Gestión correcta de cleanup de callbacks
- **`Application` con control manual de ejecución**
  - Añadido campo `running` para control explícito del loop
  - Método `start()` ahora inicializa `running = true`
  - Método `stop()` actualizado para `running = false` y detener `GameTiming`
  - Game loop verifica `running`, `shouldClose()` y `tick()`
- **`Application.run()` con input polling**
  - Verifica tecla ESCAPE en cada frame
  - Establece `running = false` cuando se presiona ESC
  - Permite cerrar la aplicación con teclado
- Versión actualizada de 0.3.10 a 0.4.0

### Corregido

- **Typo en parámetro de `KeyCallback.invoke()`**
  - `_moddifier` → `_modifier` (ortografía correcta)
  - Mejora la claridad del código

### Notas Técnicas

- **Filosofía de la v0.4.0**: Input básico de teclado simple y funcional
  - Sistema singleton para acceso global al estado del teclado
  - Integración directa con GLFW sin capas adicionales
  - API minimalista pero efectiva
  - Base para futuras extensiones (mouse, gamepad)
- **Patrón de diseño**:
  - `KeyboardEventHandler` → Singleton que gestiona estado
  - `KeyCallback` → Puente entre GLFW y el handler
  - `Application` → Consumidor del sistema de input
- **Decisiones de implementación**:
  - Array booleano en lugar de HashMap para máxima eficiencia
  - Validación de keyCode para robustez
  - Callback registrado en Window (acoplamiento justificado)
  - Liberación explícita de recursos en close()
- **Limitaciones conocidas (a mejorar en futuras versiones)**:
  - No distingue entre "tecla presionada" vs "tecla mantenida"
  - No soporta eventos de modificadores (Shift, Ctrl, Alt) separadamente
  - No resetea estado al perder foco de ventana
  - No hay constantes para teclas comunes

### Ejemplo de Uso

**Verificar si una tecla está presionada**:
```java
import org.lwjgl.glfw.GLFW;
import es.noa.rad.game.engine.event.KeyboardEventHandler;

// En el game loop o método update
if (KeyboardEventHandler.get().isKeyPressed(GLFW.GLFW_KEY_W)) {
    // Mover hacia adelante
    player.moveForward();
}

if (KeyboardEventHandler.get().isKeyPressed(GLFW.GLFW_KEY_SPACE)) {
    // Saltar
    player.jump();
}
```

**Implementación en Application.run()**:
```java
while ((this.running)
    && (!Window.get().shouldClose())
    && (GameTiming.get().tick())) {

    // Cerrar con ESC
    if (KeyboardEventHandler.get().isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
        this.running = false;
    }

    Window.get().swapBuffers();
}
```

**Integración en Window.init()**:
```java
// Registrar callback de teclado
GLFW.glfwSetKeyCallback(
    this.glfwWindow,
    KeyboardEventHandler.get().getGlfwKeyCallback()
);
```

**Liberación de recursos en Window.close()**:
```java
// Liberar callback de teclado
KeyboardEventHandler.get().close();

// Destruir ventana
GLFW.glfwDestroyWindow(this.glfwWindow);
GLFW.glfwTerminate();
```

**Arquitectura del sistema**:
```
┌─────────────────────────────────────────────┐
│  GLFW Window                                │
│  ┌───────────────────────────────────────┐  │
│  │ Usuario presiona tecla                │  │
│  └───────────┬───────────────────────────┘  │
└──────────────┼──────────────────────────────┘
               ↓
┌─────────────────────────────────────────────┐
│  glfwSetKeyCallback()                       │
└──────────────┬──────────────────────────────┘
               ↓
┌─────────────────────────────────────────────┐
│  KeyCallback.invoke()                       │
│  ┌───────────────────────────────────────┐  │
│  │ if (keyCode != GLFW_KEY_UNKNOWN)      │  │
│  │   KeyboardEventHandler.setKeyPressed()│  │
│  └───────────────────────────────────────┘  │
└──────────────┬──────────────────────────────┘
               ↓
┌─────────────────────────────────────────────┐
│  KeyboardEventHandler (Singleton)           │
│  ┌───────────────────────────────────────┐  │
│  │ boolean[] keyPressed                  │  │
│  │ keyPressed[keyCode] = (action != REL) │  │
│  └───────────────────────────────────────┘  │
└──────────────┬──────────────────────────────┘
               ↓
┌─────────────────────────────────────────────┐
│  Application / Game Logic                   │
│  ┌───────────────────────────────────────┐  │
│  │ if (isKeyPressed(GLFW_KEY_W))         │  │
│  │   player.move()                       │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

## [0.3.10][0.3.10] - 2025-12-11

### Añadido

- **Sistema de configuración de tres niveles** en `GameSettings` y `WindowSettings`
  - Valores por defecto integrados en las enumeraciones
  - Fallback automático en tres niveles:
    1. Valor cargado del archivo properties (si existe y es válido)
    2. Valor pasado como parámetro al método `get(_defaultValue)`
    3. Valor por defecto definido en la enumeración (siempre disponible)
- **Valores por defecto en `GameSettings`**:
  - `GAME_VERTICAL_SYNCHRONIZATION`: `true`
  - `GAME_UPDATES_PER_SECOND`: `60.0D`
  - `GAME_MAXIMUM_UPDATES_PER_FRAME`: `5`
  - `GAME_MAXIMUM_ACCUMULATED_TIME`: `0.5F`
- **Valores por defecto en `WindowSettings`**:
  - `WINDOW_WIDTH`: `1280`
  - `WINDOW_HEIGHT`: `720`
  - `WINDOW_TITLE`: `"3D Game Engine"`

### Cambiado

- **`GameSettings` y `WindowSettings` refactorizados**
  - Constructor ahora requiere tres parámetros: property, classType, defaultValue
  - Método `get()` usa automáticamente el valor por defecto de la enumeración
  - Método `get(T _defaultValue)` implementa lógica de tres niveles de fallback
  - Variable local `propertyValue` para determinar el default a usar
- **`GameTiming` simplificado**
  - Eliminadas constantes públicas: `FRAMERATE`, `MAXIMUM_UPDATES_PER_FRAME`, `MAXIMUM_ACCUMULATED_TIME`
  - Método `init()` llama a `GameSettings.get()` sin parámetros
  - Depende completamente del sistema de configuración
- **`Application` simplificado**
  - Eliminadas constantes `WIDTH` y `HEIGHT`
  - Llamadas a `WindowSettings.get()` sin parámetros
  - Usa valores por defecto de las enumeraciones
- Versión actualizada de 0.3.9 a 0.3.10

### Eliminado

- **Constantes duplicadas en `GameTiming`**
  - `FRAMERATE` (ahora en `GameSettings.GAME_UPDATES_PER_SECOND`)
  - `MAXIMUM_UPDATES_PER_FRAME` (ahora en `GameSettings.GAME_MAXIMUM_UPDATES_PER_FRAME`)
  - `MAXIMUM_ACCUMULATED_TIME` (ahora en `GameSettings.GAME_MAXIMUM_ACCUMULATED_TIME`)
- **Constantes duplicadas en `Application`**
  - `WIDTH` (ahora en `WindowSettings.WINDOW_WIDTH`)
  - `HEIGHT` (ahora en `WindowSettings.WINDOW_HEIGHT`)

### Mejorado

- **Reducción de duplicación de código**
  - Un solo lugar para definir valores por defecto (enumeraciones)
  - No más constantes dispersas en diferentes clases
  - Mejor adherencia al principio DRY (Don't Repeat Yourself)
- **Configuración más robusta**
  - Sistema de fallback garantiza que siempre hay un valor válido
  - Flexibilidad para override en tres niveles diferentes
  - Más fácil testear con valores personalizados
- **Mantenibilidad**
  - Cambiar valores por defecto solo requiere modificar las enumeraciones
  - Menos lugares donde buscar configuraciones
  - Código más limpio y centralizado

### Notas Técnicas

- **Filosofía de la v0.3.10**: Configuración centralizada con fallback robusto
  - Las enumeraciones son la fuente única de verdad para defaults
  - Sistema de tres niveles proporciona máxima flexibilidad
  - Eliminación total de constantes duplicadas
- **Orden de prioridad del sistema de fallback**:
  1. **Properties file**: Si existe y es válido, se usa este valor
  2. **Parámetro**: Si no hay en properties y se pasa parámetro no-null, se usa
  3. **Enum default**: Si no hay en properties ni parámetro, se usa el default de la enum
- **Ventajas del diseño**:
  - Configuración: Valores centralizados en un solo lugar
  - Flexibilidad: Override posible en código si es necesario
  - Robustez: Siempre hay un valor válido disponible
  - Testing: Fácil inyectar valores personalizados
  - Mantenibilidad: Un solo lugar para actualizar defaults

### Ejemplo de Uso

**Antes (v0.3.9) - Constantes dispersas**:
```java
// GameTiming.java
public static final double FRAMERATE = 60.0D;
public static final int MAXIMUM_UPDATES_PER_FRAME = 5;
public static final float MAXIMUM_ACCUMULATED_TIME = 0.5F;

// Application.java
public static final int WIDTH = 1280;
public static final int HEIGHT = 720;

// Uso con defaults hardcoded
final double ups = GameSettings.GAME_UPDATES_PER_SECOND
  .get(GameTiming.FRAMERATE);
Window.get().init(
  WindowSettings.WINDOW_WIDTH.get(Application.WIDTH),
  WindowSettings.WINDOW_HEIGHT.get(Application.HEIGHT),
  WindowSettings.WINDOW_TITLE.get()
);
```

**Después (v0.3.10) - Configuración centralizada**:
```java
// GameSettings.java - Un solo lugar para defaults
GAME_UPDATES_PER_SECOND("game.updates.per.second", Double.class, 60.0D),
GAME_MAXIMUM_UPDATES_PER_FRAME("game.maximum.updates.per.frame", Integer.class, 5),
GAME_MAXIMUM_ACCUMULATED_TIME("game.maximum.accumulated.time", Float.class, 0.5F)

// WindowSettings.java - Un solo lugar para defaults
WINDOW_WIDTH("window.width", Integer.class, 1280),
WINDOW_HEIGHT("window.height", Integer.class, 720),
WINDOW_TITLE("window.title", String.class, "3D Game Engine")

// Uso sin constantes duplicadas
final double ups = GameSettings.GAME_UPDATES_PER_SECOND.get();
Window.get().init(
  WindowSettings.WINDOW_WIDTH.get(),
  WindowSettings.WINDOW_HEIGHT.get(),
  WindowSettings.WINDOW_TITLE.get()
);
```

**Sistema de tres niveles en acción**:
```java
// Nivel 1: Usa valor de properties (si existe)
// Nivel 2: Usa valor pasado (si no hay en properties)
// Nivel 3: Usa default de enum (si no hay ni properties ni parámetro)

// Sin parámetro - usa properties o enum default (60.0)
double ups = GameSettings.GAME_UPDATES_PER_SECOND.get();

// Con parámetro - usa properties, o parámetro (30.0), o enum default
double ups = GameSettings.GAME_UPDATES_PER_SECOND.get(30.0D);

// Con null - usa properties o enum default (ignora null)
double ups = GameSettings.GAME_UPDATES_PER_SECOND.get(null);
```

**Implementación del método get()**:
```java
public <T> T get(final T _defaultValue) {
  /* Establish which default value to use. */
  T propertyValue = (T) this.defaultValue;  // Nivel 3: Enum default
  if (_defaultValue != null) {
    propertyValue = _defaultValue;           // Nivel 2: Parámetro
  }
  
  return (T) Configuration.get()
    .property(
      this.property,
      (Class<T>) this.classType,
      propertyValue                          // Nivel 1: Properties (inside)
    );
}
```

## [0.3.9][0.3.9] - 2025-12-11

### Añadido

- **Nueva clase `GameTiming`** en `es.noa.rad.game.engine.core`
  - Extrae toda la lógica del game loop de `Application`
  - Singleton pattern con lazy initialization thread-safe
  - Gestiona timing, spiral of death protection, y métricas UPS/FPS
  - Centraliza configuración de fixed timestep y VSync
- **Sistema de callbacks opcional**
  - `updateCallback(Consumer<Float>)` - Callback para updates
  - `renderCallback(Consumer<Float>)` - Callback para renders
  - Fallback automático a `Window.get()` si no se configuran
  - Permite desacoplamiento y testing sin dependencias
- **Método `tick()`** público
  - Procesa un frame completo del game loop
  - Retorna `boolean` indicando si debe continuar
  - Documentación Javadoc completa (11 líneas)
  - Reemplaza lógica interna de `Application.run()`
- **Métodos de ciclo de vida públicos**
  - `init()` - Inicializa configuración y timing
  - `start()` - Activa el game loop
  - `stop()` - Detiene el game loop
  - `get()` - Obtiene instancia singleton
- **Constantes públicas en `GameTiming`**
  - `FRAMERATE` (60.0D)
  - `MAXIMUM_UPDATES_PER_FRAME` (5)
  - `MAXIMUM_ACCUMULATED_TIME` (0.5F)
- **Inicialización consistente de métricas**
  - Métricas se inicializan correctamente en constructor
  - Usa métodos `resetUps()`, `resetUpsTime()`, etc.
  - Evita duplicación de código (DRY)

### Cambiado

- **`Application` significativamente simplificado**
  - De 369 líneas a ~99 líneas (-73% de código)
  - Eliminado campo `running` (delegado a `GameTiming`)
  - Eliminado campo `deltaTime` (movido a `GameTiming`)
  - Eliminados campos `previousTime`, `totalSkippedUpdates` (movidos)
  - Eliminados campos UPS/FPS (movidos a `GameTiming`)
  - Eliminadas constantes `FRAMERATE`, `MAXIMUM_UPDATES_PER_FRAME`, etc.
- **`Application.run()` ultra simplificado**
  - Antes: ~140 líneas con toda la lógica del loop
  - Después: ~15 líneas, solo loop básico
  - Loop: `while (!Window.shouldClose() && GameTiming.tick())`
  - Solo responsable de `swapBuffers()`
- **Separación de responsabilidades (SRP)**
  - `Application`: Ciclo de vida de la aplicación
  - `GameTiming`: Lógica del game loop y timing
  - `Window`: Gestión de ventana y rendering
- **Configuración de callbacks en `Application.init()`**
  - `GameTiming.get().updateCallback(dt -> Window.get().update(dt))`
  - `GameTiming.get().renderCallback(dt -> Window.get().render(dt))`
  - Conecta `GameTiming` con `Window` de forma desacoplada
- **Método `start()` en `Application` simplificado**
  - Ahora vacío (lógica delegada a `GameTiming.start()`)
  - Podría eliminarse en versiones futuras
- Versión actualizada de 0.3.8 a 0.3.9

### Mejorado

- **Arquitectura más limpia**
  - Separación clara de responsabilidades
  - Cada clase tiene un propósito único y definido
  - Menor acoplamiento entre componentes
- **Testabilidad**
  - `GameTiming` puede testearse sin `Window` real
  - Callbacks permiten mocks simples
  - No requiere GLFW/OpenGL para tests unitarios
- **Reutilización**
  - `GameTiming` puede usarse en diferentes contextos
  - Headless servers (sin rendering)
  - Herramientas (sin ventana)
  - Múltiples engines gráficos
- **Mantenibilidad**
  - Código del game loop en un solo lugar
  - Cambios en timing no afectan `Application`
  - Más fácil de entender y modificar
- **Documentación**
  - Método `tick()` completamente documentado
  - Comentarios descriptivos mantenidos
  - Comentario "Initialize metrics" en constructor

### Refactorizado

- **Extracción de clase `GameTiming`**
  - Antes: Todo en `Application`
  - Después: Lógica separada en clase dedicada
  - Patrón: Extract Class refactoring
- **Constructor de `GameTiming`**
  - Inicializa estado base (running, deltaTime, previousTime)
  - Llama a métodos reset para métricas (DRY)
  - Estado consistente desde creación
- **Singleton pattern**
  - Método `createInstance()` sincronizado
  - Lazy initialization thread-safe
  - Acceso global vía `get()`

### Notas Técnicas

- **Filosofía de la v0.3.9**: Separación de responsabilidades + Flexibilidad
  - Fixed Timestep: Física determinística (60 UPS)
  - VSync: Rendering suave sin tearing
  - Spiral of Death Protection: Robustez máxima
  - Callbacks: Desacoplamiento opcional
  - Singleton: Acceso global consistente
- **Patrón arquitectónico**:
  - `Application` → Orchestrator (coordina componentes)
  - `GameTiming` → Game Loop Engine (timing y updates)
  - `Window` → Rendering Subsystem (gráficos)
  - Callbacks → Dependency Injection (desacoplamiento)
- **Ventajas del diseño**:
  - Testing: GameTiming se puede testear aisladamente
  - Flexibilidad: Callbacks permiten diferentes implementaciones
  - Simplicidad: Application más fácil de entender
  - Extensibilidad: Añadir features en GameTiming sin tocar Application
- **Decisiones de diseño**:
  - Callbacks con fallback: Funciona sin configuración + permite override
  - `tick()` en lugar de `playback()`: Nombre estándar de la industria
  - Métricas en constructor: Estado consistente desde creación
  - `start()` público: API consistente con `stop()`

### Ejemplo de Uso

**Antes (v0.3.8) - Todo en Application**:
```java
// Application.java - 369 líneas
private double deltaTime;
private long previousTime;
// ... muchos campos más

@Override
public void run() {
    this.init();
    
    final double updatesPerSecond = ...;
    final double updateTime = ...;
    // ... cálculos de timing
    
    while (running && !Window.shouldClose()) {
        // ... lógica compleja del game loop (140 líneas)
    }
}
```

**Después (v0.3.9) - Separado en GameTiming**:
```java
// Application.java - ~99 líneas
@Override
public void run() {
    this.init();
    
    while (!Window.shouldClose() && GameTiming.get().tick()) {
        Window.get().swapBuffers();
    }
    
    this.close();
    this.stop();
}

// GameTiming.java - 402 líneas
public boolean tick() {
    if (!this.running) return false;
    
    // ... toda la lógica del game loop
    
    return true;
}
```

**Configuración de callbacks (opcional)**:
```java
// En Application.init()
GameTiming.get().updateCallback(dt -> Window.get().update(dt));
GameTiming.get().renderCallback(dt -> Window.get().render(dt));
GameTiming.get().init();
```

**Testing sin Window (nuevo)**:
```java
@Test
public void testGameTiming() {
    GameTiming timing = GameTiming.get();
    
    List<Float> updates = new ArrayList<>();
    timing.updateCallback(dt -> updates.add(dt));
    timing.init();
    
    timing.tick();  // No requiere Window real
    
    assertEquals(0.0166f, updates.get(0), 0.001f);
}
```

## [0.3.8][0.3.8] - 2025-12-11

### Añadido

- **Nueva constante `MAXIMUM_ACCUMULATED_TIME`** (0.5F)
  - Límite máximo de acumulación de tiempo (500 ms)
  - Threshold para resetear `deltaTime` cuando está demasiado alto
  - Previene catch-up infinito en hardware extremadamente lento
- **Nuevo campo `totalSkippedUpdates`** (int)
  - Rastrea el total de updates omitidos durante toda la ejecución
  - Se incrementa cuando se resetea `deltaTime` o se alcanzan límites
  - Visible en estadísticas de UPS cada segundo
- **Nueva configuración `GAME_MAXIMUM_ACCUMULATED_TIME`** en `GameSettings`
  - Tipo: `Float.class`
  - Propiedad: `game.maximum.accumulated.time`
  - Valor por defecto: 0.5F (500 ms = 30 updates a 60 UPS)
- **Protección doble contra Spiral of Death**
  - Primera barrera: Reset de `deltaTime` si excede `maxDeltaTime`
  - Segunda barrera: Límite de `maxUpdatesPerFrame` (5 updates)
  - Sistema complementario para máxima robustez
- **Logging informativo mejorado**
  - Mensaje cuando `deltaTime` es reseteado (muestra valores actuales)
  - Mensaje cuando updates son omitidos (muestra límite de frame)
  - Estadísticas de UPS incluyen total de updates omitidos
- **Comentarios internos descriptivos**
  - Explicación de "update units" y acumulador
  - Documentación de protección contra spiral of death
  - Clarificación de catch-up loop y sus límites
  - Explicación de interpolation alpha para rendering
  - Separación de secciones lógicas en constructor e init()

### Cambiado

- **Extracción de variables de configuración** (optimización)
  - `updatesPerSecond` calculado una vez al inicio de `run()`
  - `maxAccumulatedTime` obtenido una vez
  - `maxUpdatesPerFrame` obtenido una vez
  - Reduce llamadas repetidas a `GameSettings` (de 10+ a 3)
- **Nueva variable `maxDeltaTime`** calculada
  - Valor: `maxAccumulatedTime * updatesPerSecond`
  - Calculado una vez, reutilizado múltiples veces
  - Elimina cálculo duplicado (antes repetido 6 veces)
- **Simplificación de expresiones**
  - `updateTime`: eliminados casts redundantes
  - `fixedDeltaTime`: reutiliza `updatesPerSecond`
  - `maxDeltaTime`: reutiliza variables extraídas
- **Mensajes de consola mejorados**
  - Antes: `"Game too far behind, resetting delta time."`
  - Después: `"Delta time too high (%.2f updates), resetting to %.2f (max %.2f seconds)."`
  - Antes: `"Skipped %d updates to prevent spiral of death."`
  - Después: `"Skipped %d updates (limit: %d per frame) to prevent spiral of death."`
- **Estadísticas de UPS ampliadas**
  - Antes: `"Updates Per Second (UPS): %d."`
  - Después: `"Updates Per Second (UPS): %d, Total Skipped Updates: %d."`
- **Comentarios del catch-up loop reorganizados**
  - Eliminada duplicación de "Run all accumulated updates"
  - Renombrado a "Catch-up loop" para mayor claridad
  - Comentario separado para "Discard remaining updates"
- Versión actualizada de 0.3.7 a 0.3.8

### Mejorado

- **Legibilidad del código**
  - Variables de configuración extraídas al inicio
  - Expresiones simplificadas sin casts redundantes
  - Código duplicado eliminado (DRY compliance)
  - Comentarios más descriptivos y educativos
- **Rendimiento**
  - Menos llamadas a `GameSettings.get()` por frame
  - Cálculo de `maxDeltaTime` una sola vez
  - Configuración cargada al inicio, no en cada iteración
- **Mantenibilidad**
  - Cambio de umbral solo en una ubicación (`maxDeltaTime`)
  - Variables con nombres descriptivos
  - Separación clara de responsabilidades
- **Debugging**
  - Logging con valores específicos (deltaTime, maxDeltaTime, límites)
  - Rastreo de updates omitidos totales
  - Información contextual en mensajes de protección

### Protegido

- **Doble protección contra Spiral of Death**
  - Nivel 1: Reset cuando `deltaTime > maxDeltaTime` (30 updates)
  - Nivel 2: Límite de 5 updates por frame
  - Garantiza que la aplicación siempre responde, incluso en hardware muy lento
- **Degradación elegante**
  - En lugar de congelarse, el sistema omite updates y continúa
  - El usuario ve la aplicación funcionando (aunque más lenta)
  - Logging permite diagnosticar problemas de rendimiento

### Notas Técnicas

- **Filosofía de la v0.3.8**: Protección completa con monitoreo
  - Fixed Timestep: Física determinística (60 UPS)
  - VSync: Rendering suave sin tearing
  - Spiral of Death Protection Doble: Robustez máxima
  - Telemetría: Monitoreo de updates omitidos
- **Comportamiento del sistema**:
  - Hardware normal: `deltaTime` nunca excede `maxDeltaTime`, 0 updates omitidos
  - Hardware lento: Reset ocasional de `deltaTime`, updates omitidos < 100
  - Hardware muy lento: Resets frecuentes, updates omitidos > 1000
  - Sistema congelado: `deltaTime` resetea constantemente a 30.0, updates omitidos aumentan rápidamente
- **Ventajas sobre v0.3.7**:
  - Mejor logging para diagnóstico
  - Variables extraídas mejoran legibilidad
  - Doble protección más robusta que protección simple
  - Telemetría permite análisis de rendimiento
  - Código más mantenible (DRY, comentarios descriptivos)

### Ejemplo de Comportamiento

**Hardware normal (60 FPS, 60 UPS)**:
```
Updates Per Second (UPS): 60, Total Skipped Updates: 0
Frames Per Second (FPS): 60
```

**Hardware lento con spike (lag momentáneo)**:
```
Delta time too high (45.23 updates), resetting to 30.00 (max 0.50 seconds).
Updates Per Second (UPS): 45, Total Skipped Updates: 15
Frames Per Second (FPS): 20
```

**Hardware muy lento (persistente)**:
```
Skipped 3 updates (limit: 5 per frame) to prevent spiral of death.
Updates Per Second (UPS): 30, Total Skipped Updates: 180
Frames Per Second (FPS): 10
```

## [0.3.7][0.3.7] - 2025-12-10

### Añadido

- **Fixed Timestep + VSync + Spiral of Death Protection (Implementación Completa)**
  - Combina Fixed Timestep (v0.3.4), VSync (v0.3.5), y protección contra spiral of death
  - Updates con timestep fijo para física determinística (60 UPS)
  - Renders sincronizados con VSync (monitor Hz)
  - Límite de 5 updates por frame para prevenir congelamiento
- **Campo `boolean running`** para control del game loop
  - Permite parada limpia del loop
  - Inicializado a `false` en constructor
- **Métodos `start()` y `stop()`**
  - Control explícito del estado del game loop
  - `start()` llamado después de `init()`
  - `stop()` llamado al finalizar `run()`
- **Constante `MAXIMUM_UPDATES_PER_FRAME`** en `Application`
  - Valor por defecto: `5` (int)
  - Protección contra spiral of death
- **Campo `double deltaTime`** como acumulador
  - Inicializado a `0D` en constructor
  - Acumula tiempo entre frames
- **Nueva propiedad de configuración**
  - `game.maximum.updates.per.frame` - Límite de updates (Integer)
  - Valor por defecto: 5 updates máximo por frame
- **Enum `GAME_MAXIMUM_UPDATES_PER_FRAME`** en `GameSettings`
  - Tipo `Integer.class`
  - Complementa `GAME_UPDATES_PER_SECOND` y `GAME_VERTICAL_SYNCHRONIZATION`
- **Contador `updateCount`** en game loop
  - Rastrea número de updates en frame actual
  - Reiniciado a 0 antes del loop de catch-up
- **Condición doble en loop de updates**
  - `while ((deltaTime >= 1.0D) && (updateCount < maxUpdatesPerFrame))`
  - Primera condición: tiempo acumulado para update
  - Segunda condición: límite de spiral of death

### Cambiado

- **Game loop ahora tiene triple protección**
  - Fixed Timestep: Updates determinísticos
  - VSync: Renders sin tearing
  - Spiral of Death Protection: Límite de catch-up
- **Condición de loop principal mejorada**
  - Antes: `while (!Window.get().shouldClose())`
  - Después: `while ((this.running) && (!Window.get().shouldClose()))`
  - Mayor control sobre el estado del loop
- **Ciclo de vida más explícito**
  - `init()` → `start()` → game loop → `close()` → `stop()`

### Protegido

- **Prevención de Spiral of Death**
  - Sistema lento → máximo 5 updates → render continúa → aplicación responde
  - Trade-off: Física puede "saltar" frames en hardware muy lento, pero app no se congela
  - Balance entre precisión física y fluidez visual

### Notas Técnicas

- **Implementación Completa**: Esta versión representa el sistema de timing más robusto
  - Física determinística (Fixed Timestep)
  - Sin screen tearing (VSync)
  - Protección contra congelamiento (Spiral of Death Protection)
- **UPS/FPS Desacoplados**:
  - UPS: Siempre 60 (configurable)
  - FPS: Según refresh rate del monitor (60Hz, 144Hz, etc.)
  - Ejemplo: 60 UPS con 144 FPS posible
- **Comportamiento del sistema**:
  - Hardware rápido (144 FPS): 0-1 updates por frame, límite nunca alcanzado
  - Hardware normal (60 FPS): 1 update por frame, funcionamiento óptimo
  - Hardware lento (30 FPS): 2 updates por frame, catch-up automático
  - Hardware muy lento (20 FPS): 5 updates máximo, protección activa
- **Ventajas sobre versiones previas**:
  - vs v0.3.4: Añade VSync (sin screen tearing)
  - vs v0.3.5: Añade Fixed Timestep (física determinística)
  - vs v0.3.6: Añade Spiral of Death Protection (robustez)
- **Cuándo usar**:
  - ✅ Juegos multijugador con física importante
  - ✅ Simuladores que requieren determinismo
  - ✅ Aplicaciones con VSync que necesitan UPS fijo
  - ✅ Proyectos que pueden ejecutarse en hardware variable

### Ejemplo de Comportamiento

```
Monitor 60 Hz, UPS=60:
- Frame 1: 1 update, render, alpha≈0.0
- Frame 2: 1 update, render, alpha≈0.0
Resultado: 60 UPS, 60 FPS (sincronizados)

Monitor 144 Hz, UPS=60:
- Frame 1: 0 updates, render, alpha≈0.4
- Frame 2: 0 updates, render, alpha≈0.8
- Frame 3: 1 update, render, alpha≈0.2
Resultado: 60 UPS, 144 FPS (ultra smooth)

Sistema con lag, UPS=60:
- Frame largo: 8 updates pendientes → ejecuta 5 → LÍMITE
- Siguiente frame: 3 updates pendientes → ejecuta 3 → normaliza
Resultado: Sistema responde siempre, física se recupera gradualmente
```

## [0.3.6][0.3.6] - 2025-12-10

### Añadido

- **Fixed Timestep para updates + VSync para renders**
  - Combinación de lo mejor de v0.3.4 (Fixed Timestep) y v0.3.5 (VSync)
  - Updates con timestep fijo para física determinística
  - Renders sincronizados con VSync para imagen suave sin tearing
- **Campo `double deltaTime`** como acumulador de tiempo
  - Inicializado a 0D en constructor
  - Acumula tiempo para ejecutar múltiples updates si es necesario
- **Constante `FRAMERATE`** restaurada en `Application`
  - Valor por defecto: `60.0D`
  - Usado como fallback en `GAME_UPDATES_PER_SECOND.get()`
- **Nueva propiedad de configuración**
  - `game.updates.per.second` - UPS objetivo (Double) en `application.properties`
  - Valor por defecto: 60.0 UPS
  - Permite configurar frecuencia de updates independiente del monitor
- **Enum `GAME_UPDATES_PER_SECOND`** añadido en `GameSettings`
  - Tipo `Double.class`
  - Coexiste con `GAME_VERTICAL_SYNCHRONIZATION`
- **Cálculo de `updateTime`** en `run()`
  - Tiempo que debe transcurrir entre cada update
  - Fórmula: `NANOSECONDS_IN_SECOND / updatesPerSecond`
- **Cálculo de `fixedDeltaTime`** en `run()`
  - Timestep constante para física determinística
  - Fórmula: `1.0 / updatesPerSecond`
  - Siempre el mismo valor (ej: 0.0166s para 60 UPS)

### Cambiado

- **Game loop ahora combina Fixed Timestep + VSync**
  - Acumulador de tiempo para updates
  - Loop `while (deltaTime >= 1.0D)` para catch-up
  - VSync controla el frame rate de renders
  - Lo mejor de ambos mundos
- **Delta time dual purpose**
  - Como acumulador: `double deltaTime` campo de instancia
  - Para updates: Siempre `fixedDeltaTime` constante
  - Para renders: `(float) deltaTime` como alpha de interpolación (0.0-1.0)
- **Updates ahora son determinísticos**
  - Antes (v0.3.5): Variable según frame time
  - Después (v0.3.6): Siempre con mismo timestep
  - Permite física predecible y reproducible
- **Renders siguen siendo variables**
  - Controlados por VSync del monitor
  - Típicamente 60, 75, 144 Hz según hardware
  - Sin screen tearing
- **UPS y FPS desacoplados nuevamente**
  - UPS: Fijo a valor configurado (60.0 por defecto)
  - FPS: Variable según refresh rate del monitor
  - Ejemplo: 60 UPS con 144 FPS posible
- Versión actualizada de 0.3.5 a 0.3.6

### Restaurado

- **Fixed Timestep pattern** de v0.3.4
  - Acumulador de tiempo
  - Loop de catch-up para updates
  - Timestep constante para física
- **Separación UPS/FPS** de v0.3.3 y v0.3.4
  - Updates a frecuencia fija configurable
  - Renders a frecuencia del monitor (VSync)

### Notas Técnicas

- **Híbrido Fixed Timestep + VSync**:
  - Updates: Software timing con timestep fijo
  - Renders: Hardware timing con VSync
  - Combina ventajas de ambos enfoques
- **Ventajas del enfoque híbrido**:
  - ✅ Física determinística (Fixed Timestep)
  - ✅ Sin screen tearing (VSync)
  - ✅ Timing eficiente (hardware para renders)
  - ✅ Catch-up automático (acumulador)
  - ✅ Interpolación ready (alpha 0.0-1.0)
  - ✅ Networking compatible (UPS fijo)
- **Comparación con versiones anteriores**:
  - vs v0.3.4: Añade VSync, elimina Thread.sleep y spiral of death protection
  - vs v0.3.5: Añade Fixed Timestep, separa UPS de FPS
  - Toma lo mejor de cada versión
- **Comportamiento del acumulador**:
  - Si sistema es rápido (144 FPS, 60 UPS): ~0-1 updates por frame
  - Si sistema es normal (60 FPS, 60 UPS): 1 update por frame
  - Si sistema es lento (30 FPS, 60 UPS): 2 updates por frame
  - VSync evita que FPS sea demasiado alto
  - Sin límite de updates (sin spiral of death protection)
- **Sin spiral of death protection**:
  - v0.3.4 tenía límite de 5 updates/frame
  - v0.3.6 no tiene límite (más simple)
  - Asume que VSync + hardware moderno evitan el problema
  - Trade-off: Simplicidad vs robustez extrema
- **Interpolación preparada pero no implementada**:
  - `render((float) deltaTime)` recibe alpha 0.0-1.0
  - Window puede usarlo para interpolar posiciones
  - Permite renderizado ultra suave
- **Uso típico**:
  - ✅ Juegos con física importante
  - ✅ Simuladores que requieren determinismo
  - ✅ Juegos que quieren 144+ FPS con física estable
  - ✅ Aplicaciones que necesitan replayability

### Ejemplo de Comportamiento

```
Monitor 60 Hz, UPS=60:
- Frame 1: 1 update, render, alpha=0.0
- Frame 2: 1 update, render, alpha=0.0
Resultado: 60 UPS, 60 FPS (sincronizados)

Monitor 144 Hz, UPS=60:
- Frame 1: 0 updates, render, alpha=0.4
- Frame 2: 0 updates, render, alpha=0.8
- Frame 3: 1 update, render, alpha=0.2
Resultado: 60 UPS, 144 FPS (ultra smooth con interpolación)

Monitor 60 Hz, sistema con lag, UPS=60:
- Frame 1: 2 updates, render, alpha=0.0
- Frame 2: 1 update, render, alpha=0.0
Resultado: 60 UPS mantenido, catch-up automático
```

## [0.3.5][0.3.5] - 2025-12-10

### Añadido

- **VSync (Vertical Synchronization) básico**
  - Método `enableVSync()` en `Window`
  - Llama a `GLFW.glfwSwapInterval(1)` para habilitar VSync
  - Sincronización automática con la frecuencia de refresco del monitor
- **Método `swapBuffers()` en `Window`**
  - Combina `glfwSwapBuffers()` y `glfwPollEvents()` en una sola llamada
  - Simplifica el game loop principal
  - VSync controla el timing automáticamente
- **Nueva propiedad de configuración**
  - `game.vertical.synchronization` - Habilitar/deshabilitar VSync (Boolean)
  - Valor por defecto: `true`
  - Permite desactivar VSync si se necesita
- **Enum `GAME_VERTICAL_SYNCHRONIZATION`** en `GameSettings`
  - Tipo `Boolean.class`
  - Método `.get(true)` con valor por defecto
- **Contexto OpenGL configurado**
  - Añadido `glfwMakeContextCurrent()` en `Window.init()`
  - Necesario para que VSync funcione correctamente

### Cambiado

- **Game loop simplificado dramáticamente**
  - Antes: Fixed Timestep con acumulador, catch-up loop, spiral of death protection, sleep dinámico
  - Después: Simple delta time con VSync
  - De ~50 líneas de lógica de timing a ~10 líneas
- **Delta time ahora es variable local**
  - Antes: `double deltaTime` campo de instancia (acumulador)
  - Después: `float deltaTime` variable local calculada cada frame
  - Simplificación: solo para movimientos frame-independent, no para física determinística
- **Update y render llamados una vez por frame**
  - Sin loop de catch-up
  - Sin múltiples updates por frame
  - UPS = FPS (acoplados nuevamente, pero controlados por VSync)
- **Sincronización delegada a hardware**
  - Antes: `Thread.sleep()` calculado por software
  - Después: VSync maneja timing por hardware
  - Más eficiente y preciso
- **Reorganización de responsabilidades**
  - `Window.update()`: Ya no llama a `glfwPollEvents()` (movido a `swapBuffers()`)
  - `Window.render()`: Ya no llama a `glfwSwapBuffers()` (movido a `swapBuffers()`)
  - Game loop: Llama a `swapBuffers()` al final de cada iteración
- **Comentarios mejorados para mayor claridad**
  - Game loop: Explica rol de VSync en control de frame rate
  - `swapBuffers()`: Detalla comportamiento con VSync habilitado
  - `enableVSync()`: Contexto sobre sincronización vertical
- **Eliminado cast redundante**: `(boolean)` en condición de VSync
- Versión actualizada de 0.3.4 a 0.3.5

### Optimizado

- **Eliminada duplicación de `glfwPollEvents()`**
  - Antes: Llamado en `Window.update()` Y en game loop
  - Después: Solo en `Window.swapBuffers()`
  - Previene procesamiento doble de eventos por frame
  - Mejora claridad: responsabilidad única por método

### Eliminado

- **Sistema Fixed Timestep completo**
  - Eliminado acumulador `deltaTime` como campo
  - Eliminado `updateTime` y `renderTime`
  - Eliminado `fixedDeltaTime`
  - Eliminado `maxUpdatesPerFrame`
  - Eliminado loop `while (deltaTime >= 1.0D)`
  - Eliminado contador `updateCount`
  - Eliminado spiral of death protection
- **Thread.sleep() y frame capping manual**
  - Eliminado cálculo de `sleepTime`
  - Eliminado `elapsedTime`
  - Eliminado bloque try-catch de `InterruptedException`
- **Propiedades de configuración obsoletas**
  - Eliminado `game.frames.per.second`
  - Eliminado `game.updates.per.second`
  - Eliminado `game.maximum.updates.per.frame`
- **Enums de GameSettings obsoletos**
  - Eliminado `GAME_FRAMES_PER_SECOND`
  - Eliminado `GAME_UPDATES_PER_SECOND`
  - Eliminado `GAME_MAXIMUM_UPDATES_PER_FRAME`
- **Constantes de Application obsoletas**
  - Eliminado `FRAMERATE`
  - Eliminado `MAXIMUM_UPDATES_PER_FRAME`

### Notas Técnicas

- **VSync (Vertical Synchronization)**:
  - Sincroniza el swap de buffers con el refresco vertical del monitor
  - Previene screen tearing (rasgado de pantalla)
  - FPS se limita automáticamente a la frecuencia del monitor (típicamente 60 Hz, 75 Hz, 144 Hz, etc.)
  - `glfwSwapInterval(1)`: sincroniza con cada refresco (60 FPS en monitor de 60 Hz)
  - `glfwSwapInterval(0)`: desactiva VSync (sin límite de FPS)
  - `glfwSwapInterval(2)`: sincroniza cada 2 refrescos (30 FPS en monitor de 60 Hz)
- **Ventajas de VSync**:
  - ✅ Timing preciso manejado por hardware (GPU + monitor)
  - ✅ Elimina screen tearing
  - ✅ Reduce consumo de CPU (no busy-wait)
  - ✅ Código más simple y legible
  - ✅ Más eficiente para LWJGL/OpenGL
  - ✅ Comportamiento consistente entre sistemas
- **Trade-offs vs Fixed Timestep**:
  - ⚠️ Física no es determinística (deltaTime variable)
  - ⚠️ UPS acoplado a FPS del monitor
  - ⚠️ Networking puede requerir interpolación adicional
  - ⚠️ No hay catch-up si un frame tarda mucho
  - ✅ Pero: Suficiente para la mayoría de juegos
  - ✅ Pero: Más simple de implementar y mantener
- **Cuándo usar VSync**:
  - ✅ Juegos single-player con física simple
  - ✅ Aplicaciones gráficas interactivas
  - ✅ Editores y herramientas
  - ✅ Cuando la simplicidad es prioritaria
  - ⚠️ Considerar Fixed Timestep para: juegos multijugador competitivos, simulaciones físicas complejas, replays determinísticos
- **Implementación educativa**:
  - Esta versión demuestra la forma más simple y común de game loop en OpenGL/LWJGL
  - Mayoría de tutoriales y juegos indie usan este enfoque
  - Balance perfecto entre simplicidad y funcionalidad
  - Base sólida antes de optimizaciones avanzadas

### Comparación con v0.3.4

| Aspecto | v0.3.4 (Fixed Timestep) | v0.3.5 (VSync) |
|---------|-------------------------|----------------|
| Complejidad | Alta (~50 líneas timing) | Baja (~10 líneas) |
| Física | Determinística | Frame-dependent |
| Timing | Software (Thread.sleep) | Hardware (VSync) |
| UPS/FPS | Independientes | Acoplados al monitor |
| Catch-up | Sí (hasta 5 updates) | No |
| Screen tearing | Posible | Prevenido |
| Networking | Ideal | Requiere trabajo extra |
| Uso típico | Simuladores, multiplayer | Single-player, tools |

## [0.3.4][0.3.4] - 2025-12-10

### Añadido

- **Protección contra Spiral of Death**
  - Campo `int updateCount = 0` antes del loop de updates
  - Contador reiniciado antes de cada ciclo de catch-up
  - Límite máximo de updates por frame
- **Constante `MAXIMUM_UPDATES_PER_FRAME`** en `Application`
  - Valor por defecto: `5` (int)
  - Usado como fallback en configuración
  - Proporciona valor seguro si falta la propiedad
- **Nueva propiedad de configuración**
  - `game.maximum.updates.per.frame` - Límite de updates (Integer) en `application.properties`
  - Valor configurado: 5 updates máximo por frame
  - Previene bucles infinitos en sistemas muy lentos
- **Enum `GAME_MAXIMUM_UPDATES_PER_FRAME`** en `GameSettings`
  - Tipo `Integer.class` para límite discreto
  - Complementa configuración de FPS y UPS
  - Método `.get(MAXIMUM_UPDATES_PER_FRAME)` con valor por defecto para robustez
- **Condición doble en loop de updates**
  - Antes: `while (deltaTime >= 1.0D)`
  - Después: `while ((deltaTime >= 1.0D) && (updateCount < maxUpdatesPerFrame))`
  - Primera condición: hay tiempo acumulado para update
  - Segunda condición: no se ha alcanzado el límite de updates
  - Variable `maxUpdatesPerFrame` precalculada con valor por defecto
- **Incremento de contador**: `updateCount++` después de cada `update()`

### Cambiado

- **Loop de updates ahora limitado**
  - Si el sistema es muy lento, solo ejecuta máximo 5 updates
  - Después abandona el catch-up y continúa con el render
  - Sacrifica precisión física temporal para mantener respuesta visual
- Versión actualizada de 0.3.3 a 0.3.4

### Protegido

- **Prevención de Spiral of Death**
  - Sin protección: Sistema lento → más updates → más lento → infinitos updates → crash
  - Con protección: Sistema lento → 5 updates máximo → render continúa → aplicación responde
  - Trade-off: Física puede "saltar" frames en hardware muy lento, pero la app no se congela

### Notas Técnicas

- **Spiral of Death**: Problema clásico de Fixed Timestep
  - Ocurre cuando un update tarda más que el timestep fijo
  - Ejemplo: Update de 60 Hz (16.6ms) tarda 20ms en ejecutarse
  - Acumulador crece más rápido de lo que puede procesarse
  - Loop `while (deltaTime >= 1.0)` intenta catch-up infinito
  - Sistema queda congelado ejecutando solo updates, nunca renders
- **Solución implementada**:
  - Contador `updateCount` limita iteraciones del loop
  - Valor configurable: `game.maximum.updates.per.frame = 5`
  - Variable precalculada con valor por defecto: `get(5)`
  - Casting innecesarios eliminados para mejor legibilidad
  - Permite hasta 5 updates consecutivos antes de forzar un render
  - Si el límite se alcanza, `deltaTime` mantiene su valor alto
  - Próximo frame continuará con el tiempo acumulado restante
- **Trade-offs de la protección**:
  - ✅ Aplicación siempre responde, nunca se congela
  - ✅ UI y renderizado mantienen fluidez visual
  - ✅ Input del usuario sigue siendo procesado
  - ⚠️ En hardware muy lento, física puede perder precisión temporal
  - ⚠️ Objetos pueden "saltar" posiciones si se saltan updates
- **Cuándo se activa**:
  - Sistema ejecutando otras aplicaciones pesadas
  - Hardware por debajo de especificaciones mínimas
  - Algoritmos de update no optimizados (O(n²), etc.)
  - Carga de recursos bloqueante durante update
- **Ejemplo de comportamiento**:
  ```
  Frame 1: deltaTime=8.5 → 5 updates → deltaTime=3.5 → LÍMITE → render
  Frame 2: deltaTime=5.8 → 5 updates → deltaTime=0.8 → LÍMITE → render
  Frame 3: deltaTime=1.2 → 1 update → deltaTime=0.2 → render (normal)
  ```
- **Valor de 5 updates**: Balance entre catch-up y protección
  - 1-2 updates: Muy conservador, pierde mucha precisión
  - 5 updates: Buena tolerancia a picos de lag temporal
  - 10+ updates: Protección débil, puede congelar visualmente
- **Alternativas no implementadas** (para versiones futuras):
  - Max frame time: Si un frame tarda >100ms, descartar acumulador
  - Variable timestep fallback: Cambiar a delta time variable en crisis
  - Pause physics: Detener simulación si no puede mantenerse
  - VSync: Sincronización con GPU (más eficiente para LWJGL)

### Limitaciones Resueltas

- ✅ Spiral of Death ya no puede congelar la aplicación
- ✅ Sistema responde incluso en hardware muy lento
- ✅ Balance entre precisión física y fluidez visual

### Limitaciones Restantes

- ⏳ Interpolación no implementada en `Window` (solo valor pasado)
- ⏳ VSync no utilizado (implementación considera ineficiente para LWJGL)
- ⏳ Thread.sleep() tiene limitaciones de precisión del SO

> **Nota**: Esta implementación de "Delta Time Final" completa el sistema de timing básico. Las siguientes versiones explorarán VSync y técnicas más eficientes específicas de LWJGL.

## [0.3.3][0.3.3] - 2025-12-09

### Añadido

- **Fixed Timestep para Updates** con renders variables
  - Campo `double deltaTime` como acumulador de tiempo entre frames
  - Inicialización de `deltaTime = 0D` en constructor
  - Cálculo de `updateTime`: tiempo objetivo entre cada update fijo
  - Loop `while (deltaTime >= 1.0D)` para ejecutar múltiples updates si es necesario
- **Nueva propiedad de configuración**
  - `game.updates.per.second` - UPS objetivo (Double) en `application.properties`
  - Valor por defecto: 60.0 UPS
  - Permite configurar UPS independiente de FPS
- **Enum `GAME_UPDATES_PER_SECOND`** en `GameSettings`
  - Tipo `Double.class` para mayor precisión
  - Complementa a `GAME_FRAMES_PER_SECOND`
- **Timestep fijo en `update()`**
  - `update()` recibe `1.0F / GAME_UPDATES_PER_SECOND` (tiempo fijo)
  - Ejemplo: 60 UPS → deltaTime = 0.0166 segundos (fijo)
  - Garantiza física determinística
- **Variable `fixedDeltaTime`** calculada antes del game loop
  - Valor constante usado en todos los updates
  - Calculado una sola vez: `1.0D / GAME_UPDATES_PER_SECOND`
  - Evita recalcular el timestep en cada iteración del loop de updates
- **Interpolación en `render()`**
  - `render()` recibe `(float) deltaTime` (valor fraccional restante)
  - Valor entre 0.0 y 1.0 para interpolar entre estados
  - Permite renderizado suave independiente de updates

### Cambiado

- **Separación completa de UPS y FPS**
  - `updateTime` y `renderTime` calculados independientemente
  - Updates ejecutados con frecuencia fija (Fixed Timestep)
  - Renders ejecutados con frecuencia variable (basado en frame capping)
- **DeltaTime ahora es acumulador**
  - Tipo cambiado de `float` local a `double` campo de instancia
  - Acumula tiempo: `deltaTime += (currentTime - previousTime) / updateTime`
  - Decrementa en cada update: `deltaTime--` después de cada iteración
- **Update con timestep constante**
  - Antes: `update(deltaTime)` con valor variable
  - Después: `update(fixedDeltaTime)` con valor fijo precalculado
  - Independiente del frame rate
- **Render con valor de interpolación**
  - Antes: `render(deltaTime)` con tiempo transcurrido
  - Después: `render((float) deltaTime)` con valor 0.0-1.0
  - Representa progreso entre el último update y el siguiente
- **Eliminación de variable redundante**
  - Antes: `frameStartTime` y `currentTime` eran idénticos
  - Después: Solo `currentTime` para claridad
- **Comentarios mejorados** para mayor claridad educativa
  - "Fixed timestep for deterministic updates"
  - "Accumulate time in 'update units'"
  - "Run all accumulated updates with fixed timestep"
  - "Render with interpolation alpha (0.0 to 1.0)"
- Versión actualizada de 0.3.2 a 0.3.3

### Optimizado

- **Cálculo de fixedDeltaTime extraído del loop**
  - Antes: Calculado en cada iteración del `while (deltaTime >= 1.0)`
  - Después: Calculado una sola vez antes del game loop
  - Mejora significativa si hay múltiples updates por frame
  - Ejemplo: 4 updates pendientes → ahora 1 cálculo en lugar de 4

### Notas Técnicas

- **Fixed Timestep**: Técnica que garantiza updates a intervalos regulares
  - Physics engines requieren timestep constante para determinismo
  - Evita problemas de física (objetos atravesando paredes, túneling, etc.)
  - Acumulador permite "catch up" si un frame tarda demasiado
- **Acumulador de deltaTime**:
  - Convierte tiempo real en "unidades de update"
  - Valor >= 1.0 significa que debe ejecutarse al menos 1 update
  - Loop while ejecuta todos los updates pendientes
  - Ejemplo: si pasan 3 frames de update, ejecuta 3 updates seguidos
- **Interpolación para renderizado**:
  - `deltaTime` fraccional (0.0-1.0) después del loop de updates
  - Permite suavizar visualmente entre estados discretos
  - Fórmula: `posRenderizada = posAnterior + (posActual - posAnterior) * alpha`
  - `alpha` es el `deltaTime` pasado a `render()`
- **Ventajas del Fixed Timestep**:
  - Física determinística: mismo input siempre produce mismo output
  - Independencia total entre lógica y renderizado
  - Permite replays, networking, debugging consistente
  - UPS puede ser diferente de FPS (ej: 30 UPS, 144 FPS)
- **Ejemplos de comportamiento**:
  - Sistema rápido (144 FPS, 60 UPS): 1 update cada ~2.4 renders
  - Sistema lento (30 FPS, 60 UPS): 2 updates por cada render
  - Sistema muy lento (15 FPS, 60 UPS): 4 updates por render (catch up)
- **Limitaciones restantes** (educativas):
  - Sin "spiral of death" protection (máximo de updates por frame)
  - Sin interpolación implementada en Window (solo valor pasado)
  - Sin separación de estados anterior/actual para interpolación real

## [0.3.2][0.3.2] - 2025-12-09

### Añadido

- **Implementación de Delta Time** en el game loop
  - Variable `deltaTime` calculada como tiempo transcurrido entre frames en segundos
  - Fórmula: `deltaTime = (currentTime - previousTime) / NANOSECONDS_IN_SECOND`
  - Tipo `float` para balance entre precisión y rendimiento
- **Constante `FRAMERATE`** en `Application`
  - Valor por defecto: `60.0D` (double)
  - Usado como fallback en `GameSettings.GAME_FRAMES_PER_SECOND.get(FRAMERATE)`
- **Constante `NANOSECONDS_IN_SECOND`** en `Application`
  - Valor: `TimeUnit.SECONDS.toNanos(1L)` = 1,000,000,000 nanosegundos
  - Constante privada estática para reutilización
  - Usada en cálculo de `renderTime` y `deltaTime`
  - Mejora rendimiento: se calcula una vez en la carga de la clase
- **Parámetro deltaTime en métodos de actualización**
  - `Application.update(float deltaTime)` - Recibe y propaga deltaTime
  - `Application.render(float deltaTime)` - Recibe y propaga deltaTime
  - `Window.update(float deltaTime)` - Recibe deltaTime para lógica del juego
  - `Window.render(float deltaTime)` - Recibe deltaTime para interpolación de renderizado

### Cambiado

- **Métodos `update()` y `render()` ahora reciben deltaTime**
  - Antes: `update()` y `render()` sin parámetros
  - Después: `update(float deltaTime)` y `render(float deltaTime)`
  - Cambio aplicado en `Application` y `Window`
- **Simplificación del cálculo de deltaTime**
  - Variable redundante `currentTime = frameStartTime` eliminada
  - Uso directo de `currentTime` capturado con `System.nanoTime()`
  - Cálculo simplificado: `(currentTime - previousTime) / NANOSECONDS_IN_SECOND`
- **Conversión optimizada de deltaTime**
  - Eliminación de casts redundantes
  - Cast solo en el divisor: `(float) NANOSECONDS_IN_SECOND`
- **Debug output reducido en `Window`**
  - Prints de deltaTime comentados/eliminados para evitar spam en consola
  - Anteriormente imprimía 120 líneas/segundo (60 update + 60 render)
- **Comparación de sleepTime mejorada**: `sleepTime > 0L` con sufijo long explícito
- Versión actualizada de 0.3.1 a 0.3.2

### Optimizado

- **Extracción de constante `NANOSECONDS_IN_SECOND`**
  - Evita llamadas repetidas a `TimeUnit.SECONDS.toNanos(1L)`
  - Mejor legibilidad del código
  - Principio DRY (Don't Repeat Yourself) aplicado
  - Autodocumentación del valor (1 mil millones de nanosegundos = 1 segundo)

### Notas Técnicas

- **Delta Time**: Tiempo transcurrido entre frames expresado en segundos
  - Valores típicos: ~0.0166 segundos (para 60 FPS)
  - Permite actualizar movimientos: `posición += velocidad * deltaTime`
  - Garantiza simulación consistente independiente del frame rate
- **Independencia de hardware**: 
  - Sistema rápido (120 FPS): deltaTime ~0.0083s, movimientos más frecuentes pero más pequeños
  - Sistema lento (30 FPS): deltaTime ~0.0333s, movimientos menos frecuentes pero más grandes
  - Resultado: misma distancia recorrida en ambos casos
- **Precisión de float vs double**:
  - `deltaTime` usa `float` para optimizar memoria y rendimiento
  - Precisión suficiente para valores entre 0.001 y 1.0 segundos
  - `float` tiene ~7 dígitos de precisión, adecuado para 0.0166
- **Frame timing separado**:
  - `frameStartTime`: captura tiempo al inicio del loop
  - `previousTime`: usado solo para calcular deltaTime
  - `elapsedTime`: tiempo total del frame (update + render)
  - Evita acumulación de errores en medición
- **Limitaciones restantes** (educativas):
  - UPS y FPS siguen acoplados (mismo ciclo)
  - Sin fixed timestep para física determinística
  - Sin interpolación de renderizado
  - Delta time no está "clamped" (puede ser muy grande si hay lag)

## [0.3.1][0.3.1] - 2025-12-09

### Añadido

- **Sistema de Frame Capping con timing dinámico**
  - Campo `long previousTime` para almacenar timestamp del frame anterior
  - Inicialización de `previousTime` con `System.nanoTime()` en constructor
  - Cálculo de `renderTime` objetivo basado en FPS configurado
  - Variable `elapsedTime` para medir tiempo real entre frames
  - Cálculo dinámico de `sleepTime` que compensa el overhead de ejecución
- **Nueva propiedad de configuración**
  - `game.frames.per.second` - FPS objetivo (Double) en `application.properties`
  - Valor por defecto: 60.0 FPS
- **Enum `GAME_FRAMES_PER_SECOND`** en `GameSettings`
  - Tipo `Double.class` para mayor precisión
  - Reemplaza `GAME_FREQUENCY_TIME` (Long)

### Cambiado

- **Game loop ahora usa Frame Capping** en lugar de Fixed Sleep
  - Calcula `renderTime = TimeUnit.SECONDS.toNanos(1L) / framesPerSecond`
  - Mide tiempo transcurrido: `elapsedTime = System.nanoTime() - previousTime`
  - Ajusta sleep dinámicamente: `sleepTime = (renderTime - elapsedTime) / toNanos(1L)`
  - Solo hace `Thread.sleep(sleepTime)` si `sleepTime > 0`
  - Actualiza `previousTime = System.nanoTime()` después de cada iteración
- **Medición de tiempo de alta precisión**
  - De `System.currentTimeMillis()` a `System.nanoTime()` para el game loop
  - Precisión en nanosegundos en lugar de milisegundos
- **Separación de salida en consola**
  - `update()` ahora imprime solo "Updates Per Second (UPS): X"
  - `render()` ahora imprime solo "Frames Per Second (FPS): Y"
  - Salidas independientes en lugar de formato combinado
- **Valor por defecto añadido** en `GAME_FRAMES_PER_SECOND.get(60.0)` para mayor robustez
- **Comparación corregida**: `sleepTime > 0` (long) en lugar de `> 0.0F` (float) para consistencia de tipos
- Versión actualizada de 0.3.0 a 0.3.1

### Eliminado

- Propiedad `game.frequency.time` (reemplazada por `game.frames.per.second`)
- Enum `GAME_FREQUENCY_TIME` (reemplazado por `GAME_FRAMES_PER_SECOND`)
- Sleep fijo de 16ms (reemplazado por cálculo dinámico)

### Notas Técnicas

- **Frame Capping**: Técnica que ajusta el sleep según el tiempo real de ejecución
  - Compensa el overhead de `update()` y `render()`
  - Mantiene frame rate más estable que Fixed Sleep
  - Usa conversión de nanosegundos: `TimeUnit.SECONDS.toNanos(1L)` y `TimeUnit.MILLISECONDS.toNanos(1L)`
- **Alta precisión**: `System.nanoTime()` proporciona precisión a nivel de nanosegundos
- **Cálculo de renderTime**: Se calcula una vez al inicio del loop
  - Fórmula: `1 segundo en nanosegundos / FPS objetivo`
  - Ejemplo: `1,000,000,000 ns / 60 FPS = 16,666,666 ns por frame`
- **Limitaciones conocidas** (educativas):
  - UPS y FPS siguen acoplados al mismo ciclo
  - No recupera frames perdidos si `elapsedTime > renderTime`
  - `Thread.sleep()` tiene variabilidad del sistema operativo (1-15ms típicamente)
  - Sin implementación de delta time para movimientos independientes del frame rate
- Mejora progresiva hacia control profesional de game loop

## [0.3.0][0.3.0] - 2025-12-09

### Añadido

- **Sistema de medición de FPS (Frames Per Second)** en `Application`
  - Campo `int fps` para contar frames por segundo
  - Campo `long fpsTime` para timestamp de medición de FPS
  - Método `increaseFps()` para incrementar contador
  - Método `resetFps()` para reiniciar contador a cero
  - Método `resetFpsTime()` para actualizar timestamp
- **Sistema de medición de UPS (Updates Per Second)** en `Application`
  - Campo `int ups` para contar actualizaciones por segundo
  - Campo `long upsTime` para timestamp de medición de UPS
  - Método `increaseUps()` para incrementar contador
  - Método `resetUps()` para reiniciar contador a cero
  - Método `resetUpsTime()` para actualizar timestamp
- **Impresión de estadísticas en consola** cada segundo
  - Formato: `UPS: X | FPS: Y`
  - Actualización automática de contadores
- **Soporte extendido de tipos en `Configuration`**
  - `Byte` - Conversión con `Byte.parseByte()`
  - `Short` - Conversión con `Short.parseShort()`
  - `Float` - Conversión con `Float.parseFloat()`
  - `Character` - Extracción con `charAt(0)`, valor por defecto '\0'

### Cambiado

- **Game loop ahora mide rendimiento en tiempo real**
  - Llama a `increaseUps()` después de cada `update()`
  - Llama a `increaseFps()` después de cada `render()`
  - Verifica timestamps cada iteración para imprimir estadísticas
- `Application.run()` ahora usa `TimeUnit.SECONDS.toMillis(1L)` para obtener 1 segundo en milisegundos
- `Application` ahora usa `System.currentTimeMillis()` para mediciones de tiempo
- Versión actualizada de 0.2.3 a 0.3.0

### Notas Técnicas

- **Enfoque "Fixed Sleep"**: Implementación educativa con limitaciones conocidas
  - FPS y UPS están acoplados (mismo ciclo)
  - `Thread.sleep(16)` proporciona ~60 FPS/UPS objetivo
  - No compensa el overhead de `update()` y `render()`
  - Los valores varían según carga del sistema
- Medición de tiempo con `System.currentTimeMillis()`
- Reset automático de contadores cada segundo
- Separación lógica de medición FPS/UPS aunque ejecuten en el mismo ciclo
- Base para futuras mejoras en control de timing
- Todos los tipos primitivos de Java ahora soportados en Configuration
- Character type usa `charAt(0)` para extraer primer carácter del string

## [0.2.3][0.2.3] - 2025-11-21

### Añadido

- **Enum `WindowSettings`** para encapsular propiedades de ventana
  - `WINDOW_WIDTH` - Ancho de la ventana (Integer)
  - `WINDOW_HEIGHT` - Alto de la ventana (Integer)
  - `WINDOW_TITLE` - Título de la ventana (String)
- **Enum `GameSettings`** para encapsular propiedades del juego
  - `GAME_FREQUENCY_TIME` - Tiempo de frame en milisegundos (Long)
- Métodos `get()` y `get(T defaultValue)` en enums de settings
- Flag `initialized` en `Configuration` para control de estado
- Método `initialized()` para validar que Configuration está cargada antes de acceder
- Paquete `engine.configuration.settings` para clases de configuración
- Validación fail-fast con `IllegalStateException` si se accede sin inicializar

### Cambiado

- **Método `property(String)` ahora es privado** - Encapsulamiento mejorado
- `Application` ahora usa `WindowSettings.WINDOW_WIDTH.get()` en lugar de strings literales
- `Application` ahora usa `WindowSettings.WINDOW_HEIGHT.get()` en lugar de strings literales
- `Application` ahora usa `WindowSettings.WINDOW_TITLE.get()` en lugar de strings literales
- `Application` ahora usa `GameSettings.GAME_FREQUENCY_TIME.get()` para frame time
- Acceso a configuración ahora es type-safe mediante enums
- Eliminación de strings mágicos en el código de aplicación
- Versión actualizada de 0.2.2 a 0.2.3

### Eliminado

- Soporte para `Duration` en `Configuration` - Simplificado a `Long` para milisegundos
- Acceso directo a propiedades mediante strings desde código de aplicación

### Notas Técnicas

- **Patrón Enum para settings**: Type-safety a nivel de compilación
- Enums proporcionan autocomplete en IDE y refactoring seguro
- Principio DRY aplicado: propiedades definidas una sola vez en enums
- Cada enum encapsula nombre de propiedad y tipo de dato
- Mejora la mantenibilidad: cambios en nombres de propiedades solo en un lugar
- Sistema de configuración completamente type-safe desde el código cliente

## [0.2.2][0.2.2] - 2025-11-21

### Añadido

- Sistema de caché en clase `Configuration` para propiedades convertidas
- `ConcurrentHashMap<String, Object>` para almacenar valores en caché
- Caché thread-safe para acceso concurrente seguro
- Cacheo automático de valores convertidos (Integer, Long, Boolean, Double, String)
- Cacheo de valores por defecto cuando una propiedad no existe

### Cambiado

- `property(String, Class<T>)` ahora usa caché antes de realizar conversión
- Clave de caché compuesta: `propiedad:nombreClase` para unicidad
- Versión actualizada de 0.2.1 a 0.2.2

### Optimizado

- Conversiones de tipo se realizan solo una vez por propiedad+tipo
- Mejora significativa en game loop: `property("game.frequency.time", Integer.class)` 
  se ejecuta ~60 veces/segundo, ahora convierte solo la primera vez
- Reducción de parsing repetitivo de strings a tipos primitivos

### Notas Técnicas

- `ConcurrentHashMap` garantiza thread-safety sin sincronización explícita
- Patrón de caché implementado: Check-Convert-Store
- Variable temporal `value` para mejor legibilidad del código
- Caché se mantiene durante toda la vida de la aplicación

## [0.2.1][0.2.1] - 2025-11-21

### Añadido

- Clase `Configuration` en paquete `engine.configuration`
- Archivo de configuración `application.properties` en `src/main/resources/es/noa/rad/game/settings/`
- Propiedades configurables:
  - `window.width` - Ancho de la ventana
  - `window.height` - Alto de la ventana
  - `window.title` - Título de la ventana
  - `game.frequency.time` - Tiempo de frame en milisegundos
- Método `property(String)` para obtener propiedades como String
- Método `property(String, Class<T>)` para obtener propiedades con conversión de tipo
- Método `property(String, Class<T>, T)` para obtener propiedades con valor por defecto
- Soporte para tipos: Integer, Long, Boolean, Double y String
- Método `init()` en Configuration para cargar propiedades
- Cierre explícito de InputStream en bloque finally

### Cambiado

- `Application` eliminó constantes hardcodeadas (WIDTH, HEIGHT, TITLE, FRAME_TIME)
- `Application.init()` ahora carga Configuration y usa propiedades
- `Application.run()` usa `Configuration.get().property()` para frame time
- Valores de configuración ahora se leen desde `application.properties`
- Versión actualizada de 0.2.0 a 0.2.1

### Notas Técnicas

- Patrón Singleton thread-safe en Configuration
- Try-catch-finally para gestión correcta de recursos
- Conversión de tipos genérica con soporte para primitivos y wrappers
- Manejo de excepciones con IllegalArgumentException si propiedad no existe
- Método con valor por defecto usa try-catch para gestionar propiedades opcionales
- Documentación completa en application.properties

## [0.2.0][0.2.0] - 2025-11-20

### Añadido

- Dependencia LWJGL 3.3.1 (Lightweight Java Game Library)
- Dependencia JOML 1.10.5 (Java OpenGL Math Library)
- Dependencia JOML Primitives 1.10.0
- Módulos LWJGL:
  - lwjgl-glfw - Gestión de ventanas y entrada
  - lwjgl-opengl - API de renderizado OpenGL
  - lwjgl-openal - API de audio OpenAL
  - lwjgl-assimp - Importación de modelos 3D
  - lwjgl-bgfx - Abstracción de gráficos
  - lwjgl-nanovg - Gráficos vectoriales
  - lwjgl-nuklear - GUI inmediato
  - lwjgl-par - Generación de formas paramétricas
  - lwjgl-stb - Carga de imágenes y fuentes
  - lwjgl-vulkan - API Vulkan
- Clase `Window` con patrón Singleton
- Gestión de ventana con GLFW
- Ventana configurable (1280x720, "3D Game Engine Tutorial")
- Ventana centrada automáticamente en la pantalla
- Sistema de double buffering
- Polling de eventos de ventana
- Detección de cierre de ventana
- Método `cleanup()` en Window para liberar recursos
- Método `cleanup()` en Application que delega en Window
- Constantes en `Application`: WIDTH, HEIGHT, TITLE
- Natives de LWJGL para Windows

### Cambiado

- Game loop ahora termina cuando se cierra la ventana
- `Application.init()` ahora inicializa la clase Window
- `Application.update()` delega en `Window.update()`
- `Application.render()` delega en `Window.render()`
- `Application.cleanup()` delega en `Window.cleanup()`
- Bucle while ahora usa `Window.get().shouldClose()`
- Versión actualizada de 0.1.0 a 0.2.0

### Notas Técnicas

- Patrón Singleton thread-safe implementado en Window
- Inicialización correcta de GLFW
- Control de errores en creación de ventana
- Gestión de memoria con MemoryUtil.NULL
- Liberación correcta de recursos con glfwDestroyWindow y glfwTerminate
- Ciclo de vida completo: init → loop → cleanup
- Getters y setters para propiedades de Window
- Ventana visible y funcional

## [0.1.0][0.1.0] - 2025-11-20

### Añadido

- Implementación básica del Game Loop
- Clase `Application` ahora implementa `Runnable`
- Thread dedicado para el motor de juego
- Método `init()` para inicialización del juego
- Método `update()` para actualización de lógica del juego
- Método `render()` para renderizado del juego
- Loop infinito con ciclo update-render
- Control básico de frame rate con `Thread.sleep()`
- Constante `FRAME_TIME` configurada a 16ms (~60 FPS)

### Cambiado

- `Application` cambió de simple "Hello World" a arquitectura de game loop
- Versión actualizada de 0.0.0 a 0.1.0

### Notas Técnicas

- Game loop ejecuta en thread separado
- Update y render se ejecutan continuamente
- Frame rate aproximado de 60 FPS (16ms por frame)
- Manejo de `InterruptedException` en el game loop
- Sin mecanismo de salida/shutdown implementado aún

## [0.0.0][0.0.0] - 2025-11-19

### Añadido

- Estructura inicial del proyecto Maven
- Clase principal `Application.java` con mensaje "Hello, World!"
- Configuración de Java 21 y Maven 3.8.6
- Estructura de directorios estándar Maven:
  - `src/main/java` - Código fuente principal
  - `src/main/resources` - Recursos de la aplicación
  - `src/test/java` - Código fuente de pruebas
  - `src/test/resources` - Recursos de pruebas
- Configuración de Checkstyle con reglas estrictas
  - Límite de 80 caracteres por línea
  - Convenciones de código Sun/Oracle Java
- Plugin `exec-maven-plugin` para ejecutar la aplicación con Maven
- Archivo `.editorconfig` para consistencia de formato
- Archivo `.gitignore` completo para Maven, IDEs y OS
- Archivo `.gitattributes` para control de finales de línea
  - Fuerza LF en archivos de texto
  - Mantiene CRLF en scripts de Windows
  - Marca archivos binarios correctamente
- Documentación completa en `README.md`:
  - Requisitos del proyecto
  - Instrucciones de compilación y ejecución
  - Comandos para pruebas y verificación de calidad
  - Estructura del proyecto
- Archivo `LICENSE`

### Configuración Maven

- maven-clean-plugin 3.3.2
- maven-enforcer-plugin 3.4.1
- maven-resources-plugin 3.3.1
- maven-compiler-plugin 3.12.1
- maven-surefire-plugin 3.2.5
- maven-jar-plugin 3.3.0
- maven-checkstyle-plugin 3.3.1
- exec-maven-plugin 3.1.1
- maven-install-plugin 3.1.1
- maven-deploy-plugin 3.1.1

### Notas Técnicas

- Encoding UTF-8 para todo el proyecto
- Sin dependencias externas
- Build exitoso sin errores ni warnings
- Código cumple 100% con reglas de Checkstyle

[0.4.1]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.4.1
[0.4.0]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.4.0
[0.3.10]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.3.10
[0.3.9]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.3.9
[0.3.8]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.3.8
[0.3.7]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.3.7
[0.3.6]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.3.6
[0.3.5]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.3.5
[0.3.4]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.3.4
[0.3.3]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.3.3
[0.3.2]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.3.2
[0.3.1]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.3.1
[0.3.0]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.3.0
[0.2.3]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.2.3
[0.2.2]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.2.2
[0.2.1]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.2.1
[0.2.0]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.2.0
[0.1.0]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.1.0
[0.0.0]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.0.0