# Changelog

Todos los cambios notables en este proyecto serán documentados en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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