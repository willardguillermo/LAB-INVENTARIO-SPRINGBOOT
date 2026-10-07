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
     `controller/AuditoriaController`, `dto/PaginaRespuesta` y sus pruebas en `src/test/`.
   - `pom.xml`, solo para agregar `spring-boot-starter-aspectj` y H2 (scope test).
   - `src/test/resources/application.properties` (H2 para pruebas).
   - `src/main/resources/datos-demo.sql` (datos de demo, no se ejecuta automáticamente).
   - En `src/main/resources/static/index.html`: la sección de productos y la pestaña de Auditoría.

   No tocar archivos de Categoría, UnidadMedida ni Proveedor sin preguntar antes (tampoco para
   anotarlos con `@Auditable`: eso lo hace cada compañero).
2. Los commits NO deben llevar "Co-Authored-By: Claude" ni "Generated with Claude Code".
   Mensajes de commit en español, en imperativo (ej. "Agrega endpoint para desactivar productos").
3. No hacer push sin que el usuario lo pida.
4. No modificar `src/main/resources/application.properties` (los compañeros usan otra configuración:
   puerto 3307). Las pruebas usan su propio `src/test/resources/application.properties` con H2.

## Cómo ejecutar en esta máquina

MySQL (XAMPP) corre en el puerto **3306**, usuario `root` sin contraseña. Se sobrescribe la URL
por argumento en lugar de editar `application.properties`:

```
mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.datasource.url=jdbc:mysql://localhost:3306/inventario_hospital?createDatabaseIfNotExist=true"
```

- Compilar: `mvnw.cmd compile`
- Todas las pruebas (usan H2 en memoria, no requieren MySQL): `mvnw.cmd test`
- Solo las de Producto: `mvnw.cmd test -Dtest=ProductoServiceTest`

### Datos de demo

`src/main/resources/datos-demo.sql` (5 categorías, 5 unidades, 3 proveedores ficticios y 15 productos,
3 de ellos inactivos). No se ejecuta al arrancar. Para cargarlo o recargarlo (la app debe haber
arrancado antes al menos una vez para que existan las tablas):

```
C:\xampp\mysql\bin\mysql.exe -uroot -P3306 --default-character-set=utf8mb4 inventario_hospital < src\main\resources\datos-demo.sql
```

Desde PowerShell, que no admite `<`:

```
Get-Content -Raw -Encoding UTF8 src\main\resources\datos-demo.sql | C:\xampp\mysql\bin\mysql.exe -uroot -P3306 --default-character-set=utf8mb4 inventario_hospital
```

Es re-ejecutable: borra solo sus propias filas (por código, RUC o nombre exacto) y las vuelve a insertar.

## Plan de trabajo (rama `feature/productos-eval02`, sin push)

Un commit por paso. Estado al último commit de esta rama:

| # | Paso | Estado | Commit |
|---|---|---|---|
| 0 | CLAUDE.md, compilar y arrancar con MySQL 3306 | ✅ | `cf4c9a3` |
| 1 | Excepciones propias + `GlobalExceptionHandler` ampliado | ✅ | `eec7d89` |
| — | Reparto del equipo en CLAUDE.md | ✅ | `1181967` |
| 2 | `@Size` en textos y `@NotNull` en categoría y unidad | ✅ | `f9548a8` |
| 3 | `ProductoService`: `@Transactional`, `saveAndFlush`, reglas de stock, relaciones inactivas, PUT ignora `activo` | ✅ | `3e9391e` |
| 4 | RF-INV-03: `PATCH /{id}/activar` y `/desactivar` (409 si ya está en ese estado) | ✅ | `0cf786f` |
| 5 | RF-INV-14: `GET /api/productos` con filtros combinables; LEFT JOIN FETCH | ✅ | `9243447` |
| 6 | Frontend de productos: filtros, activar/desactivar, XSS, selects solo activos | ✅ | `c0753ff` |
| — | Plan en CLAUDE.md | ✅ | `6533448` |
| — | Datos de demo (`datos-demo.sql`) | ✅ | `17b7f34` |
| — | Columna Acciones fija (sticky) en la tabla de productos | ✅ | `8bd61c0` |
| 7 | `ProductoServiceTest` con Mockito (26 pruebas) | ✅ | `079fed3` |
| 8 | H2 para pruebas, `contextLoads` sin MySQL | ✅ | `210336b` |
| 9 | P2-B: `spring-boot-starter-aspectj`, entidad `Auditoria`, `@Auditable`, aspecto, evento AFTER_COMMIT guardado con REQUIRES_NEW, `UsuarioActualProvider` ("sistema" + TODO), anotar `ProductoService`, pendientes del equipo | ✅ | `a0c5def` |
| 10 | P2-C: `GET /api/auditoria` con filtros (entidad, operación, usuario, rango de fechas), más reciente primero, paginado (50 por defecto) con DTO propio (no serializar `Page`) | ✅ | (este commit) |
| 11 | P2-D: pestaña Auditoría en `index.html` (glassmorphism, sin XSS) | pendiente | |
| 12 | P2-E: test de integración "registrar producto genera auditoría" (no `@Transactional`; limpiar en `@AfterEach`) | pendiente | |

