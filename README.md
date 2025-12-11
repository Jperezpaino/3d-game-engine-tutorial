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

El archivo JAR se generará en `target/3d-game-engine-tutorial-0.3.8.jar`

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
- ✅ **Configurable**: Ajustar `game.maximun.updates.per.frame` según necesidades
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

#### v0.3.7 - Fixed Timestep + VSync + Spiral of Death Protection (Implementación Actual)

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

> **Nota**: Esta es la **implementación más completa y robusta** del tutorial, combinando Fixed Timestep (v0.3.4), VSync (v0.3.5), y Spiral of Death Protection (v0.3.4). Ideal para proyectos profesionales que requieren física determinística, calidad visual perfecta, y funcionamiento en hardware variable.

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
              Window.java
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