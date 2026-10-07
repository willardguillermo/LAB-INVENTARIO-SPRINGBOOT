# CLAUDE.md

## Contexto del proyecto

API REST de inventario hospitalario con Spring Boot 4 + JPA/Hibernate + MySQL (Java 21, Lombok).
Evaluación 02 del curso Desarrollo de Aplicaciones Web (Tecsup): arquitectura en capas
(Controller, Service, Repository), relaciones JPA, validaciones, manejo global de excepciones,
AOP y Spring Security con roles.

Proyecto en equipo de 3 (reparto de la evaluación):

- **willardguillermo** (dueño de este entorno):
  - P1 con **Producto**: RF-INV-01 Registrar, RF-INV-02 Modificar, RF-INV-03 Activar/desactivar,
    RF-INV-14 Buscar y filtrar.
  - **P2 Auditoría** (AOP): registra usuario, fecha y hora, operación, entidad e id del registro
    en registro, modificación y eliminación lógica (desactivar), y permite consultar la bitácora.
- **alexanderFaustino** (Categoría y Unidad de medida, RF-INV-15/16/17): P1 con `@OneToMany` en
  Categoría y Unidad + P3 Usuarios y roles (backend) + P4 frontend de usuarios y roles.
- **mijaelino21-debug** (Proveedor, RF-INV-20/21/22): P1 con `@OneToMany` en Proveedor +
  P5 Spring Security y control de acceso.

Roles acordados:

| Rol | Permisos |
|---|---|
| ADMINISTRADOR | Todo |
| ALMACENERO | Gestiona productos, categorías y proveedores |
| MÉDICO | Solo consulta y búsqueda de productos |

Los métodos de servicio de Producto deben quedar listos para protegerse con `@PreAuthorize`.

Producto tiene `@ManyToOne` con Categoria y UnidadMedida (obligatorias) y Proveedor (opcional).

## Reglas

1. Solo modificar estos archivos:
   - Producto: `ProductoEntity`, `ProductoService`, `ProductoController`, `ProductoRepository`,
     `ProductoServiceTest`.
   - El paquete `exception/` (incluido `GlobalExceptionHandler`).
   - Auditoría: el paquete `aop/` (anotación `@Auditable`, aspecto y componente de usuario actual),
     `entity/Auditoria`, `repository/AuditoriaRepository`, `service/AuditoriaService`,
     `controller/AuditoriaController` y sus pruebas en `src/test/`.
   - `pom.xml`, solo para agregar `spring-boot-starter-aspectj` y H2 (scope test).
   - En `src/main/resources/static/index.html`: la sección de productos y la pestaña de Auditoría.

   No tocar archivos de Categoría, UnidadMedida ni Proveedor sin preguntar antes (tampoco para
   anotarlos con `@Auditable`: eso lo hace cada compañero).
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
  (En Productos se corrige como parte de este trabajo.)
- `Categoria` y `UnidadMedida` no tienen `@NotBlank` y sus controladores no usan `@Valid`.
- Inconsistencias: paquetes `model`/`models`/`entity`/`impl` mezclados, tres estilos de Lombok y
  tres convenciones de estado (`Boolean estado`, `Boolean activo`, `String "ACTIVO"`).
- `contextLoads` necesita MySQL en el puerto 3307.
- El commit `7759922` (ramas `feature/productos` y `feature/integracion`, repo público) expone una
  contraseña de MySQL en `application.properties`.
