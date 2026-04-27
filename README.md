# Proyecto desarrollado por Juan Manuel Daza y Camila Rodríguez — 2026
-----------------------------------------------------------------------

## Arquitectura del Proyecto

```text
PROYECTO-INTEGRADOR/
│
├── src/main/java/com/proyecto/
│   │
│   ├── config/
│   │   └── ConexionBD.java
│   │      → Aquí se conecta Java con MySQL
│   │        (como un cable entre los dos)
│   │
│   ├── modelo/
│   │   → Son las “fichas” de los datos.
│   │     Por ejemplo: qué datos tiene un Producto
│   │
│   ├── dao/
│   │   → Aquí están las consultas a la base de datos
│   │     (SELECT, INSERT, UPDATE, DELETE)
│   │
│   ├── controlador/
│   │   → Es el cerebro del sistema.
│   │     Decide qué hacer cuando el usuario realiza una acción
│   │
│   ├── vista/
│   │   → Son las pantallas que ve el usuario
│   │     (ventanas, formularios, botones, tablas)
│   │
│   └── Main.java
│       → Punto de inicio del programa
│         (lo primero que se ejecuta)
│
├── test/
│   → Carpeta para pruebas del sistema
│
├── pom.xml
│   → Archivo de Maven que contiene las dependencias
│     y configuraciones del proyecto
│
└── Proyecto_integrador.sql
    → Script SQL que crea la base de datos y las tablas en MySQL
```