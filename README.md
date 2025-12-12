# 3D Game Engine Tutorial

Un motor de juego 3D desarrollado en Java como proyecto tutorial.

## Requisitos

- **Java**: JDK 21 o superior
- **Maven**: 3.8.6 o superior

## Compilación y Ejecución

### Compilar el proyecto

```bash
mvn clean compile
```

### Ejecutar la aplicación

**Opción 1: Usando Maven** (recomendado)

```bash
mvn exec:java
```

**Opción 2: Manualmente**

```bash
# Primero compilar
mvn clean compile

# Luego ejecutar
java -cp target\classes es.noa.rad.game.Application
```

### Generar JAR

```bash
mvn clean package
```

El archivo JAR se generará en `target/3d-game-engine-tutorial-0.4.3.jar`

## Sistema de Configuración

El proyecto incluye un sistema de configuración flexible basado en archivos `.properties`:

### Archivo de Configuración

Las propiedades se definen en `src/main/resources/es/noa/rad/game/settings/application.properties`:

```properties
# Window Configuration
window.width = 1280
window.height = 720
window.title = 3D Game Engine Tutorial

# Game Loop Configuration
game.vertical.synchronization = true
game.updates.per.second = 60.0
```

### Uso de Configuration

El sistema utiliza enums type-safe para acceder a las propiedades:

```java
// Inicializar configuración (solo una vez al inicio)
Configuration.get().init();

// Usar WindowSettings para propiedades de ventana
Integer width = WindowSettings.WINDOW_WIDTH.get();
Integer height = WindowSettings.WINDOW_HEIGHT.get();
String title = WindowSettings.WINDOW_TITLE.get();

// Usar valores por defecto si la propiedad no existe
Integer width = WindowSettings.WINDOW_WIDTH.get(1280);

// Usar GameSettings para propiedades del juego (con valores por defecto)
Boolean vsync = GameSettings.GAME_VERTICAL_SYNCHRONIZATION.get(true);
Double updatesPerSecond = GameSettings.GAME_UPDATES_PER_SECOND.get(60.0);
```

#### Ventajas del sistema de enums:

- **Type-safe**: Los tipos están definidos en tiempo de compilación
- **Autocomplete**: El IDE muestra todas las propiedades disponibles
- **Refactoring seguro**: Cambiar el nombre de una propiedad es seguro
- **Sin strings mágicos**: No hay strings literales en el código
- **Documentación implícita**: Los enums documentan qué propiedades existen

### Características

- **Tipos soportados**: Byte, Short, Integer, Long, Float, Double, Boolean, Character, String
- **Sistema de caché**: Thread-safe con `ConcurrentHashMap`
- **Valores por defecto**: Soporte para propiedades opcionales
- **Singleton**: Instancia única accesible globalmente
- **Optimizado**: Las conversiones se realizan solo una vez

## Sistema de Medición de Rendimiento

El motor incluye un sistema de medición de FPS (Frames Per Second) y UPS (Updates Per Second) que permite monitorear el rendimiento en tiempo real.

### Evolución del Sistema de Timing

#### v0.3.0 - Fixed Sleep

La versión 0.3.0 implementó el enfoque **"Fixed Sleep"** básico:

```java
update();          // Actualizar lógica
render();          // Renderizar frame
Thread.sleep(16);  // Esperar fijo ~16ms (objetivo: 60 FPS/UPS)
```

**Limitaciones**: UPS=FPS acoplados, no compensa overhead de ejecución, frame rate variable.

#### v0.3.1 - Frame Capping

La versión 0.3.1 introduce **"Frame Capping"** con medición dinámica del tiempo entre frames:

```java
// Configuración inicial
double renderTime = TimeUnit.SECONDS.toNanos(1L) / framesPerSecond;
long previousTime = System.nanoTime();

// En el game loop
update();
render();

// Calcular tiempo transcurrido
long elapsedTime = System.nanoTime() - previousTime;

// Calcular sleep dinámico
long sleepTime = (renderTime - elapsedTime) / TimeUnit.MILLISECONDS.toNanos(1L);
if (sleepTime > 0) {
    Thread.sleep(sleepTime);
}

previousTime = System.nanoTime();
```

**Mejoras**: Compensación dinámica del overhead, frame rate más estable, alta precisión con nanosegundos.

#### v0.3.2 - Delta Time

La versión 0.3.2 añade **"Delta Time"** para movimientos independientes del frame rate:

```java
// Constante para conversión de nanosegundos a segundos
private static final long NANOSECONDS_IN_SECOND = TimeUnit.SECONDS.toNanos(1L); // 1,000,000,000

// Configuración inicial
double renderTime = NANOSECONDS_IN_SECOND / framesPerSecond;
long previousTime = System.nanoTime();

// En el game loop
long currentTime = System.nanoTime();

// Calcular Delta Time en segundos
float deltaTime = (currentTime - previousTime) / (float) NANOSECONDS_IN_SECOND;
previousTime = currentTime;

// Pasar deltaTime a update y render
update(deltaTime);  // Lógica usa deltaTime
render(deltaTime);  // Renderizado usa deltaTime

// Calcular tiempo del frame
long elapsedTime = System.nanoTime() - currentTime;

// Sleep dinámico
long sleepTime = (renderTime - elapsedTime) / TimeUnit.MILLISECONDS.toNanos(1L);
if (sleepTime > 0L) {
    Thread.sleep(sleepTime);
}
```

### Características del Sistema (v0.3.2)

- **Delta Time implementado**: Tiempo transcurrido entre frames en segundos (tipo `float`)
- **Constante extraída**: `NANOSECONDS_IN_SECOND` evita cálculos repetidos (DRY principle)
- **Movimientos independientes del FPS**: Los objetos se mueven la misma distancia independientemente del frame rate
- **Propagación de deltaTime**: `update(deltaTime)` y `render(deltaTime)` reciben el valor
- **Código simplificado**: Eliminadas variables redundantes y casts innecesarios
- **Mejor rendimiento**: Constante calculada una sola vez al cargar la clase
- **Medición en tiempo real**: Imprime estadísticas FPS/UPS cada segundo
- **Frame Capping**: Ajusta el sleep dinámicamente según el tiempo real de ejecución
- **Alta precisión**: Usa `System.nanoTime()` para medición en nanosegundos
- **Configurable**: FPS objetivo definido en `application.properties` (60.0 por defecto)
- **Robusto**: Valor por defecto `FRAMERATE = 60.0D` si falta la configuración

### Ventajas del Delta Time

- ✅ **Independencia de hardware**: Mismo comportamiento en PCs rápidos y lentos
- ✅ **Simulación consistente**: `posición += velocidad * deltaTime`
- ✅ **Suavidad garantizada**: Los movimientos son proporcionales al tiempo real
- ✅ **Base para física realista**: Permite implementar física determinística

### Optimizaciones Aplicadas

- **Constante `NANOSECONDS_IN_SECOND`**: 
  - Valor precalculado: 1,000,000,000 nanosegundos
  - Se calcula una vez al cargar la clase
  - Evita llamadas repetidas a `TimeUnit.SECONDS.toNanos(1L)`
  - Mejora legibilidad: nombre autodocumentado
  - Principio DRY aplicado

### Ejemplo de Uso

```java
// Sin Delta Time (Incorrecto - dependiente del FPS)
position += velocity;  // Se mueve más rápido a mayor FPS

// Con Delta Time (Correcto - independiente del FPS)
position += velocity * deltaTime;  // Movimiento consistente

// Ejemplo práctico:
// velocidad = 100 unidades/segundo
// A 60 FPS: deltaTime ≈ 0.0166s → movimiento = 100 * 0.0166 = 1.66 unidades/frame
// A 30 FPS: deltaTime ≈ 0.0333s → movimiento = 100 * 0.0333 = 3.33 unidades/frame
// Resultado: 100 unidades por segundo en ambos casos
```

**Limitaciones**: UPS=FPS acoplados, sin fixed timestep, física no determinística.

#### v0.3.3 - Fixed Timestep para Updates

La versión 0.3.3 introduce **"Fixed Timestep"** separando completamente updates de renders:

```java
// Configuración inicial (calculado una vez)
double renderTime = NANOSECONDS_IN_SECOND / framesPerSecond;
double updateTime = NANOSECONDS_IN_SECOND / updatesPerSecond;
float fixedDeltaTime = (float) (1.0 / updatesPerSecond);  // Timestep fijo
double deltaTime = 0.0;  // Acumulador
long previousTime = System.nanoTime();

// En el game loop
long currentTime = System.nanoTime();

// Acumular tiempo en "unidades de update"
deltaTime += (currentTime - previousTime) / updateTime;
previousTime = currentTime;

// Ejecutar todos los updates pendientes con timestep fijo precalculado
while (deltaTime >= 1.0) {
    update(fixedDeltaTime);  // Siempre el mismo valor: 0.0166s
    deltaTime--;
}

// Renderizar con valor de interpolación (0.0 a 1.0)
render((float) deltaTime);

// Sleep dinámico basado en renderTime
long elapsedTime = System.nanoTime() - currentTime;
long sleepTime = (renderTime - elapsedTime) / TimeUnit.MILLISECONDS.toNanos(1L);
if (sleepTime > 0L) {
    Thread.sleep(sleepTime);
}
```

### Características del Sistema (v0.3.3)

- **Fixed Timestep implementado**: Updates ejecutados con intervalo de tiempo constante
- **UPS desacoplado de FPS**: Configurable independientemente (`game.updates.per.second`)
- **Acumulador de tiempo**: `deltaTime` tipo `double` acumula tiempo entre frames
- **Loop de catch-up**: `while (deltaTime >= 1.0)` ejecuta múltiples updates si es necesario
- **Variable `fixedDeltaTime` precalculada**: Timestep constante calculado una vez fuera del loop
- **Timestep constante**: `update(fixedDeltaTime)` siempre recibe el mismo valor (ej: 0.0166s)
- **Física determinística**: Mismo timestep garantiza resultados consistentes
- **Interpolación preparada**: `render(deltaTime)` recibe valor 0.0-1.0 para suavizado
- **Renders variables**: FPS puede ser diferente de UPS (ej: 144 FPS con 60 UPS)
- **Medición separada**: UPS y FPS se miden y reportan independientemente
- **Código optimizado**: Eliminadas variables redundantes y cálculos repetitivos

### Ventajas del Fixed Timestep

- ✅ **Física determinística**: Mismo input siempre produce mismo output
- ✅ **Independencia total**: Lógica y renderizado completamente desacoplados
- ✅ **Catch-up automático**: Si un frame tarda mucho, ejecuta múltiples updates
- ✅ **Networking friendly**: UPS fijo facilita sincronización en red
- ✅ **Debugging mejorado**: Comportamiento predecible y reproducible
- ✅ **Flexibility**: Diferentes UPS/FPS según necesidades (30 UPS, 144 FPS)
- ✅ **Rendimiento optimizado**: `fixedDeltaTime` calculado una vez, no en cada update

### Ejemplo de Comportamiento

```
Escenario 1: Sistema rápido (144 FPS, 60 UPS)
- Frame 1: 0 updates, render con alpha=0.4
- Frame 2: 0 updates, render con alpha=0.8
- Frame 3: 1 update,  render con alpha=0.2
Resultado: Renderizado ultra suave con lógica a 60 Hz

Escenario 2: Sistema lento (30 FPS, 60 UPS)
- Frame 1: 2 updates, render con alpha=0.0
- Frame 2: 2 updates, render con alpha=0.0
Resultado: Lógica mantiene 60 Hz, renderizado a 30 Hz

Escenario 3: Sistema muy lento (15 FPS, 60 UPS)
- Frame 1: 4 updates, render con alpha=0.0
Resultado: Catch-up completo, lógica no se ralentiza
```

### Uso de Interpolación

```java
// En el método render(), alpha es el deltaTime restante (0.0-1.0)
void render(float alpha) {
    // Interpolar posición entre estado anterior y actual
    float renderX = previousX + (currentX - previousX) * alpha;
    float renderY = previousY + (currentY - previousY) * alpha;
    
    // Dibujar en posición interpolada
    draw(renderX, renderY);
}

// Resultado: movimiento visual suave incluso con updates discretos
```

**Limitaciones**: Sin spiral of death protection, puede congelarse en hardware muy lento.

#### v0.3.4 - Final Delta Time con Spiral of Death Protection

La versión 0.3.4 añade **protección contra "Spiral of Death"** limitando updates por frame:

