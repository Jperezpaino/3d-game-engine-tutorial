# Changelog

Todos los cambios notables en este proyecto serán documentados en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.0.0] - 2025-11-19

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

[0.0.0]: https://github.com/tu-usuario/3d-game-engine-tutorial/releases/tag/v0.0.0