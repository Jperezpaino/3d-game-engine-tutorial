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

El archivo JAR se generará en `target/3d-game-engine-tutorial-0.3.1.jar`

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

// Usar GameSettings para propiedades del juego (con valor por defecto)
Double framesPerSecond = GameSettings.GAME_FRAMES_PER_SECOND.get(60.0);
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

#### v0.3.1 - Frame Capping (Implementación Actual)

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

### Características del Sistema (v0.3.1)

- **Medición en tiempo real**: Imprime estadísticas cada segundo en consola
- **Contadores separados**: UPS y FPS se miden independientemente
- **Frame Capping**: Ajusta el sleep dinámicamente según el tiempo real de ejecución
- **Compensación de overhead**: Resta el tiempo de `update()` y `render()` del sleep
- **Alta precisión**: Usa `System.nanoTime()` para medición en nanosegundos
- **Configurable**: FPS objetivo definido en `application.properties` (60.0 por defecto)
- **Robusto**: Valor por defecto de 60.0 FPS si falta la configuración
- **Consistencia de tipos**: Comparación correcta de tipos (`long > 0` en lugar de `> 0.0F`)

### Mejoras sobre Fixed Sleep

- ✅ **Compensación dinámica**: El sleep se ajusta según el overhead real
- ✅ **Mayor precisión**: Uso de nanosegundos en lugar de milisegundos
- ✅ **Frame rate más estable**: Reduce variabilidad en el timing
- ✅ **Configurable**: FPS objetivo se lee desde configuración

### Limitaciones Conocidas (Frame Capping)

Este enfoque aún tiene limitaciones educativas:

- **UPS = FPS**: Ambos siguen acoplados al mismo ciclo
- **No recupera frames perdidos**: Si un frame tarda más que `renderTime`, no compensa
- **Sin delta time**: Los movimientos no se ajustan según el tiempo real transcurrido
- **Sleep no es preciso**: `Thread.sleep()` tiene variabilidad del sistema operativo

### Ejemplo de Salida

```
Updates Per Second (UPS): 60
Frames Per Second (FPS): 60
Updates Per Second (UPS): 60
Frames Per Second (FPS): 60
```

> **Nota**: Las siguientes versiones introducirán UPS desacoplado de FPS, delta time, y técnicas avanzadas de sincronización para lograr un control profesional del game loop.

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