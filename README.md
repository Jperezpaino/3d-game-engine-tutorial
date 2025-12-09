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

El archivo JAR se generará en `target/3d-game-engine-tutorial-0.3.3.jar`

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
game.frames.per.second = 60.0
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
Double framesPerSecond = GameSettings.GAME_FRAMES_PER_SECOND.get(60.0);
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

#### v0.3.3 - Fixed Timestep para Updates (Implementación Actual)

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

### Limitaciones Conocidas (Fixed Timestep)

Este enfoque aún tiene limitaciones educativas:

- **Sin spiral of death protection**: No limita máximo de updates por frame
- **Interpolación no implementada**: `Window` recibe alpha pero no interpola aún
- **Sin estados separados**: No guarda posición anterior para interpolación real
- **Sleep no es preciso**: `Thread.sleep()` tiene variabilidad del sistema operativo

### Ejemplo de Salida

```
Updates Per Second (UPS): 60
Frames Per Second (FPS): 144
Updates Per Second (UPS): 60
Frames Per Second (FPS): 143
```

> **Nota**: Ahora UPS y FPS pueden tener valores diferentes. Las siguientes versiones implementarán interpolación real, spiral of death protection, y optimizaciones adicionales para un game loop de nivel profesional.

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