```java
// Configuración inicial (calculado una vez)
double renderTime = NANOSECONDS_IN_SECOND / framesPerSecond;
double updateTime = NANOSECONDS_IN_SECOND / updatesPerSecond;
float fixedDeltaTime = (float) (1.0 / updatesPerSecond);  // Timestep fijo
double deltaTime = 0.0;  // Acumulador
long previousTime = System.nanoTime();
int maxUpdatesPerFrame = GameSettings.GAME_MAXIMUM_UPDATES_PER_FRAME.get(5);  // 5 por defecto

// En el game loop
long currentTime = System.nanoTime();

// Acumular tiempo en "unidades de update"
deltaTime += (currentTime - previousTime) / updateTime;
previousTime = currentTime;

// Limitar updates por frame (Spiral of Death Protection)
int updateCount = 0;
while ((deltaTime >= 1.0) && (updateCount < maxUpdatesPerFrame)) {
    update(fixedDeltaTime);  // Siempre el mismo valor: 0.0166s
    deltaTime--;
    updateCount++;  // Incrementar contador
}

// Renderizar con valor de interpolación (0.0 a 1.0)
render((float) deltaTime);

// Sleep dinámico basado en renderTime
long elapsedTime = System.nanoTime() - currentTime;
long sleepTime = (renderTime - elapsedTime) / TimeUnit.MILLISECONDS.toNanos(1L);
if (sleepTime > 0L) {
    Thread.sleep(sleepTime);
}
```

### Características del Sistema (v0.3.4)

- **Spiral of Death Protection**: Límite máximo de 5 updates por frame
- **Constante de fallback**: `MAXIMUM_UPDATES_PER_FRAME = 5` en Application
- **Contador de updates**: `int updateCount` rastrea iteraciones del loop
- **Condición doble**: Loop verifica tiempo acumulado Y límite de updates
- **Configuración flexible**: `game.maximum.updates.per.frame = 5` en properties
- **Enum adicional**: `GAME_MAXIMUM_UPDATES_PER_FRAME` en GameSettings
- **Aplicación siempre responde**: Nunca se congela, incluso en hardware muy lento
- **Trade-off inteligente**: Sacrifica precisión física por fluidez visual si es necesario
- Todas las características de v0.3.3 (Fixed Timestep, UPS/FPS independientes, etc.)

### El Problema del Spiral of Death

El **Spiral of Death** es un problema clásico de Fixed Timestep que ocurre cuando:

1. Un update tarda más tiempo que el timestep fijo (ej: update de 16.6ms tarda 20ms)
2. El acumulador crece más rápido de lo que puede procesarse
3. El loop `while (deltaTime >= 1.0)` intenta hacer catch-up infinito
4. Cada update adicional hace el sistema aún más lento
5. La aplicación se congela ejecutando solo updates, nunca renders

**Ejemplo sin protección:**
```
Frame 1: 1 update tarda 20ms → acumula 3ms extra
Frame 2: Necesita 1 update, pero tarda 20ms → acumula 6ms extra
Frame 3: Necesita 1 update, pero tarda 20ms → acumula 9ms extra
Frame 4: Necesita 2 updates, tardan 40ms → acumula 26ms extra
Frame 5: Necesita 2 updates, tardan 40ms → acumula 43ms extra
Frame N: Infinitos updates, aplicación congelada ❌
```

### La Solución Implementada

**Límite de 5 updates por frame:**
```
Frame 1: deltaTime=8.5 → ejecuta 5 updates → deltaTime=3.5 → LÍMITE → render
Frame 2: deltaTime=5.8 → ejecuta 5 updates → deltaTime=0.8 → LÍMITE → render
Frame 3: deltaTime=1.2 → ejecuta 1 update  → deltaTime=0.2 → render normal
```

✅ La aplicación siempre renderiza, mantiene respuesta visual
✅ Input del usuario sigue siendo procesado
✅ UI no se congela
⚠️ En hardware muy lento, física puede "saltar" frames

### Ventajas de la Protección

- ✅ **Nunca se congela**: Aplicación siempre responde
- ✅ **Balance automático**: Entre precisión física y fluidez visual
- ✅ **Configurable**: Ajustar `game.maximum.updates.per.frame` según necesidades
- ✅ **Degradación elegante**: En lugar de crash, funciona con precisión reducida
- ✅ **Hardware tolerante**: Funciona desde PCs lentos hasta potentes

### Ejemplo de Comportamiento

```
Hardware rápido (144 FPS, 60 UPS):
- Updates por frame: 0-1
- Límite nunca se alcanza
- Funcionamiento óptimo

Hardware normal (60 FPS, 60 UPS):
- Updates por frame: 1
- Límite nunca se alcanza
- Funcionamiento óptimo

Hardware lento (20 FPS, 60 UPS):
- Updates por frame: 3
- Límite alcanzado ocasionalmente
- Funcionamiento aceptable

Hardware muy lento (10 FPS, 60 UPS):
- Updates por frame: 5 (límite)
- Física pierde precisión, pero app responde
- Funcionamiento degradado pero estable
```

**Limitaciones**: Complejidad alta, sin VSync aún.

#### v0.3.5 - VSync Básico

La versión 0.3.5 simplifica dramáticamente el game loop usando **VSync (Vertical Synchronization)**:

```java
// Constante para conversión
private static final long NANOSECONDS_IN_SECOND = TimeUnit.SECONDS.toNanos(1L);

// Inicialización
Configuration.get().init();
Window.get().init(width, height, title);

// Habilitar VSync si está configurado
if (GameSettings.GAME_VERTICAL_SYNCHRONIZATION.get(true)) {
    Window.get().enableVSync();  // glfwSwapInterval(1)
}

// Game loop simplificado
long previousTime = System.nanoTime();

while (!Window.get().shouldClose()) {
    long currentTime = System.nanoTime();
    
    // Calcular delta time en segundos
    float deltaTime = (currentTime - previousTime) / (float) NANOSECONDS_IN_SECOND;
    previousTime = currentTime;
    
    // Update y render (una vez cada uno por frame)
    update(deltaTime);
    render(deltaTime);
    
    // VSync maneja el timing automáticamente
    Window.get().swapBuffers();  // glfwSwapBuffers + glfwPollEvents
}
```

### Características del Sistema (v0.3.5)

- **VSync habilitado**: Sincronización con monitor vía `glfwSwapInterval(1)`
- **Timing por hardware**: GPU + monitor controlan el frame rate
- **Sin Thread.sleep()**: VSync bloquea en `glfwSwapBuffers()` hasta próximo refresco
- **Screen tearing eliminado**: Buffers se intercambian en sincronía con monitor
- **FPS = Refresh rate del monitor**: Típicamente 60, 75, 144, 165 Hz
- **Game loop ultra simple**: ~10 líneas vs ~50 líneas de v0.3.4
- **Delta time variable**: Simple cálculo frame-to-frame para movimientos suaves
- **UPS = FPS**: Acoplados nuevamente, pero con precisión de hardware
- **Configuración flexible**: `game.vertical.synchronization` puede desactivar VSync
- **Método `swapBuffers()`**: Combina swap + poll events en una llamada

### Ventajas de VSync

- ✅ **Simplicidad máxima**: Código limpio y fácil de entender
- ✅ **Eficiencia**: Timing manejado por hardware, no CPU
- ✅ **Sin screen tearing**: Imagen perfecta sin artefactos visuales
- ✅ **Consumo reducido**: CPU descansa durante VSync wait
- ✅ **Precisión**: Sincronización exacta con monitor
- ✅ **Estándar OpenGL/LWJGL**: Forma más común en la industria
- ✅ **FPS consistente**: No varía constantemente

### Comparación de Versiones

| Característica | v0.3.4 (Fixed Timestep) | v0.3.5 (VSync) |
|----------------|-------------------------|----------------|
| **Complejidad** | Alta (~50 líneas) | Baja (~10 líneas) |
| **Timing** | Software (Thread.sleep) | Hardware (VSync) |
| **Física** | Determinística (fixed) | Frame-dependent (variable) |
| **UPS/FPS** | Independientes | Acoplados al monitor |
| **Catch-up** | Sí (hasta 5 updates) | No |
| **Screen tearing** | Posible | Prevenido |
| **CPU idle** | Busy-wait en sleep | Bloqueado en VSync |
| **Uso típico** | Simuladores, multiplayer | Single-player, mayoría de juegos |
| **Networking** | Ideal | Requiere trabajo extra |

### Cuándo Usar Cada Enfoque

**VSync (v0.3.5) - Recomendado para:**
- ✅ Juegos single-player
- ✅ Aplicaciones gráficas interactivas
- ✅ Editores y herramientas
- ✅ Prototipos rápidos
- ✅ Cuando simplicidad es prioritaria

**Fixed Timestep (v0.3.4) - Considerar para:**
- ⚠️ Juegos multijugador competitivos
- ⚠️ Simulaciones físicas complejas
- ⚠️ Sistemas que requieren replays determinísticos
- ⚠️ Networking con predicción/rollback

### Ejemplo de Salida

```
Updates Per Second (UPS): 60
Frames Per Second (FPS): 60
Updates Per Second (UPS): 60
Frames Per Second (FPS): 60
```

> **Nota**: VSync es la forma más estándar y recomendada de implementar game loops en OpenGL/LWJGL. Esta implementación representa el enfoque más común en la industria para juegos y aplicaciones gráficas.

#### v0.3.6 - Fixed Timestep + VSync

La versión 0.3.6 combina lo mejor de ambos mundos: **Fixed Timestep para updates determinísticos** y **VSync para renders suaves**:

```java
// Constantes
private static final long NANOSECONDS_IN_SECOND = TimeUnit.SECONDS.toNanos(1L);
private static final double FRAMERATE = 60.0D;

// Variables de timing
private double deltaTime;
private long previousTime;

// Inicialización
Configuration.get().init();
Window.get().init(width, height, title);

// Habilitar VSync si está configurado
if (GameSettings.GAME_VERTICAL_SYNCHRONIZATION.get(true)) {
    Window.get().enableVSync();  // glfwSwapInterval(1)
}

// Calcular tiempos del fixed timestep
final double updatesPerSecond = GameSettings.GAME_UPDATES_PER_SECOND.get(FRAMERATE);
final double updateTime = NANOSECONDS_IN_SECOND / updatesPerSecond;
final float fixedDeltaTime = (float) (1.0 / updatesPerSecond);

// Timing inicial
previousTime = System.nanoTime();

// Game loop híbrido
while (!Window.get().shouldClose()) {
    long currentTime = System.nanoTime();
    
    // Acumular tiempo transcurrido
    deltaTime += (currentTime - previousTime) / updateTime;
    previousTime = currentTime;
    
    // Fixed updates: ejecutar hasta consumir el tiempo acumulado
    while (deltaTime >= 1.0D) {
        update(fixedDeltaTime);  // Siempre el mismo valor (ej: 0.0166s para 60 UPS)
        deltaTime--;
    }
    
    // VSync render: deltaTime restante es el "alpha" para interpolación
    render((float) deltaTime);  // Alpha entre 0.0 y 1.0
    
    // VSync bloquea aquí hasta el próximo refresco del monitor
    Window.get().swapBuffers();  // glfwSwapBuffers + glfwPollEvents
}
```

### Características del Sistema (v0.3.6)

- **Fixed Timestep para updates**: Física determinística con `fixedDeltaTime` constante
- **VSync para renders**: Sincronización con monitor para imagen suave sin tearing
- **UPS/FPS independientes**: Updates a 60 Hz, Renders al refresh rate del monitor
- **Accumulator pattern**: `deltaTime` acumula tiempo y se consume en updates
- **Interpolation alpha**: Valor residual de `deltaTime` (0.0-1.0) para suavizado visual
- **Configuración dual**: `game.updates.per.second` y `game.vertical.synchronization`
- **Doble propósito de deltaTime**: Acumulador de updates Y alpha de render
- **Sin límite de catch-up**: Asume VSync + hardware moderno previenen lag extremo
- **Timing híbrido**: Software para updates (CPU), hardware para renders (GPU/monitor)

### Ventajas del Enfoque Híbrido

- ✅ **Física determinística**: Updates fijos permiten simulaciones predecibles
- ✅ **Networking compatible**: UPS fijo facilita sincronización entre clientes
- ✅ **Renders suaves**: VSync elimina tearing y aprovecha monitor de alta frecuencia
- ✅ **FPS variable sin consecuencias**: Física no se afecta por variaciones en FPS
- ✅ **Interpolación posible**: Alpha permite suavizado entre estados de física
- ✅ **UPS/FPS desacoplados**: 60 UPS fijo puede coexistir con 144 FPS
- ✅ **Eficiencia energética**: VSync permite idle del CPU durante espera
- ✅ **Best of both worlds**: Precisión de v0.3.4 + simplicidad de v0.3.5

### Comportamiento en Diferentes Escenarios

