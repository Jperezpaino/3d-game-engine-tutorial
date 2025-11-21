# Changelog

Todos los cambios notables en este proyecto serán documentados en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
- Código educativo sin complejidad de caching
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

[0.2.1]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.2.1
[0.2.0]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.2.0
[0.1.0]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.1.0
[0.0.0]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.0.0