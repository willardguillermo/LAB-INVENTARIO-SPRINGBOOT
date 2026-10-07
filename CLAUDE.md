# CLAUDE.md

## Contexto del proyecto

API REST de inventario hospitalario con Spring Boot 4 + JPA/Hibernate + MySQL (Java 21, Lombok).
Evaluación 02 del curso Desarrollo de Aplicaciones Web (Tecsup): arquitectura en capas
(Controller, Service, Repository), relaciones JPA, validaciones, manejo global de excepciones,
AOP y Spring Security con roles.

Incluye un panel web de prueba en `src/main/resources/static/index.html` (http://localhost:8080).
El README tiene los endpoints, las reglas de negocio de producto y cómo ejecutar y probar.

## Equipo y reparto de la evaluación

| Integrante | Módulo | Preguntas |
|---|---|---|
| willardguillermo | Producto (RF-INV-01, 02, 03, 14) | P1 con Producto (`@ManyToOne` a Categoría, Unidad de medida y Proveedor) + **P2 Auditoría** (AOP) |
| alexanderFaustino | Categoría y Unidad de medida (RF-INV-15, 16, 17) | P1 con `@OneToMany` en Categoría y Unidad + **P3** Usuarios y roles (backend) + **P4** frontend de usuarios y roles |
| mijaelino21-debug | Proveedor (RF-INV-20, 21, 22) | P1 con `@OneToMany` en Proveedor + **P5** Spring Security y control de acceso |

Cada integrante trabaja en una rama `feature/...` y lleva su parte a `main` por Pull Request.

## Roles acordados

| Rol | Permisos |
|---|---|
| ADMINISTRADOR | Todo (incluida la consulta de la bitácora de auditoría) |
| ALMACENERO | Gestiona productos, categorías y proveedores |
| MÉDICO | Solo consulta y búsqueda de productos |

Los métodos de `ProductoService` son públicos y no se llaman entre sí a través de `this`, así que
se pueden proteger con `@PreAuthorize` directamente.

## Estructura

- `controller/`, `service/`, `repository/`: capas habituales. `entity/`, `model/` y `models/`: entidades.
- `exception/`: `GlobalExceptionHandler` (todas las respuestas de error llevan `message`),
  `RecursoNoEncontradoException` (404) y `ReglaNegocioException` (400 o el estado que se indique).
  `ResponseStatusException` también se maneja y muestra su mensaje.
  No hay handler genérico de `Exception`: convertiría el 403 de `@PreAuthorize` en un 500.
- `aop/`: auditoría (ver abajo). `dto/PaginaRespuesta`: formato estable para respuestas paginadas.
- Pruebas: `mvnw test` usa H2 en memoria (`src/test/resources/application.properties`) y no
  necesita MySQL.
- `src/main/resources/datos-demo.sql`: datos de demostración opcionales; no se cargan solos.

## Auditoría (P2): cómo auditar sus propios métodos

La infraestructura está en `aop/` (`@Auditable`, `AuditoriaAspect`, `AuditoriaListener`,
`UsuarioActualProvider`) y la bitácora en la tabla `auditoria` (`GET /api/auditoria`).
Cada integrante anota los métodos de servicio de su módulo:

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
- Poner `@Transactional` en el método: la auditoría se guarda después del commit, así que una
  operación revertida no queda registrada (sin transacción también se audita, pero en el momento).
- El método debe ser `public` y llamarse desde otro bean (el controlador). Una llamada interna
  `this.metodo()` no pasa por el aspecto.
- Si el servicio implementa una interfaz (como `ProveedorServiceImpl`), la anotación va en el
  método de la **clase**, no en la interfaz.
- Solo se auditan operaciones que terminan sin excepción.
- El usuario sale de `UsuarioActualProvider.obtenerUsuario()`; hoy devuelve `"sistema"`.

## Pendientes del equipo

### Para P5 (Spring Security)

- Cambiar solo `UsuarioActualProvider.obtenerUsuario()` para leer el `SecurityContextHolder`
  (el TODO del archivo trae el código). El aspecto no necesita cambios.
- Proteger `GET /api/auditoria` solo para ADMINISTRADOR, y los endpoints de productos según la
  tabla de roles (MÉDICO: solo `GET /api/productos` y `GET /api/productos/{id}`).

### Generales

- XSS en `index.html`: las secciones de Categorías y Proveedores construyen el `onclick` con
  `JSON.stringify(...)` dentro del atributo HTML; un nombre con `&quot;` permite inyectar JavaScript.
  En Productos ya se corrigió (botones con `data-id` y los datos en un `Map`).
- `Categoria` y `UnidadMedida` no tienen `@NotBlank` y sus controladores no usan `@Valid`.
- Inconsistencias: paquetes `model`/`models`/`entity`/`impl` mezclados, tres estilos de Lombok y
  tres convenciones de estado (`Boolean estado`, `Boolean activo`, `String "ACTIVO"`).
- Se agregó H2 (`com.h2database:h2`, scope test) solo para pruebas, con
  `src/test/resources/application.properties` (H2 en memoria, modo MySQL, `ddl-auto=create-drop`).
  `contextLoads` y las pruebas de integración ya no necesitan MySQL. No afecta a la app ni a
  `src/main/resources/application.properties`.
- El commit `7759922` (ramas `feature/productos` y `feature/integracion`) expone una contraseña
  de MySQL en `application.properties`. Si esa contraseña se usa en otro lugar, conviene cambiarla.