**Monitor 60Hz con VSync:**
```
Updates: 60 UPS constante (fixed timestep)
Renders: 60 FPS (VSync bloqueado al monitor)
Resultado: 1 update, 1 render por frame
```

**Monitor 144Hz con VSync:**
```
Updates: 60 UPS constante (fixed timestep)
Renders: 144 FPS (VSync bloqueado al monitor)
Resultado: ~2.4 renders por update (interpolación útil)
```

**Lag momentáneo:**
```
Frame largo: Acumulador crece → múltiples updates consecutivos
Siguiente frame: Catch-up completo, renders retoman suavidad
Sin límite: Asume que VSync + hardware previenen espirales de muerte
```

### Comparación con Versiones Previas

| Característica | v0.3.4 (Fixed) | v0.3.5 (VSync) | v0.3.6 (Híbrido) |
|----------------|----------------|----------------|------------------|
| **Updates** | Fijos (60 UPS) | Variables (FPS) | **Fijos (60 UPS)** |
| **Renders** | Variables (sleep) | Fijos (VSync) | **Fijos (VSync)** |
| **Física** | Determinística | Frame-dependent | **Determinística** |
| **Screen tearing** | Posible | Prevenido | **Prevenido** |
| **Interpolación** | Sí (alpha) | No | **Sí (deltaTime)** |
| **UPS/FPS** | Independientes | Acoplados | **Independientes** |
| **Timing update** | Software | Hardware | **Software** |
| **Timing render** | Software | Hardware | **Hardware** |
| **Complejidad** | Alta | Baja | **Media** |
| **Catch-up limit** | 5 updates máx | N/A | **Sin límite** |
| **Networking** | Ideal | Difícil | **Ideal** |

### Cuándo Usar Este Enfoque

**Híbrido Fixed + VSync (v0.3.6) - Recomendado para:**
- ✅ Juegos multijugador que necesitan física determinística
- ✅ Simulaciones complejas con VSync activo
- ✅ Juegos competitivos con monitores de alta frecuencia
- ✅ Cuando se necesita interpolación suave Y física precisa
- ✅ Proyectos que requieren replays determinísticos con VSync
- ✅ Motores de juego educativos que muestran ambas técnicas

**Ventajas específicas sobre v0.3.5:**
- Física no depende del FPS del monitor
- Permite networking confiable con sincronización fija
- Interpolación visual aprovecha monitores de alta frecuencia
- Simulaciones reproducibles frame-perfect

**Ventajas específicas sobre v0.3.4:**
- Eliminación total de screen tearing
- No requiere Thread.sleep ni busy-waiting
- Aprovecha hardware de GPU/monitor para timing de render
- Menor consumo de CPU durante esperas

### Ejemplo de Salida

```
Updates Per Second (UPS): 60
Frames Per Second (FPS): 60   (Monitor 60Hz)
Updates Per Second (UPS): 60
Frames Per Second (FPS): 144  (Monitor 144Hz)
```

> **Nota**: Esta versión representa un enfoque avanzado que combina lo mejor de Fixed Timestep (v0.3.4) y VSync (v0.3.5), ideal para proyectos que requieren física determinística sin sacrificar la calidad visual de VSync.

#### v0.3.7 - Fixed Timestep + VSync + Spiral of Death Protection

La versión 0.3.7 añade **protección contra Spiral of Death** a la implementación híbrida de v0.3.6:

```java
// Constantes
private static final long NANOSECONDS_IN_SECOND = TimeUnit.SECONDS.toNanos(1L);
private static final double FRAMERATE = 60.0D;
private static final int MAXIMUM_UPDATES_PER_FRAME = 5;

// Variables de timing y control
private boolean running;
private double deltaTime;
private long previousTime;

// Inicialización
Configuration.get().init();
Window.get().init(width, height, title);

// Habilitar VSync si está configurado
if (GameSettings.GAME_VERTICAL_SYNCHRONIZATION.get(true)) {
    Window.get().enableVSync();  // glfwSwapInterval(1)
}

// Iniciar el game loop
running = true;

// Calcular tiempos del fixed timestep (simplificado)
final double updatesPerSecond = GameSettings.GAME_UPDATES_PER_SECOND.get(FRAMERATE);
final double updateTime = NANOSECONDS_IN_SECOND / updatesPerSecond;
final float fixedDeltaTime = (float) (1.0 / updatesPerSecond);

// Configurar límite de spiral of death
final int maxUpdatesPerFrame = GameSettings.GAME_MAXIMUM_UPDATES_PER_FRAME.get(MAXIMUM_UPDATES_PER_FRAME);

// Timing inicial
previousTime = System.nanoTime();

// Game loop completo con triple protección
while (running && !Window.get().shouldClose()) {
    long currentTime = System.nanoTime();
    
    // Acumular tiempo transcurrido
    deltaTime += (currentTime - previousTime) / updateTime;
    previousTime = currentTime;
    
    // Fixed updates con límite de spiral of death
    int updateCount = 0;
    while (deltaTime >= 1.0D && updateCount < maxUpdatesPerFrame) {
        update(fixedDeltaTime);  // Siempre el mismo valor (0.0166s para 60 UPS)
        deltaTime--;
        updateCount++;  // Incrementar contador de protección
    }
    
    // VSync render con interpolation alpha
    render((float) deltaTime);  // Alpha entre 0.0 y 1.0
    
    // VSync bloquea aquí hasta el próximo refresco
    Window.get().swapBuffers();  // glfwSwapBuffers + glfwPollEvents
}
```

### Características del Sistema (v0.3.7)

- **Fixed Timestep para updates**: Física determinística con `fixedDeltaTime` constante
- **VSync para renders**: Sincronización con monitor para imagen suave sin tearing
- **Spiral of Death Protection**: Límite de 5 updates por frame previene congelamiento
- **UPS/FPS independientes**: Updates a 60 Hz, Renders al refresh rate del monitor
- **Accumulator pattern**: `deltaTime` acumula tiempo y se consume en updates
- **Interpolation alpha**: Valor residual de `deltaTime` (0.0-1.0) para suavizado visual
- **Control de flujo**: Campo `boolean running` para parada limpia del loop
- **Métodos de ciclo de vida**: `start()`, `stop()`, `close()` para control explícito
- **Configuración triple**: `game.updates.per.second`, `game.maximum.updates.per.frame`, `game.vertical.synchronization`
- **Timing híbrido**: Software para updates (CPU), hardware para renders (GPU/monitor)
- **Variable extraída**: `updatesPerSecond` evita repetición y mejora legibilidad

### Ventajas de la Protección Completa

- ✅ **Nunca se congela**: Aplicación siempre responde incluso en hardware muy lento
- ✅ **Física determinística**: Updates fijos permiten simulaciones predecibles
- ✅ **Networking compatible**: UPS fijo facilita sincronización entre clientes
- ✅ **Renders suaves**: VSync elimina tearing y aprovecha monitor de alta frecuencia
- ✅ **Degradación elegante**: En lugar de crash, funciona con precisión reducida
- ✅ **Balance automático**: Entre precisión física y fluidez visual
- ✅ **Hardware tolerante**: Funciona desde PCs lentos hasta potentes
- ✅ **Interpolación posible**: Alpha permite suavizado entre estados de física
- ✅ **UPS/FPS desacoplados**: 60 UPS fijo puede coexistir con 144 FPS

### El Problema del Spiral of Death

El **Spiral of Death** ocurre cuando:
1. Un update tarda más tiempo que el timestep fijo (ej: 16.6ms tarda 20ms)
2. El acumulador crece más rápido de lo que puede procesarse
3. El loop intenta hacer catch-up infinito
4. Cada update adicional hace el sistema aún más lento
5. La aplicación se congela ejecutando solo updates, nunca renders

**Solución en v0.3.7:**
```
Frame 1: deltaTime=8.5 → ejecuta 5 updates → deltaTime=3.5 → LÍMITE → render
Frame 2: deltaTime=5.8 → ejecuta 5 updates → deltaTime=0.8 → LÍMITE → render
Frame 3: deltaTime=1.2 → ejecuta 1 update  → deltaTime=0.2 → render normal
```

### Comportamiento en Diferentes Escenarios

**Hardware rápido (144 FPS, 60 UPS):**
```
Updates por frame: 0-1
Límite: Nunca alcanzado
Funcionamiento: Óptimo
```

**Hardware normal (60 FPS, 60 UPS):**
```
Updates por frame: 1
Límite: Nunca alcanzado
Funcionamiento: Óptimo
```

**Hardware lento (20 FPS, 60 UPS):**
```
Updates por frame: 3
Límite: Ocasionalmente alcanzado
Funcionamiento: Aceptable con catch-up
```

**Hardware muy lento (10 FPS, 60 UPS):**
```
Updates por frame: 5 (límite)
Física: Pierde precisión temporal
Aplicación: Responde, no se congela
Funcionamiento: Degradado pero estable
```

### Comparación con Versiones Previas

| Característica | v0.3.4 (Fixed) | v0.3.5 (VSync) | v0.3.6 (Híbrido) | v0.3.7 (Completo) |
|----------------|----------------|----------------|------------------|-------------------|
| **Updates** | Fijos (60 UPS) | Variables (FPS) | Fijos (60 UPS) | **Fijos (60 UPS)** |
| **Renders** | Variables (sleep) | Fijos (VSync) | Fijos (VSync) | **Fijos (VSync)** |
| **Física** | Determinística | Frame-dependent | Determinística | **Determinística** |
| **Screen tearing** | Posible | Prevenido | Prevenido | **Prevenido** |
| **Catch-up limit** | 5 updates máx | N/A | Sin límite | **5 updates máx** |
| **Control flujo** | No | No | No | **Sí (running)** |
| **Spiral protection** | Sí | N/A | No | **Sí** |
| **Robustez** | Alta | Media | Media | **Máxima** |
| **Complejidad** | Alta | Baja | Media | **Alta** |
| **Networking** | Ideal | Difícil | Ideal | **Ideal** |

### Cuándo Usar Esta Implementación

**Fixed + VSync + Protection (v0.3.7) - Recomendado para:**
- ✅ **Juegos multijugador** que necesitan física determinística
- ✅ **Simulaciones complejas** con VSync activo
- ✅ **Hardware variable** desde PCs lentos a potentes
- ✅ **Aplicaciones críticas** que no pueden congelarse
- ✅ **Proyectos profesionales** que requieren máxima robustez
- ✅ **Juegos competitivos** con monitores de alta frecuencia
- ✅ **Motores de juego educativos** mostrando best practices

**Ventajas específicas sobre v0.3.6:**
- Protección contra congelamiento en hardware muy lento
- Control de flujo explícito con `running`
- Métodos de ciclo de vida claramente definidos
- Configuración de límite de spiral of death

**Ventajas específicas sobre v0.3.4:**
- Eliminación total de screen tearing (VSync)
- No requiere Thread.sleep ni busy-waiting
- Aprovecha hardware de GPU/monitor para timing de render
- Menor consumo de CPU durante esperas

### Mejoras de Legibilidad Aplicadas

1. **Variable `updatesPerSecond` extraída**: Evita repetición de `GameSettings.GAME_UPDATES_PER_SECOND.get(FRAMERATE)` usada en múltiples cálculos
2. **Expresión del acumulador simplificada**: Eliminados paréntesis externos redundantes - `((currentTime - previousTime) / updateTime)` → `(currentTime - previousTime) / updateTime`

> **Nota sobre casts**: Los casts explícitos como `((boolean) ...)` se mantienen intencionalmente para prevenir errores en tiempo de ejecución cuando `Configuration.get()` no recibe valor por defecto, garantizando type safety.

### Ejemplo de Salida

```
Updates Per Second (UPS): 60
Frames Per Second (FPS): 60   (Monitor 60Hz)
Updates Per Second (UPS): 60
Frames Per Second (FPS): 144  (Monitor 144Hz)
```

> **Nota**: Esta versión combina Fixed Timestep (v0.3.4), VSync (v0.3.5), y Spiral of Death Protection (v0.3.4). Versión robusta ideal para proyectos que requieren física determinística, calidad visual perfecta, y funcionamiento en hardware variable.

#### v0.4.2 - Tracking de Posición del Cursor (Implementación Actual)

La versión 0.4.2 extiende el sistema de input de ratón de v0.4.1, añadiendo **tracking de la posición del cursor**. Ahora es posible conocer las coordenadas exactas del cursor en todo momento:

##### Sistema de Tracking de Posición

