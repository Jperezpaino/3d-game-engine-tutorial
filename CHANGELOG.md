# Changelog

Todos los cambios notables en este proyecto serán documentados en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [[0.1.0]] - 2025-11-20

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

## [[0.0.0]] - 2025-11-19

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

[0.1.0]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.1.0
[0.0.0]: https://github.com/Jperezpaino/3d-game-engine-tutorial/releases/tag/0.0.0