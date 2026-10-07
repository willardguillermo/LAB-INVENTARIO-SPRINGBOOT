# CLAUDE.md

## Contexto del proyecto

API REST de inventario hospitalario con Spring Boot 4 + JPA/Hibernate + MySQL (Java 21, Lombok).
Evaluación 02 del curso Desarrollo de Aplicaciones Web (Tecsup): arquitectura en capas
(Controller, Service, Repository), relaciones JPA, validaciones, manejo global de excepciones,
AOP y Spring Security con roles.

Proyecto en equipo de 3:

- **willardguillermo** (dueño de este entorno): módulo **Producto**
  - RF-INV-01 Registrar productos
  - RF-INV-02 Modificar productos
  - RF-INV-03 Activar/desactivar productos
  - RF-INV-14 Buscar y filtrar productos
- **alexanderFaustino**: Categoría y Unidad de medida.
- **mijaelino21-debug**: Proveedores.
- Usuarios, roles, Spring Security y auditoría (AOP) los implementan los compañeros.
  Los métodos de servicio de Producto deben quedar listos para ser interceptados por su
  auditoría AOP (usuario, fecha, operación, entidad, id) y protegidos con `@PreAuthorize`.

Producto tiene `@ManyToOne` con Categoria y UnidadMedida (obligatorias) y Proveedor (opcional).

## Reglas

1. Solo modificar archivos de Producto (`ProductoEntity`, `ProductoService`, `ProductoController`,
   `ProductoRepository`, `ProductoServiceTest`), el paquete `exception/` (incluido
   `GlobalExceptionHandler`) y la sección de productos de `src/main/resources/static/index.html`.
   No tocar archivos de Categoría, UnidadMedida ni Proveedor sin preguntar antes.
2. Los commits NO deben llevar "Co-Authored-By: Claude" ni "Generated with Claude Code".
   Mensajes de commit en español, en imperativo (ej. "Agrega endpoint para desactivar productos").
3. No hacer push sin que el usuario lo pida.
4. No modificar `application.properties` (los compañeros usan otra configuración: puerto 3307).
5. No tocar el test `contextLoads` (`LabInventarioSpringbootApplicationTests`).

## Cómo ejecutar en esta máquina

MySQL (XAMPP) corre en el puerto **3306**, usuario `root` sin contraseña. Se sobrescribe la URL
por argumento en lugar de editar `application.properties`:

```
mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.datasource.url=jdbc:mysql://localhost:3306/inventario_hospital?createDatabaseIfNotExist=true"
```

- Compilar: `mvnw.cmd compile`
- Pruebas de Producto (no requieren MySQL): `mvnw.cmd test -Dtest=ProductoServiceTest`
- `mvnw.cmd test` completo falla en esta máquina porque `contextLoads` apunta al puerto 3307.

## Pendientes del equipo (no corregir sin preguntar)

- XSS en `index.html`: las secciones de Categorías y Proveedores construyen el `onclick` con
  `JSON.stringify(...)` dentro del atributo HTML; un nombre con `&quot;` permite inyectar JavaScript.
  (En Productos ya se corrigió.)
- `Categoria` y `UnidadMedida` no tienen `@NotBlank` y sus controladores no usan `@Valid`.
- Inconsistencias: paquetes `model`/`models`/`entity`/`impl` mezclados, tres estilos de Lombok y
  tres convenciones de estado (`Boolean estado`, `Boolean activo`, `String "ACTIVO"`).
- `contextLoads` necesita MySQL en el puerto 3307.
- El commit `7759922` (ramas `feature/productos` y `feature/integracion`, repo público) expone una
  contraseña de MySQL en `application.properties`.