**Componentes principales**:
```
MouseEventHandler (Singleton)
├─> boolean[] mouseButtonPressed         // Estado de todos los botones (v0.4.1)
├─> double cursorPositionX               // Posición X del cursor (v0.4.2) ⭐
├─> double cursorPositionY               // Posición Y del cursor (v0.4.2) ⭐
├─> GLFWMouseButtonCallback callback     // Callback de botones
├─> GLFWCursorPosCallback callback       // Callback de posición (v0.4.2) ⭐
├─> getCursorPositionX()                 // Obtener X (v0.4.2) ⭐
├─> getCursorPositionY()                 // Obtener Y (v0.4.2) ⭐
├─> setCursorPositionX(x)                // Actualizar X (v0.4.2) ⭐
└─> setCursorPositionY(y)                // Actualizar Y (v0.4.2) ⭐

CursorPosCallback (GLFWCursorPosCallback) (v0.4.2) ⭐
└─> invoke(window, xPos, yPos)           // Procesa eventos de movimiento
    ├─> MouseEventHandler.setCursorPositionX(xPos)
    └─> MouseEventHandler.setCursorPositionY(yPos)
```

##### Implementación

**CursorPosCallback (v0.4.2)**:
```java
package es.noa.rad.game.engine.event.callback;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import es.noa.rad.game.engine.event.MouseEventHandler;

public final class CursorPosCallback extends GLFWCursorPosCallback {
  
  @Override
  public void invoke(
      final long _window,
      final double _xPosition,
      final double _yPosition) {
    MouseEventHandler.get().setCursorPositionX(_xPosition);
    MouseEventHandler.get().setCursorPositionY(_yPosition);
  }
}
```

**MouseEventHandler actualizado (v0.4.2)**:
```java
public final class MouseEventHandler {
  
  private static MouseEventHandler instance = null;
  
  private final GLFWCursorPosCallback glfwCursorPosCallback;      // ⭐ NUEVO
  private final GLFWMouseButtonCallback glfwMouseButtonCallback;
  
  private double cursorPositionX;  // ⭐ NUEVO
  private double cursorPositionY;  // ⭐ NUEVO
  private final boolean[] mouseButtonPressed;
  
  private MouseEventHandler() {
    this.cursorPositionX = 0.0d;   // ⭐ NUEVO
    this.cursorPositionY = 0.0d;   // ⭐ NUEVO
    this.mouseButtonPressed = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST];
    this.glfwCursorPosCallback = new CursorPosCallback();        // ⭐ NUEVO
    this.glfwMouseButtonCallback = new MouseButtonCallback();
  }
  
  // Getters y Setters de posición (v0.4.2) ⭐
  public double getCursorPositionX() {
    return this.cursorPositionX;
  }
  
  public void setCursorPositionX(final double _cursorPositionX) {
    this.cursorPositionX = _cursorPositionX;
  }
  
  public double getCursorPositionY() {
    return this.cursorPositionY;
  }
  
  public void setCursorPositionY(final double _cursorPositionY) {
    this.cursorPositionY = _cursorPositionY;
  }
  
  public GLFWCursorPosCallback getGlfwCursorPosCallback() {  // ⭐ NUEVO
    return this.glfwCursorPosCallback;
  }
  
  public void close() {
    this.glfwCursorPosCallback.free();     // ⭐ NUEVO
    this.glfwMouseButtonCallback.free();
  }
  
  // ... resto de métodos de v0.4.1 ...
}
```

**Window.init() actualizado (v0.4.2)**:
```java
public void init() {
  // ... código anterior ...
  
  GLFW.glfwSetKeyCallback(
    this.glfwWindow,
    KeyboardEventHandler.get().getGlfwKeyCallback()
  );
  GLFW.glfwSetCursorPosCallback(                            // ⭐ NUEVO
    this.glfwWindow,
    MouseEventHandler.get().getGlfwCursorPosCallback()
  );
  GLFW.glfwSetMouseButtonCallback(
    this.glfwWindow,
    MouseEventHandler.get().getGlfwMouseButtonCallback()
  );
  
  GLFW.glfwShowWindow(this.glfwWindow);
}
```

##### Ejemplos de Uso

**1. Detectar clic y mostrar coordenadas:**
```java
public void input() {
  final MouseEventHandler mouse = MouseEventHandler.get();
  
  if (mouse.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
    System.out.printf(
      "(x: %.0f, y: %.0f)%n",
      mouse.getCursorPositionX(),
      mouse.getCursorPositionY()
    );
  }
}
```

**2. Detectar cursor en una región:**
```java
public void input() {
  final MouseEventHandler mouse = MouseEventHandler.get();
  
  double x = mouse.getCursorPositionX();
  double y = mouse.getCursorPositionY();
  
  // Detectar si el cursor está sobre un botón UI
  if (x >= 100 && x <= 300 && y >= 50 && y <= 100) {
    // Cursor sobre botón (100, 50) - (300, 100)
    if (mouse.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
      System.out.println("¡Botón clickeado!");
    }
  }
}
```

**3. Tracking continuo de posición:**
```java
public void input() {
  final MouseEventHandler mouse = MouseEventHandler.get();
  
  // Mostrar posición en todo momento (útil para debug)
  System.out.printf(
    "Cursor: (%.0f, %.0f)%n",
    mouse.getCursorPositionX(),
    mouse.getCursorPositionY()
  );
  
  // O almacenar para usar en update()
  this.lastMouseX = mouse.getCursorPositionX();
  this.lastMouseY = mouse.getCursorPositionY();
}
```

##### Características del Sistema

| Característica | Descripción |
|---------------|-------------|
| **Precisión** | `double` (64-bit) para coordenadas exactas |
| **Sistema de coordenadas** | Origen (0,0) en esquina superior izquierda |
| **Dirección X** | Aumenta hacia la derecha |
| **Dirección Y** | Aumenta hacia abajo |
| **Unidades** | Píxeles de la ventana |
| **Actualización** | Automática con cada movimiento del cursor |
| **Rendimiento** | Sin overhead, actualización directa |
| **Thread-safety** | GLFW garantiza llamadas en main thread |

##### Flujo de Eventos (v0.4.2)

```
┌──────────────────────────────────────────────┐
│  Usuario mueve el cursor dentro de la ventana │
└────────────────┬─────────────────────────────┘
                 ▼
┌──────────────────────────────────────────────┐
│  GLFW detecta el movimiento                  │
│  glfwPollEvents() procesa eventos            │
└────────────────┬─────────────────────────────┘
                 ▼
┌──────────────────────────────────────────────┐
│  CursorPosCallback.invoke(window, x, y)      │
│  Callback registrado con glfwSetCursorPos... │
└────────────────┬─────────────────────────────┘
                 ▼
┌──────────────────────────────────────────────┐
│  MouseEventHandler.setCursorPositionX(x)     │
│  MouseEventHandler.setCursorPositionY(y)     │
│  Almacena las coordenadas                    │
└────────────────┬─────────────────────────────┘
                 ▼
┌──────────────────────────────────────────────┐
│  Window.input() o cualquier método de juego  │
│  Consulta: getCursorPositionX/Y()            │
│  Usa las coordenadas para lógica de juego    │
└──────────────────────────────────────────────┘
```

##### Integración con Sistema de Botones (v0.4.1)

El tracking de posición complementa perfectamente el sistema de botones:

```java
public void input() {
  final MouseEventHandler mouse = MouseEventHandler.get();
  
  // Sistema combinado: botones + posición
  if (mouse.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
    // Botón izquierdo presionado (v0.4.1)
    double x = mouse.getCursorPositionX();  // Posición (v0.4.2)
    double y = mouse.getCursorPositionY();  // Posición (v0.4.2)
    
    // Lógica de juego: pintar, seleccionar, etc.
    handleClick(x, y);
  }
}
```

##### Mejoras Futuras Planificadas

Las siguientes características se implementarán en versiones posteriores:

| Mejora | Versión Planificada | Descripción |
|--------|---------------------|-------------|
| **Delta tracking** | v0.4.3+ | Movimiento relativo frame a frame |
| **firstMouseMove flag** | v0.4.3+ | Evitar salto inicial al entrar |
| **resetDeltas()** | v0.4.3+ | Limpiar deltas cada frame |
| **Cursor capture mode** | v0.5.0+ | Para juegos FPS (cursor oculto) |
| **Epsilon filtering** | v0.5.0+ | Filtrar ruido de hardware |
| **Scroll wheel** | v0.5.0+ | Eventos de scroll horizontal/vertical |

#### v0.4.1 - Sistema de Input de Ratón (Implementación Actual)

La versión 0.4.1 añade el **sistema de manejo de input de ratón**, complementando el sistema de teclado de v0.4.0. Incluye también un nuevo callback `inputCallback` en `GameTiming` para separar input de update/render:

##### Nuevo Sistema de Input de Ratón

**Componentes principales**:
```
MouseEventHandler (Singleton)
├─> boolean[] mouseButtonPressed      // Estado de todos los botones
├─> GLFWMouseButtonCallback callback  // Callback de GLFW
├─> isMouseButtonPressed(buttonCode)  // Consultar estado
└─> setMouseButtonPressed(code, bool) // Actualizar estado

MouseButtonCallback (GLFWMouseButtonCallback)
└─> invoke()                          // Procesa eventos de GLFW
    └─> MouseEventHandler.setMouseButtonPressed()
```

##### Implementación

**MouseEventHandler (Singleton)**:
```java
package es.noa.rad.game.engine.event;

public final class MouseEventHandler {
    
    private static MouseEventHandler instance = null;
    private final GLFWMouseButtonCallback glfwMouseButtonCallback;
    private final boolean[] mouseButtonPressed;
    
    private MouseEventHandler() {
        this.mouseButtonPressed = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST];
        this.glfwMouseButtonCallback = new MouseButtonCallback();
    }
    
    public static MouseEventHandler get() {
        if (instance == null) {
            createInstance();
        }
        return instance;
    }
    
    /**
     * Verifica si un botón del ratón está presionado.
     * 
     * @param _buttonCode Código de botón GLFW (ej: GLFW_MOUSE_BUTTON_LEFT)
     * @return true si está presionado, false en caso contrario
     */
    public boolean isMouseButtonPressed(final int _buttonCode) {
        if ((_buttonCode >= 0) && (_buttonCode < GLFW.GLFW_MOUSE_BUTTON_LAST)) {
            return this.mouseButtonPressed[_buttonCode];
        }
        return false;
    }
    
    /**
     * Actualiza el estado de un botón del ratón.
     * Llamado internamente por MouseButtonCallback.
     */
    public void setMouseButtonPressed(final int _buttonCode, final boolean _buttonStatus) {
        if ((_buttonCode >= 0) && (_buttonCode < GLFW.GLFW_MOUSE_BUTTON_LAST)) {
            this.mouseButtonPressed[_buttonCode] = _buttonStatus;
        }
    }
    
    public GLFWMouseButtonCallback getGlfwMouseButtonCallback() {
        return this.glfwMouseButtonCallback;
    }
    
    public void close() {
        this.glfwMouseButtonCallback.free();
    }
}
```

**MouseButtonCallback (Procesa eventos de GLFW)**:
```java
package es.noa.rad.game.engine.event.callback;

public final class MouseButtonCallback extends GLFWMouseButtonCallback {
    
    @Override
    public void invoke(
        final long _window,
        final int _button,
        final int _action,
        final int _modifier) {
        
        // Actualizar estado: presionado si action != RELEASE
        MouseEventHandler.get()
            .setMouseButtonPressed(_button, (_action != GLFW.GLFW_RELEASE));
    }
}
```

**Integración en Window.init()**:
```java
// Registrar callback de botones del ratón en GLFW
GLFW.glfwSetMouseButtonCallback(
    this.glfwWindow,
    MouseEventHandler.get().getGlfwMouseButtonCallback()
);
```

**Cleanup en Window.close()**:
```java
// Liberar recursos de callbacks
KeyboardEventHandler.get().close();
MouseEventHandler.get().close();
GLFW.glfwDestroyWindow(this.glfwWindow);
GLFW.glfwTerminate();
```

##### Nuevo Callback de Input en GameTiming

**inputCallback con Runnable**:
```java
// En GameTiming.java
private Runnable inputCallback;

public void inputCallback(final Runnable _inputCallback) {
    this.inputCallback = _inputCallback;
}

private void input() {
    if (this.inputCallback != null) {
        this.inputCallback.run();
    } else {
        Window.get().input();  // Fallback
    }
}
```

**Configuración en Application.init()**:
```java
// Configurar callback de input (más idiomático con Runnable)
GameTiming.get().inputCallback(() -> Window.get().input());
```

**Método Window.input() centralizado**:
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

##### Flujo del Game Loop con Input