Decisiones ya tomadas (no volver a preguntar):

- Categoría y unidad obligatorias (`@NotNull`); proveedor opcional. Se cargan con LEFT JOIN FETCH
  para no ocultar productos antiguos sin categoría.
- Se conserva una relación que el producto ya tenía aunque luego se inactive; no se puede asignar
  una inactiva nueva.
- Desactivar = eliminación lógica. En auditoría: operación `DESACTIVAR`, detalle "Eliminación lógica".
- Auditoría: el aspecto (`@AfterReturning` sobre `@Auditable`) captura el usuario al crear el evento;
  un `@TransactionalEventListener(AFTER_COMMIT, fallbackExecution = true)` lo guarda con
  `REQUIRES_NEW`. Solo se audita lo confirmado.
- Sin handler genérico `Exception` en `GlobalExceptionHandler`: convertiría el 403 de
  `@PreAuthorize` en 500.

## Pendientes del equipo (no corregir sin preguntar)

### Auditoría (P2): cómo auditar sus propios métodos

La infraestructura está en `aop/` (`@Auditable`, `AuditoriaAspect`, `AuditoriaListener`,
`UsuarioActualProvider`) y la bitácora en la tabla `auditoria`. Cada compañero anota **sus** métodos
de servicio (Willard no los toca):

```java
@Transactional
@Auditable(entidad = "Categoria", operacion = "REGISTRAR")
public Categoria registrar(Categoria categoria) { ... }

@Transactional
@Auditable(entidad = "Proveedor", operacion = "DESACTIVAR", detalle = "Eliminación lógica")
public Proveedor desactivar(Long id) { ... }
```

- Operaciones acordadas: `REGISTRAR`, `MODIFICAR`, `ACTIVAR`, `DESACTIVAR` (eliminación lógica).
  Entidad en singular y sin tildes: `Producto`, `Categoria`, `UnidadMedida`, `Proveedor`, `Usuario`.
- El id se toma del objeto devuelto (`getId()`); si el método no devuelve la entidad, del primer
  argumento `Long`. Conviene devolver la entidad.
- Poner `@Transactional` en el método: así la auditoría se guarda solo si el commit funciona
  (sin transacción también se audita, pero en el momento).
- El método debe ser `public` y llamarse desde otro bean (el controlador). Una llamada interna
  `this.metodo()` no pasa por el aspecto.
- `ProveedorServiceImpl` implementa una interfaz: la anotación va en el método de la **clase**
  `ProveedorServiceImpl`, no en la interfaz.
- Solo se auditan operaciones que terminan sin excepción.
- **Mijael (P5)**: cuando exista Spring Security, cambiar solo `UsuarioActualProvider.obtenerUsuario()`
  para leer el `SecurityContextHolder` (el TODO del archivo trae el código). Además, proteger
  `GET /api/auditoria` solo para ADMINISTRADOR.

### Otros pendientes

- XSS en `index.html`: las secciones de Categorías y Proveedores construyen el `onclick` con
  `JSON.stringify(...)` dentro del atributo HTML; un nombre con `&quot;` permite inyectar JavaScript.
  (En Productos ya se corrigió.)
- `Categoria` y `UnidadMedida` no tienen `@NotBlank` y sus controladores no usan `@Valid`.
- Inconsistencias: paquetes `model`/`models`/`entity`/`impl` mezclados, tres estilos de Lombok y
  tres convenciones de estado (`Boolean estado`, `Boolean activo`, `String "ACTIVO"`).
- Se agregó H2 (`com.h2database:h2`, scope test) solo para pruebas, con
  `src/test/resources/application.properties` (H2 en memoria, modo MySQL, `ddl-auto=create-drop`).
  `contextLoads` y las pruebas de integración ya no necesitan MySQL. No afecta a la app ni a
  `src/main/resources/application.properties`.
- El commit `7759922` (ramas `feature/productos` y `feature/integracion`, repo público) expone una
  contraseña de MySQL en `application.properties`.