```
┌─────────────────────────────────────────────┐
│  GameTiming.tick()                          │
│  ┌───────────────────────────────────────┐  │
│  │ 1. input()      ← NUEVO: inputCallback│  │
│  │    └─> Window.input()                 │  │
│  │         ├─> Keyboard.isKeyPressed()   │  │
│  │         └─> Mouse.isMouseButtonPressed│  │
│  │                                        │  │
│  │ 2. while (deltaTime >= 1.0)           │  │
│  │      update(fixedDeltaTime)           │  │
│  │      deltaTime--                       │  │
│  │                                        │  │
│  │ 3. render(alpha)                      │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

##### Ejemplos de Uso

**Ejemplo 1: Detección de botones del ratón**:
```java
public void input() {
    // Botón izquierdo - Acción primaria
    if (MouseEventHandler.get().isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
        player.shoot();
        // o
        ui.selectObject();
    }
    
    // Botón derecho - Acción secundaria
    if (MouseEventHandler.get().isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
        player.aim();
        // o
        ui.showContextMenu();
    }
    
    // Botón central - Acción terciaria
    if (MouseEventHandler.get().isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_MIDDLE)) {
        camera.toggleMode();
    }
}
```

**Ejemplo 2: Combinación teclado + ratón**:
```java
public void input() {
    // Control de cámara con WASD
    if (KeyboardEventHandler.get().isKeyPressed(GLFW.GLFW_KEY_W)) {
        camera.moveForward();
    }
    
    // Disparo con clic izquierdo
    if (MouseEventHandler.get().isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
        weapon.fire();
    }
    
    // Sprint + disparo = disparo especial
    if (KeyboardEventHandler.get().isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT)
        && MouseEventHandler.get().isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
        weapon.fireSpecial();
    }
}
```

**Ejemplo 3: Detección de múltiples botones**:
```java
// Acción solo si ambos botones están presionados
if (MouseEventHandler.get().isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)
    && MouseEventHandler.get().isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
    player.performSpecialAction();
}
```

##### Características

| Característica | Descripción |
|----------------|-------------|
| **Patrón Singleton** | `MouseEventHandler.get()` acceso global |
| **Eficiencia** | Array booleano O(1) para consultas |
| **Validación robusta** | Verifica límites superior e inferior (>= 0 y < GLFW_MOUSE_BUTTON_LAST) |
| **Integración GLFW** | Callback registrado automáticamente en Window |
| **Cleanup automático** | Liberación de recursos en Window.close() |
| **Callback `inputCallback`** | Usa `Runnable` (más idiomático que `Consumer<Void>`) |
| **Separación de fases** | input → update → render claramente diferenciadas |
| **Thread-safe** | Singleton con createInstance() sincronizado |
| **Consistencia** | Mismo patrón que KeyboardEventHandler |

##### Códigos de Botón GLFW Comunes

```java
// Botones del ratón
GLFW.GLFW_MOUSE_BUTTON_LEFT    // 0 - Botón izquierdo
GLFW.GLFW_MOUSE_BUTTON_RIGHT   // 1 - Botón derecho
GLFW.GLFW_MOUSE_BUTTON_MIDDLE  // 2 - Botón central (rueda)
GLFW.GLFW_MOUSE_BUTTON_4       // 3 - Botón lateral 1
GLFW.GLFW_MOUSE_BUTTON_5       // 4 - Botón lateral 2
GLFW.GLFW_MOUSE_BUTTON_6       // 5 - Botón lateral 3
GLFW.GLFW_MOUSE_BUTTON_7       // 6 - Botón lateral 4
GLFW.GLFW_MOUSE_BUTTON_8       // 7 - Botón lateral 5

// Límite
GLFW.GLFW_MOUSE_BUTTON_LAST    // 8 - Total de botones soportados
```

##### Arquitectura del Sistema de Input

```
┌──────────────────────────────────────────────────┐
│  Usuario interactúa (teclado/ratón)             │
└────────────────────┬─────────────────────────────┘
                     ↓
┌──────────────────────────────────────────────────┐
│  GLFW Window Callbacks                           │
│  ├─> glfwSetKeyCallback()                        │
│  └─> glfwSetMouseButtonCallback()                │
└────────────────────┬─────────────────────────────┘
                     ↓
┌──────────────────────────────────────────────────┐
│  Event Callbacks                                 │
│  ├─> KeyCallback.invoke()                        │
│  └─> MouseButtonCallback.invoke()                │
└────────────────────┬─────────────────────────────┘
                     ↓
┌──────────────────────────────────────────────────┐
│  Event Handlers (Singletons)                     │
│  ├─> KeyboardEventHandler                        │
│  │    └─> boolean[] keyPressed                   │
│  └─> MouseEventHandler                           │
│       └─> boolean[] mouseButtonPressed           │
└────────────────────┬─────────────────────────────┘
                     ↓
┌──────────────────────────────────────────────────┐
│  GameTiming.input() → inputCallback              │
│  └─> Window.input()                              │
│       ├─> KeyboardEventHandler.isKeyPressed()    │
│       └─> MouseEventHandler.isMouseButtonPressed()│
└──────────────────────────────────────────────────┘
```

##### Validaciones de Seguridad

**Prevención de ArrayIndexOutOfBoundsException**:
```java
// Validación completa de límites
public boolean isMouseButtonPressed(final int _buttonCode) {
    if ((_buttonCode >= 0) && (_buttonCode < GLFW.GLFW_MOUSE_BUTTON_LAST)) {
        return this.mouseButtonPressed[_buttonCode];  // Seguro
    }
    return false;  // ButtonCode inválido
}
```

##### Mejoras sobre v0.4.0

✅ **Sistema de input de ratón**: Complementa el teclado  
✅ **Callback `inputCallback`**: Separación clara de input/update/render  
✅ **Tipo `Runnable`**: Más idiomático que `Consumer<Void>`  
✅ **Validación robusta**: Límites inferior y superior en ambos handlers  
✅ **Consistencia arquitectónica**: Mismo patrón para teclado y ratón  
✅ **Método `Window.input()`**: Centraliza toda la lógica de input  

##### Mejoras Futuras (Próximas Versiones)

⏳ **Posición del cursor**: mouseX, mouseY, deltaX, deltaY  
⏳ **Scroll wheel**: Detección de scroll vertical y horizontal  
⏳ **Modo captura de cursor**: Para cámaras FPS (GLFW_CURSOR_DISABLED)  
⏳ **Detección "just pressed"**: Diferenciar pressed vs held  
⏳ **Reset al perder foco**: Limpiar estado cuando ventana pierde foco  
⏳ **Constantes para botones**: Wrapper para códigos comunes  
⏳ **Gamepad support**: Controladores y joysticks  

> **Nota**: Esta versión establece un sistema de input completo para teclado y ratón (botones). La separación input → update → render mejora la organización del código. Sistema simple, eficiente y extensible. Ideal para juegos 2D/3D básicos que requieren control de teclado y clics del ratón.

#### v0.4.0 - Sistema de Input de Teclado

La versión 0.4.0 introduce el **sistema de manejo de input de teclado**, permitiendo detectar cuando las teclas están presionadas. Implementación simple y eficiente usando GLFW callbacks:

##### Nuevo Sistema de Input

**Componentes principales**:
```
KeyboardEventHandler (Singleton)
├─> boolean[] keyPressed         // Estado de todas las teclas
├─> GLFWKeyCallback callback     // Callback de GLFW
├─> isKeyPressed(keyCode)        // Consultar estado
└─> setKeyPressed(keyCode, bool) // Actualizar estado

KeyCallback (GLFWKeyCallback)
└─> invoke()                     // Procesa eventos de GLFW
    └─> KeyboardEventHandler.setKeyPressed()
```

##### Implementación

**KeyboardEventHandler (Singleton)**:
```java
package es.noa.rad.game.engine.event;

public final class KeyboardEventHandler {
    
    private static KeyboardEventHandler instance = null;
    private final GLFWKeyCallback glfwKeyCallback;
    private final boolean[] keyPressed;
    
    private KeyboardEventHandler() {
        this.keyPressed = new boolean[GLFW.GLFW_KEY_LAST];
        this.glfwKeyCallback = new KeyCallback();
    }
    
    public static KeyboardEventHandler get() {
        if (instance == null) {
            createInstance();
        }
        return instance;
    }
    
    /**
     * Verifica si una tecla está presionada.
     * 
     * @param _keyCode Código de tecla GLFW (ej: GLFW_KEY_W)
     * @return true si está presionada, false en caso contrario
     */
    public boolean isKeyPressed(final int _keyCode) {
        if ((_keyCode >= 0) && (_keyCode < GLFW.GLFW_KEY_LAST)) {
            return this.keyPressed[_keyCode];
        }
        return false;
    }
    
    /**
     * Actualiza el estado de una tecla.
     * Llamado internamente por KeyCallback.
     */
    public void setKeyPressed(final int _keyCode, final boolean _keyStatus) {
        if ((_keyCode >= 0) && (_keyCode < GLFW.GLFW_KEY_LAST)) {
            this.keyPressed[_keyCode] = _keyStatus;
        }
    }
    
    public GLFWKeyCallback getGlfwKeyCallback() {
        return this.glfwKeyCallback;
    }
    
    public void close() {
        this.glfwKeyCallback.free();  // Liberar recursos
    }
}
```

**KeyCallback (Procesa eventos de GLFW)**:
```java
package es.noa.rad.game.engine.event.callback;

public final class KeyCallback extends GLFWKeyCallback {
    
    @Override
    public void invoke(
        final long _window,
        final int _keyCode,
        final int _scanCode,
        final int _action,
        final int _modifier) {
        
        if (_keyCode != GLFW.GLFW_KEY_UNKNOWN) {
            // Actualizar estado: presionada si action != RELEASE
            KeyboardEventHandler.get()
                .setKeyPressed(_keyCode, (_action != GLFW.GLFW_RELEASE));
        }
    }
}
```

**Integración en Window.init()**:
```java
// Registrar callback de teclado en GLFW
GLFW.glfwSetKeyCallback(
    this.glfwWindow,
    KeyboardEventHandler.get().getGlfwKeyCallback()
);
```

**Cleanup en Window.close()**:
```java
// Liberar recursos del callback
KeyboardEventHandler.get().close();
GLFW.glfwDestroyWindow(this.glfwWindow);
GLFW.glfwTerminate();
```

##### Funcionalidad ESC para Salir

**Application.run() con control de ESC**:
```java
private boolean running;

private void start() {
    this.running = true;
}

@Override
public void run() {
    this.init();
    
    while ((this.running)
        && (!Window.get().shouldClose())
        && (GameTiming.get().tick())) {
        
        // Cerrar aplicación con tecla ESC
        if (KeyboardEventHandler.get().isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
            this.running = false;
        }
        
        Window.get().swapBuffers();
    }
    
    this.close();
    this.stop();
}
```

##### Ejemplos de Uso

**Ejemplo 1: Movimiento básico (WASD)**:
```java
public void update(final float _deltaTime) {
    // Movimiento hacia adelante/atrás
    if (KeyboardEventHandler.get().isKeyPressed(GLFW.GLFW_KEY_W)) {
        player.moveForward(_deltaTime);
    }
    if (KeyboardEventHandler.get().isKeyPressed(GLFW.GLFW_KEY_S)) {
        player.moveBackward(_deltaTime);
    }
    
    // Movimiento lateral
    if (KeyboardEventHandler.get().isKeyPressed(GLFW.GLFW_KEY_A)) {
        player.moveLeft(_deltaTime);
    }
    if (KeyboardEventHandler.get().isKeyPressed(GLFW.GLFW_KEY_D)) {
        player.moveRight(_deltaTime);
    }
    
    // Saltar
    if (KeyboardEventHandler.get().isKeyPressed(GLFW.GLFW_KEY_SPACE)) {
        player.jump();
    }
}
```

**Ejemplo 2: Múltiples teclas simultáneas**:
```java
// Correr (Shift + W)
if (KeyboardEventHandler.get().isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT)
    && KeyboardEventHandler.get().isKeyPressed(GLFW.GLFW_KEY_W)) {
    player.run(_deltaTime);
} else if (KeyboardEventHandler.get().isKeyPressed(GLFW.GLFW_KEY_W)) {
    player.walk(_deltaTime);
}
```

**Ejemplo 3: Teclas de acción**:
```java
if (KeyboardEventHandler.get().isKeyPressed(GLFW.GLFW_KEY_E)) {
    player.interact();
}

if (KeyboardEventHandler.get().isKeyPressed(GLFW.GLFW_KEY_R)) {
    player.reload();
}

if (KeyboardEventHandler.get().isKeyPressed(GLFW.GLFW_KEY_TAB)) {
    ui.toggleInventory();
}
```

##### Características

| Característica | Descripción |
|----------------|-------------|
| **Patrón Singleton** | `KeyboardEventHandler.get()` acceso global |
| **Eficiencia** | Array booleano O(1) para consultas |
| **Validación robusta** | Verifica límites superior e inferior (>= 0 y < GLFW_KEY_LAST) |
| **Integración GLFW** | Callback registrado automáticamente en Window |
| **Cleanup automático** | Liberación de recursos en Window.close() |
| **ESC para salir** | Funcionalidad estándar implementada |
| **Thread-safe** | Singleton con createInstance() sincronizado |

##### Arquitectura del Sistema

```
┌──────────────────────────────────────────────────┐
│  Usuario presiona tecla                          │
└────────────────────┬─────────────────────────────┘
                     ↓
┌──────────────────────────────────────────────────┐
│  GLFW Window                                     │
│  glfwSetKeyCallback()                            │
└────────────────────┬─────────────────────────────┘
                     ↓
┌──────────────────────────────────────────────────┐
│  KeyCallback.invoke()                            │
│  ┌────────────────────────────────────────────┐  │
│  │ if (keyCode != GLFW_KEY_UNKNOWN)           │  │
│  │   KeyboardEventHandler.setKeyPressed(...)  │  │
│  └────────────────────────────────────────────┘  │
└────────────────────┬─────────────────────────────┘
                     ↓
┌──────────────────────────────────────────────────┐
│  KeyboardEventHandler (Singleton)                │
│  ┌────────────────────────────────────────────┐  │
│  │ boolean[] keyPressed                       │  │
│  │ keyPressed[keyCode] = (action != RELEASE)  │  │
│  └────────────────────────────────────────────┘  │
└────────────────────┬─────────────────────────────┘
                     ↓
┌──────────────────────────────────────────────────┐
│  Application / Game Logic                        │
│  ┌────────────────────────────────────────────┐  │
│  │ if (isKeyPressed(GLFW_KEY_W))              │  │
│  │   player.move()                            │  │
│  └────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────┘
```

##### Códigos de Tecla GLFW Comunes

```java
// Letras
GLFW.GLFW_KEY_W        // W
GLFW.GLFW_KEY_A        // A
GLFW.GLFW_KEY_S        // S
GLFW.GLFW_KEY_D        // D

// Funciones
GLFW.GLFW_KEY_SPACE    // Espacio
GLFW.GLFW_KEY_ESCAPE   // ESC
GLFW.GLFW_KEY_ENTER    // Enter
GLFW.GLFW_KEY_TAB      // Tab

// Modificadores
GLFW.GLFW_KEY_LEFT_SHIFT   // Shift izquierdo
GLFW.GLFW_KEY_LEFT_CONTROL // Ctrl izquierdo
GLFW.GLFW_KEY_LEFT_ALT     // Alt izquierdo

// Flechas
GLFW.GLFW_KEY_UP       // Flecha arriba
GLFW.GLFW_KEY_DOWN     // Flecha abajo
GLFW.GLFW_KEY_LEFT     // Flecha izquierda
GLFW.GLFW_KEY_RIGHT    // Flecha derecha

// Números
GLFW.GLFW_KEY_1 ... GLFW.GLFW_KEY_9  // 1-9
GLFW.GLFW_KEY_0                       // 0
```

##### Validaciones de Seguridad

**Prevención de ArrayIndexOutOfBoundsException**:
```java
// Validación completa de límites
public boolean isKeyPressed(final int _keyCode) {
    if ((_keyCode >= 0) && (_keyCode < GLFW.GLFW_KEY_LAST)) {
        return this.keyPressed[_keyCode];  // Seguro
    }
    return false;  // KeyCode inválido
}
```

**Filtrado de teclas desconocidas**:
```java
// En KeyCallback.invoke()
if (_keyCode != GLFW.GLFW_KEY_UNKNOWN) {
    // Solo procesar teclas válidas
    KeyboardEventHandler.get().setKeyPressed(_keyCode, ...);
}
```

##### Mejoras Futuras (Próximas Versiones)

⏳ **Detección de "just pressed"**: Diferenciar presionado vs mantenido  
⏳ **Soporte de modificadores**: APIs para Shift/Ctrl/Alt  
⏳ **Reset al perder foco**: Limpiar estado cuando ventana pierde foco  
⏳ **Constantes de teclas**: Wrapper para códigos comunes  
⏳ **Input de mouse**: Sistema similar para ratón  
⏳ **Gamepad support**: Controladores y joysticks  

> **Nota**: Esta versión establece la base del sistema de input con una implementación simple, eficiente y robusta. Funcionalidad mínima pero completa para control de teclado básico. Ideal para aprender los fundamentos del manejo de input en game engines.

#### v0.3.10 - Configuración Centralizada con Tres Niveles de Fallback

La versión 0.3.10 mejora el sistema de configuración eliminando **toda la duplicación de constantes** y centralizando los valores por defecto en las enumeraciones con un sistema de **tres niveles de fallback**:

##### Problema Resuelto

**Antes (v0.3.9)**: Constantes duplicadas en múltiples clases
```java
// GameTiming.java
public static final double FRAMERATE = 60.0D;
public static final int MAXIMUM_UPDATES_PER_FRAME = 5;
public static final float MAXIMUM_ACCUMULATED_TIME = 0.5F;

// Application.java
public static final int WIDTH = 1280;
public static final int HEIGHT = 720;

// Uso con duplicación
final double ups = GameSettings.GAME_UPDATES_PER_SECOND
  .get(GameTiming.FRAMERATE);  // ¡Default hardcoded!
```

**Después (v0.3.10)**: Valores centralizados en enumeraciones
```java
// GameSettings.java - Un solo lugar
GAME_UPDATES_PER_SECOND("game.updates.per.second", Double.class, 60.0D),
GAME_MAXIMUM_UPDATES_PER_FRAME("game.maximum.updates.per.frame", Integer.class, 5),
GAME_MAXIMUM_ACCUMULATED_TIME("game.maximum.accumulated.time", Float.class, 0.5F),
GAME_VERTICAL_SYNCHRONIZATION("game.vertical.synchronization", Boolean.class, true)

// WindowSettings.java - Un solo lugar
WINDOW_WIDTH("window.width", Integer.class, 1280),
WINDOW_HEIGHT("window.height", Integer.class, 720),
WINDOW_TITLE("window.title", String.class, "3D Game Engine")

// Uso sin duplicación
final double ups = GameSettings.GAME_UPDATES_PER_SECOND.get();  // ¡Simple!
```

##### Sistema de Tres Niveles de Fallback

El nuevo sistema garantiza que **siempre hay un valor válido** disponible:

```
┌─────────────────────────────────────────────┐
│  Nivel 1: Archivo properties                │
│  ┌───────────────────────────────────────┐  │
│  │ game.updates.per.second=120.0         │  │ ← PRIORIDAD MÁXIMA
│  │ (Si existe y es válido, se usa este)  │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
              ↓ (si no existe)
┌─────────────────────────────────────────────┐
│  Nivel 2: Parámetro en get()                │
│  ┌───────────────────────────────────────┐  │
│  │ .get(30.0)                            │  │ ← PRIORIDAD MEDIA
│  │ (Si se pasa, se usa como fallback)    │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
              ↓ (si es null)
┌─────────────────────────────────────────────┐
│  Nivel 3: Default en Enumeración            │
│  ┌───────────────────────────────────────┐  │
│  │ GAME_UPDATES_PER_SECOND(..., 60.0D)  │  │ ← SIEMPRE DISPONIBLE
│  │ (Garantiza valor válido)              │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

##### Implementación del Sistema

**Enumeración con valores por defecto**:
```java
public enum GameSettings {
  GAME_UPDATES_PER_SECOND(
    "game.updates.per.second",  // Property key
    Double.class,                // Type
    60.0D                        // ← Built-in default (Nivel 3)
  ),
  // ... más configuraciones
  
  private final String property;
  private final Class<?> classType;
  private final Object defaultValue;  // ← Nuevo campo
  
  GameSettings(String _property, Class<?> _classType, Object _defaultValue) {
    this.property = _property;
    this.classType = _classType;
    this.defaultValue = _defaultValue;
  }
}
```

**Método get() sin parámetros** (Nivel 1 → Nivel 3):
```java
public <T> T get() {
  return (T) Configuration.get()
    .property(
      this.property,
      (Class<T>) this.classType,
      (T) this.defaultValue  // Usa default de enum si no hay en properties
    );
}
```

**Método get(T) con parámetro** (Nivel 1 → Nivel 2 → Nivel 3):
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
      propertyValue                          // Nivel 1: Properties file
    );
}
```

##### Ejemplos de Uso

**Ejemplo 1: Sin properties, sin parámetro** → Usa default de enum
```java
// application.properties está vacío o no existe

double ups = GameSettings.GAME_UPDATES_PER_SECOND.get();
// Resultado: 60.0 (default de la enumeración)
```

**Ejemplo 2: Con properties** → Usa properties file
```java
// application.properties:
// game.updates.per.second=120.0

double ups = GameSettings.GAME_UPDATES_PER_SECOND.get();
// Resultado: 120.0 (valor del archivo)
```

**Ejemplo 3: Sin properties, con parámetro** → Usa parámetro
```java
// application.properties está vacío

double ups = GameSettings.GAME_UPDATES_PER_SECOND.get(30.0D);
// Resultado: 30.0 (parámetro pasado)
```

**Ejemplo 4: Con properties y parámetro** → Properties gana
```java
// application.properties:
// game.updates.per.second=120.0

double ups = GameSettings.GAME_UPDATES_PER_SECOND.get(30.0D);
// Resultado: 120.0 (properties tiene prioridad sobre parámetro)
```

**Ejemplo 5: Parámetro null** → Usa default de enum
```java
double ups = GameSettings.GAME_UPDATES_PER_SECOND.get(null);
// Resultado: 60.0 (null es ignorado, usa enum default)
```

##### Valores por Defecto Integrados

**GameSettings**:
| Configuración | Valor por Defecto | Descripción |
|---------------|-------------------|-------------|
| `GAME_VERTICAL_SYNCHRONIZATION` | `true` | VSync activado |
| `GAME_UPDATES_PER_SECOND` | `60.0D` | 60 updates/segundo |
| `GAME_MAXIMUM_UPDATES_PER_FRAME` | `5` | Máximo 5 updates/frame |
| `GAME_MAXIMUM_ACCUMULATED_TIME` | `0.5F` | 500ms antes de reset |

**WindowSettings**:
| Configuración | Valor por Defecto | Descripción |
|---------------|-------------------|-------------|
| `WINDOW_WIDTH` | `1280` | Ancho en píxeles |
| `WINDOW_HEIGHT` | `720` | Alto en píxeles |
| `WINDOW_TITLE` | `"3D Game Engine"` | Título de ventana |

##### Ventajas sobre v0.3.9

✅ **Cero duplicación**: Valores por defecto en un solo lugar  
✅ **Robustez máxima**: Sistema de tres niveles garantiza valor válido  
✅ **Flexibilidad total**: Override posible en properties, código o enum  
✅ **Testing mejorado**: Fácil inyectar valores personalizados  
✅ **Mantenibilidad**: Cambiar default solo requiere editar enum  
✅ **Código más limpio**: No más constantes dispersas  
✅ **DRY completo**: Adherencia perfecta al principio Don't Repeat Yourself  

##### Código Simplificado

**GameTiming.java** - Eliminadas constantes públicas:
```java
// ANTES (v0.3.9)
public static final double FRAMERATE = 60.0D;
public static final int MAXIMUM_UPDATES_PER_FRAME = 5;
public static final float MAXIMUM_ACCUMULATED_TIME = 0.5F;

public void init() {
  final double ups = GameSettings.GAME_UPDATES_PER_SECOND
    .get(GameTiming.FRAMERATE);  // Duplicación
}

// DESPUÉS (v0.3.10)
public void init() {
  final double ups = GameSettings.GAME_UPDATES_PER_SECOND.get();
  // ¡Sin constantes, sin duplicación!
}
```

**Application.java** - Eliminadas constantes públicas:
```java
// ANTES (v0.3.9)
public static final int WIDTH = 1280;
public static final int HEIGHT = 720;

Window.get().init(
  WindowSettings.WINDOW_WIDTH.get(Application.WIDTH),   // Duplicación
  WindowSettings.WINDOW_HEIGHT.get(Application.HEIGHT), // Duplicación
  WindowSettings.WINDOW_TITLE.get()
);

// DESPUÉS (v0.3.10)
Window.get().init(
  WindowSettings.WINDOW_WIDTH.get(),   // ¡Simple y limpio!
  WindowSettings.WINDOW_HEIGHT.get(),
  WindowSettings.WINDOW_TITLE.get()
);
```

##### Comparación de Arquitectura

| Aspecto | v0.3.9 | v0.3.10 |
|---------|---------|---------|
| **Constantes duplicadas** | Sí (GameTiming, Application) | No (solo en enums) |
| **Niveles de fallback** | 2 (properties → parámetro) | 3 (properties → parámetro → enum) |
| **Valores por defecto** | Hardcoded en constantes | Integrados en enumeraciones |
| **Líneas de código** | +15 líneas (constantes) | 0 líneas extra |
| **Principio DRY** | Violado | Respetado |
| **Mantenibilidad** | Media (múltiples lugares) | Alta (un solo lugar) |
| **Robustez** | Buena | Excelente |

##### Configuración

Archivo `application.properties` (opcional):
```properties
# Game loop configuration
game.updates.per.second=60.0
game.maximum.updates.per.frame=5
game.maximum.accumulated.time=0.5
game.vertical.synchronization=true

# Window configuration
window.width=1280
window.height=720
window.title=3D Game Engine Tutorial
```

Si el archivo no existe o alguna propiedad falta, el sistema **automáticamente usa los valores por defecto de las enumeraciones**.

> **Nota**: Esta versión mantiene toda la robustez de v0.3.9 (separación de responsabilidades, GameTiming, callbacks) pero elimina completamente la duplicación de código y centraliza la configuración. Ideal para proyectos que valoran código limpio, mantenible y sin repeticiones.

#### v0.3.9 - Separación de Responsabilidades con GameTiming

La versión 0.3.9 representa una **refactorización arquitectónica mayor** que extrae toda la lógica del game loop de `Application` a una nueva clase `GameTiming`:

##### Arquitectura

**Antes (v0.3.8)**: Todo en `Application` (369 líneas)
```
Application
├─> Timing logic (~140 líneas)
├─> Spiral protection
├─> UPS/FPS metrics
├─> Window management
└─> Configuration
```

**Después (v0.3.9)**: Separación de responsabilidades
```
Application (~99 líneas)          GameTiming (402 líneas, Singleton)
├─> Window setup                  ├─> Timing logic
├─> Configuration                 ├─> Spiral protection
├─> Callbacks setup               ├─> UPS/FPS metrics
└─> Main loop (simple)            ├─> Update/Render callbacks
                                  └─> Fixed timestep control
```

##### Nueva Clase GameTiming

```java
package es.noa.rad.game.engine.core;

public final class GameTiming {
    
    /* Singleton instance. */
    private static GameTiming instance;
    
    /* Constants. */
    public static final double FRAMERATE = 60.0D;
    public static final int MAXIMUM_UPDATES_PER_FRAME = 5;
    public static final float MAXIMUM_ACCUMULATED_TIME = 0.5F;
    
    /* Timing fields. */
    private boolean running;
    private double deltaTime;
    private long previousTime;
    private double updateTime;
    private float fixedDeltaTime;
    private double maxDeltaTime;
    
    /* Metrics. */
    private int totalSkippedUpdates;
    private int ups, fps;
    private long upsTime, fpsTime;
    
    /* Callbacks (optional, with fallback). */
    private Consumer<Float> updateCallback;
    private Consumer<Float> renderCallback;
    
    /**
     * Gets the singleton instance of GameTiming.
     */
    public static GameTiming get() {
        if (instance == null) {
            instance = createInstance();
        }
        return instance;
    }
    
    /**
     * Initializes timing configuration from GameSettings.
     */
    public void init() {
        // Load configuration values once
        final double updatesPerSecond = GameSettings.GAME_UPDATES_PER_SECOND
            .get(FRAMERATE);
        final float maxAccumulatedTime = GameSettings.GAME_MAXIMUM_ACCUMULATED_TIME
            .get(MAXIMUM_ACCUMULATED_TIME);
        final int maxUpdatesPerFrame = GameSettings.GAME_MAXIMUM_UPDATES_PER_FRAME
            .get(MAXIMUM_UPDATES_PER_FRAME);
        
        // Calculate timing constants
        this.updateTime = NANOSECONDS_IN_SECOND / updatesPerSecond;
        this.fixedDeltaTime = (float) (1.0D / updatesPerSecond);
        this.maxDeltaTime = maxAccumulatedTime * updatesPerSecond;
    }
    
    /**
     * Configures the update callback.
     */
    public GameTiming updateCallback(final Consumer<Float> _callback) {
        this.updateCallback = _callback;
        return this;
    }
    
    /**
     * Configures the render callback.
     */
    public GameTiming renderCallback(final Consumer<Float> _callback) {
        this.renderCallback = _callback;
        return this;
    }
    
    /**
     * Processes one complete frame of the game loop.
     */
    public boolean tick() {
        if (!this.running) return false;
        
        // Calculate delta time since last frame
        final long currentTime = System.nanoTime();
        this.deltaTime += (currentTime - this.previousTime) / this.updateTime;
        this.previousTime = currentTime;
        
        // PROTECTION 1: Reset if spiral of death detected
        if (this.deltaTime > this.maxDeltaTime) {
            final int skippedUpdates = (int) (this.deltaTime - this.maxDeltaTime);
            this.totalSkippedUpdates += skippedUpdates;
            LOGGER.debug(...);
            this.deltaTime = 0D;
        }
        
        // PROTECTION 2: Limit updates per frame
        int updates = 0;
        while (this.deltaTime >= 1.0D && updates < maxUpdatesPerFrame) {
            this.update(this.fixedDeltaTime);
            this.deltaTime--;
            updates++;
        }
        
        // Discard excess accumulated time
        if (this.deltaTime >= 1.0D) {
            final int discarded = (int) this.deltaTime;
            this.totalSkippedUpdates += discarded;
            LOGGER.debug(...);
            this.deltaTime = 0D;
        }
        
        // Render with interpolation
        final float alpha = (float) this.deltaTime;
        this.render(alpha);
        
        return true;
    }
    
    /* Private methods with hybrid callback pattern. */
    private void update(final float _deltaTime) {
        if (this.updateCallback != null) {
            this.updateCallback.accept(_deltaTime);
        } else {
            Window.get().update(_deltaTime);  // Fallback
        }
        // ... UPS metrics
    }
    
    private void render(final float _alpha) {
        if (this.renderCallback != null) {
            this.renderCallback.accept(_alpha);
        } else {
            Window.get().render(_alpha);  // Fallback
        }
        // ... FPS metrics
    }
}
```

##### Application Simplificado

```java
@Override
public void run() {
    this.init();
    
    // Simple loop: GameTiming handles everything
    while (!Window.shouldClose() && GameTiming.get().tick()) {
        Window.get().swapBuffers();
    }
    
    this.close();
    this.stop();
}

private void init() {
    Configuration.get().init();
    Window.get().init(width, height, title);
    
    if (GameSettings.GAME_VERTICAL_SYNCHRONIZATION.get(true)) {
        Window.get().enableVSync();
    }
    
    // Configure callbacks (optional)
    GameTiming.get().updateCallback(dt -> Window.get().update(dt));
    GameTiming.get().renderCallback(dt -> Window.get().render(dt));
    GameTiming.get().init();
}
```

##### Características de la v0.3.9

| Aspecto | Descripción |
|---------|-------------|
| **Separación de Responsabilidades** | `Application` solo orquesta, `GameTiming` gestiona el loop |
| **Singleton Pattern** | Acceso global consistente vía `GameTiming.get()` |
| **Hybrid Callbacks** | Callbacks opcionales con fallback a `Window.get()` |
| **Testabilidad** | `GameTiming` se puede testear sin Window/GLFW |
| **Reutilización** | `GameTiming` puede usarse en diferentes contextos |
| **Simplicidad** | `Application` reducido de 369 a ~99 líneas (-73%) |
| **Método `tick()`** | Procesa frame completo, retorna si debe continuar |
| **Lifecycle Público** | `init()`, `start()`, `stop()` son públicos |

##### Ventajas sobre v0.3.8

✅ **Arquitectura más limpia**: Cada clase tiene un propósito único  
✅ **Mejor testabilidad**: Game loop sin dependencias de GLFW/OpenGL  
✅ **Mayor reutilización**: `GameTiming` puede usarse en herramientas, servidores headless  
✅ **Más mantenible**: Cambios en timing no afectan `Application`  
✅ **Flexibilidad**: Callbacks permiten diferentes implementaciones  
✅ **Extensibilidad**: Añadir features en `GameTiming` sin tocar `Application`  
✅ **Misma robustez**: Mantiene doble protección y telemetría de v0.3.8  

##### Comparación de Código

**Antes (v0.3.8)**: 369 líneas en Application
```java
// Application.java - Muchos campos
private boolean running;
private double deltaTime;
private long previousTime;
private int totalSkippedUpdates;
private int ups, fps;
private long upsTime, fpsTime;
// ... muchos más

@Override
public void run() {
    // ... 140+ líneas de lógica del game loop
    while (running && !Window.shouldClose()) {
        final long currentTime = System.nanoTime();
        deltaTime += (currentTime - previousTime) / updateTime;
        previousTime = currentTime;
        
        // ... mucha lógica compleja
    }
}
```

**Después (v0.3.9)**: 99 líneas en Application + 402 en GameTiming
```java
// Application.java - Sin campos de timing
@Override
public void run() {
    this.init();  // Setup
    
    // Loop ultra simple
    while (!Window.shouldClose() && GameTiming.get().tick()) {
        Window.get().swapBuffers();
    }
    
    this.close();
    this.stop();
}

// GameTiming.java - Toda la lógica del loop
public boolean tick() {
    if (!this.running) return false;
    // ... lógica del game loop (140 líneas)
    return true;
}
```

##### Uso del Sistema de Callbacks

**Uso básico (sin callbacks, usa fallback)**:
```java
GameTiming.get().init();

while (!Window.shouldClose() && GameTiming.get().tick()) {
    Window.get().swapBuffers();
}
// GameTiming llama automáticamente a Window.get().update() y .render()
```

**Uso avanzado (con callbacks para testing)**:
```java
@Test
public void testGameLoop() {
    List<Float> updates = new ArrayList<>();
    List<Float> renders = new ArrayList<>();
    
    GameTiming.get()
        .updateCallback(dt -> updates.add(dt))
        .renderCallback(alpha -> renders.add(alpha))
        .init();
    
    GameTiming.get().tick();  // No requiere Window real
    
    assertEquals(0.0166f, updates.get(0), 0.001f);  // 1/60
}
```

**Uso avanzado (headless server sin rendering)**:
```java
GameTiming.get()
    .updateCallback(dt -> gameWorld.simulate(dt))
    .renderCallback(alpha -> { /* no-op */ })
    .init();

while (serverRunning && GameTiming.get().tick()) {
    networkManager.sync();  // Sincronizar con clientes
}
```

##### Configuración

Usa las mismas propiedades de v0.3.8:

```properties
# Fixed timestep configuration
game.updates.per.second=60.0

# Spiral of death protection
game.maximum.updates.per.frame=5
game.maximum.accumulated.time=0.5

# VSync
game.vertical.synchronization=true
```

##### Métricas de Rendimiento

GameTiming mantiene las mismas métricas de v0.3.8:

- **UPS** (Updates Per Second): Updates procesados por segundo
- **FPS** (Frames Per Second): Frames renderizados por segundo
- **Total Skipped Updates**: Contador acumulado de updates descartados

Acceso a métricas:
```java
int ups = GameTiming.get().getUps();
int fps = GameTiming.get().getFps();
int skipped = GameTiming.get().getTotalSkippedUpdates();
```

> **Nota**: Esta versión mantiene toda la robustez de v0.3.8 (Fixed Timestep, VSync, doble protección, telemetría) pero con una arquitectura significativamente mejor. Ideal para proyectos que valoran separación de responsabilidades, testabilidad, y código mantenible.

#### v0.3.8 - Spiral of Death Protegido con Telemetría

La versión 0.3.8 mejora la v0.3.7 añadiendo **doble protección contra Spiral of Death** y **telemetría de rendimiento**:

```java
// Constantes ampliadas
private static final long NANOSECONDS_IN_SECOND = TimeUnit.SECONDS.toNanos(1L);
private static final double FRAMERATE = 60.0D;
private static final int MAXIMUM_UPDATES_PER_FRAME = 5;
private static final float MAXIMUM_ACCUMULATED_TIME = 0.5F;  // NUEVO: 500ms

// Variables de timing, control y telemetría
private boolean running;
private double deltaTime;
private long previousTime;
private int totalSkippedUpdates;  // NUEVO: Contador total

// Configuración cargada una vez (optimización)
Configuration.get().init();
Window.get().init(width, height, title);

if (GameSettings.GAME_VERTICAL_SYNCHRONIZATION.get(true)) {
    Window.get().enableVSync();
}

running = true;

/* Load configuration values once to avoid repeated lookups. */
final double updatesPerSecond = GameSettings.GAME_UPDATES_PER_SECOND
  .get(FRAMERATE);
final float maxAccumulatedTime = GameSettings.GAME_MAXIMUM_ACCUMULATED_TIME
  .get(MAXIMUM_ACCUMULATED_TIME);
final int maxUpdatesPerFrame = GameSettings.GAME_MAXIMUM_UPDATES_PER_FRAME
  .get(MAXIMUM_UPDATES_PER_FRAME);

/* Establish the time that must elapse between each update. */
final double updateTime = NANOSECONDS_IN_SECOND / updatesPerSecond;

/* Fixed timestep for deterministic updates. */
final float fixedDeltaTime = (float) (1.0D / updatesPerSecond);

/* Maximum delta time threshold (in update units). */
final double maxDeltaTime = maxAccumulatedTime * updatesPerSecond;

// Timing inicial
previousTime = System.nanoTime();

// Game loop con doble protección
while (running && !Window.get().shouldClose()) {
    long currentTime = System.nanoTime();
    
    /*
     * Accumulate elapsed time normalized to "update units".
     * deltaTime >= 1.0 means one update is needed.
     */
    deltaTime += (currentTime - previousTime) / updateTime;
    previousTime = currentTime;
    
    /*
     * PROTECCIÓN 1: Reset si acumulación excesiva.
     * If too much time accumulated, reset to maximum threshold.
     * This prevents infinite catch-up loop on very slow hardware.
     */
    if (deltaTime > maxDeltaTime) {
        System.out.printf(
          "Delta time too high (%.2f updates), "
          + "resetting to %.2f (max %.2f seconds).%n",
          deltaTime, maxDeltaTime, maxAccumulatedTime
        );
        int skippedUpdates = (int) (deltaTime - maxDeltaTime);
        totalSkippedUpdates += skippedUpdates;  // Telemetría
        deltaTime = maxDeltaTime;
    }
    
    /*
     * Catch-up loop: Run accumulated updates with fixed timestep.
     * Limited to maxUpdatesPerFrame to prevent spiral of death.
     */
    int updateCount = 0;
    while (deltaTime >= 1.0D && updateCount < maxUpdatesPerFrame) {
        update(fixedDeltaTime);  // Siempre 0.0166s para 60 UPS
        deltaTime--;
        updateCount++;
    }
    
    /*
     * PROTECCIÓN 2: Descartar updates restantes si se alcanzó el límite.
     */
    if (deltaTime >= 1.0D) {
        int skipped = (int) deltaTime;
        totalSkippedUpdates += skipped;  // Telemetría
        deltaTime -= skipped;
        if (skipped > 0) {
            System.out.printf(
              "Skipped %d updates (limit: %d per frame) "
              + "to prevent spiral of death.%n",
              skipped, maxUpdatesPerFrame
            );
        }
    }
    
    /*
     * Render with interpolation alpha (0.0 to 1.0).
     * Alpha represents progress between current and next update,
     * allowing smooth visuals even with fixed physics timestep.
     */
    render((float) deltaTime);
    
    /*
     * Swap buffers and poll events.
     * VSync makes this call block until the next vertical refresh.
     */
    Window.get().swapBuffers();
}
```

### Características del Sistema (v0.3.8)

- **Doble protección contra Spiral of Death**:
  - Nivel 1: Reset de `deltaTime` si excede 30 updates (500ms)
  - Nivel 2: Límite de 5 updates por frame
- **Telemetría de rendimiento**: Campo `totalSkippedUpdates` rastrea updates omitidos
- **Configuración optimizada**: Variables cargadas una sola vez al inicio
- **Expresiones simplificadas**: Eliminados casts redundantes y código duplicado
- **Logging informativo**: Mensajes con valores específicos para debugging
- **Fixed Timestep**: Física determinística con `fixedDeltaTime` constante (0.0166s)
- **VSync**: Sincronización con monitor para imagen suave sin tearing
- **UPS/FPS independientes**: Updates a 60 Hz, Renders al refresh rate del monitor
- **Accumulator pattern**: `deltaTime` acumula tiempo y se consume en updates
- **Interpolation alpha**: Valor residual (0.0-1.0) para suavizado visual
- **Control de flujo**: Campo `boolean running` para parada limpia del loop
- **Comentarios descriptivos**: Documentación interna clara y educativa

### Ventajas de v0.3.8 sobre v0.3.7

- ✅ **Mejor diagnóstico**: Telemetría de updates omitidos totales
- ✅ **Logging más informativo**: Mensajes con valores actuales de deltaTime, límites y umbrales
- ✅ **Código más limpio**: Variables extraídas, eliminada duplicación (DRY)
- ✅ **Mejor rendimiento**: Menos llamadas a `GameSettings` (de 10+ a 3)
- ✅ **Mayor robustez**: Doble protección en lugar de protección simple
- ✅ **Más mantenible**: Cambios en un solo lugar, comentarios descriptivos
- ✅ **Análisis de rendimiento**: Estadísticas muestran total de updates omitidos

### Protección Doble Explicada

**Nivel 1 - Reset de acumulación excesiva:**
```java
if (deltaTime > maxDeltaTime) {  // Si > 30 updates acumulados
    totalSkippedUpdates += (deltaTime - maxDeltaTime);
    deltaTime = maxDeltaTime;  // Reset a 30 updates máximo
}
```

**Nivel 2 - Límite de updates por frame:**
```java
while (deltaTime >= 1.0D && updateCount < 5) {
    update(fixedDeltaTime);
    updateCount++;
}
```

**¿Por qué doble protección?**
- Hardware muy lento puede acumular > 30 updates en un solo frame
- Nivel 1 evita acumulación infinita
- Nivel 2 garantiza que cada frame renderiza (máximo 5 updates)
- Juntos previenen congelamiento total

### Comportamiento en Diferentes Escenarios

**Hardware normal (60 FPS, 60 UPS):**
```
Updates por frame: 1
deltaTime: 0.0-2.0
Protección nivel 1: Nunca activa
Protección nivel 2: Nunca activa
Total skipped: 0
```

**Hardware lento con spike (lag momentáneo):**
```
Frame problema: deltaTime sube a 45.23
Protección nivel 1: ACTIVA → reset a 30.0
Updates ejecutados: 5 (límite nivel 2)
Updates omitidos: ~25
Total skipped: 25
```

**Hardware muy lento persistente (10 FPS, 60 UPS):**
```
Cada frame: deltaTime = 6.0
Protección nivel 1: No (< 30)
Protección nivel 2: ACTIVA → límite 5 updates
Update omitido por frame: 1
Total skipped: Incrementa constantemente
```

**Sistema colapsado (< 5 FPS):**
```
Cada frame: deltaTime > 30.0
Protección nivel 1: ACTIVA → reset a 30.0
Protección nivel 2: ACTIVA → límite 5 updates
Updates omitidos por frame: ~25+
Total skipped: Incrementa rápidamente
Aplicación: Sigue respondiendo (no se congela)
```

### Comparación v0.3.7 vs v0.3.8

| Característica | v0.3.7 | v0.3.8 |
|----------------|--------|--------|
| **Protección spiral** | Simple (límite 5) | **Doble (reset + límite)** |
| **Telemetría** | No | **Sí (totalSkippedUpdates)** |
| **Logging** | Genérico | **Informativo con valores** |
| **Config lookups** | 10+ por frame | **3 al inicio** |
| **Código duplicado** | Sí (6 veces) | **No (DRY)** |
| **Comentarios** | Básicos | **Descriptivos y educativos** |
| **Mantenibilidad** | Media | **Alta** |
| **Debugging** | Limitado | **Completo** |
| **Rendimiento** | Bueno | **Mejor** |

### Cuándo Usar v0.3.8

**Ideal para:**
- ✅ **Proyectos profesionales** que necesitan máxima robustez
- ✅ **Juegos multijugador** con física determinística
- ✅ **Hardware variable** (PCs, laptops, equipos antiguos)
- ✅ **Análisis de rendimiento** (necesitas saber updates omitidos)
- ✅ **Debugging complejo** (logging informativo crucial)
- ✅ **Código mantenible** (proyectos a largo plazo)
- ✅ **Tutoriales educativos** (comentarios descriptivos)

**No usar si:**
- ❌ Necesitas la implementación más simple posible
- ❌ Hardware garantizado siempre rápido
- ❌ No te importa el logging detallado

### Ejemplo de Salida

**Ejecución normal:**
```
Window created with a size (1280x720) and with the title '3D Game Engine Tutorial'.
Updates Per Second (UPS): 60, Total Skipped Updates: 0
Frames Per Second (FPS): 60
Updates Per Second (UPS): 60, Total Skipped Updates: 0
Frames Per Second (FPS): 60
```

**Durante lag spike:**
```
Delta time too high (45.23 updates), resetting to 30.00 (max 0.50 seconds).
Skipped 25 updates (limit: 5 per frame) to prevent spiral of death.
Updates Per Second (UPS): 35, Total Skipped Updates: 25
Frames Per Second (FPS): 20
Updates Per Second (UPS): 60, Total Skipped Updates: 25
Frames Per Second (FPS): 60
```

**Hardware persistentemente lento:**
```
Skipped 1 updates (limit: 5 per frame) to prevent spiral of death.
Updates Per Second (UPS): 55, Total Skipped Updates: 60
Frames Per Second (FPS): 55
Skipped 1 updates (limit: 5 per frame) to prevent spiral of death.
Updates Per Second (UPS): 55, Total Skipped Updates: 120
Frames Per Second (FPS): 55
```

> **Nota**: Esta es la **implementación más completa, robusta y mantenible** del tutorial, combinando Fixed Timestep (v0.3.4), VSync (v0.3.5), Spiral of Death Protection doble (v0.3.7 mejorada), y telemetría de rendimiento. Ideal para proyectos profesionales que requieren física determinística, calidad visual perfecta, funcionamiento en hardware variable, y capacidades de análisis de rendimiento.

## Pruebas

Ejecutar las pruebas unitarias:

```bash
mvn test
```

## Verificación de Calidad de Código

El proyecto usa Checkstyle para mantener estándares de calidad:

```bash
mvn checkstyle:check
```

### Configuración de Checkstyle

El proyecto sigue las convenciones de código de Sun/Oracle con personalizaciones:

- **Longitud de línea**: Máximo 80 caracteres
- **JavaDoc**: Requerido en todos los métodos y clases
- **Magic Numbers**: Números literales permitidos: `-3, -2, -1, 0, 1, 2, 3`
  - Números pequeños comunes no requieren constantes
  - Números mayores (5, 10, 16, 60, etc.) deben usar constantes nombradas
  - Ejemplo: `FRAMERATE = 60.0D`, `MAXIMUM_UPDATES_PER_FRAME = 5`
- **Naming**: Parámetros con prefijo `_` (ej: `_deltaTime`)
- **Exceptions**: Nombres con sufijo `Exception` (ej: `interruptedException`)

El archivo de configuración está en `doc/checkstyle/checkstyle-rules.xml`.

## Estructura del Proyecto

```
3d-game-engine-tutorial/
  src/
    main/
      java/
        es/noa/rad/game/
          Application.java
          engine/
            configuration/
              Configuration.java
              settings/
                GameSettings.java
                WindowSettings.java
            core/
              GameTiming.java
              Window.java
            event/
              KeyboardEventHandler.java
              MouseEventHandler.java
              callback/
                KeyCallback.java
                MouseButtonCallback.java
                CursorPosCallback.java
      resources/
        es/noa/rad/game/
          settings/
            application.properties
    test/
      java/
        es/noa/rad/game/
      resources/
        es/noa/rad/game/
  doc/
    checkstyle/
      dtd/
        configuration-1-3.dtd
      checkstyle-rules.xml
  .gitattributes
  .editorconfig
  .gitignore
  CHANGELOG.md
  LICENSE
  pom.xml
  README.md
```

## Ciclo de Vida de la Aplicación

El motor de juego sigue un ciclo de vida claro y educativo:

1. **Inicialización (`init`)**: Configuración de la ventana y recursos
2. **Game Loop**: Bucle principal con actualización y renderizado
   - `update()`: Actualiza la lógica del juego
   - `render()`: Dibuja el frame actual
   - Medición de FPS/UPS en tiempo real
3. **Limpieza (`close`)**: Libera recursos y termina GLFW

Este patrón enseña la importancia de la gestión correcta de recursos.

## Convenciones de Código

Este proyecto sigue las convenciones de codificación de Sun/Oracle Java y utiliza Checkstyle para su cumplimiento.

## Licencia

Ver archivo [LICENSE](LICENSE) para más detalles.