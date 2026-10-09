# CLAUDE.md

## Contexto del proyecto

API REST de inventario hospitalario con Spring Boot 4 + JPA/Hibernate + MySQL (Java 21, Lombok).
Evaluación 02 del curso Desarrollo de Aplicaciones Web (Tecsup): arquitectura en capas
(Controller, Service, Repository), relaciones JPA, validaciones, manejo global de excepciones,
AOP y Spring Security con roles.

Incluye un panel web en `src/main/resources/static/index.html` (http://localhost:8080). Los estilos
están en `static/css/tema.css` (variables en `:root`), compartidos con las plantillas Thymeleaf a
través de `fragments/layout.html` (fragmentos `estilos`, `barra(activo)` y `confirmacion`).
El README tiene los endpoints, las reglas de negocio de producto y cómo ejecutar y probar.

## Equipo y reparto de la evaluación

| Integrante | Módulo | Preguntas |
|---|---|---|
| willardguillermo | Producto (RF-INV-01, 02, 03, 14) | P1 con Producto (`@ManyToOne` a Categoría, Unidad de medida y Proveedor) + **P2 Auditoría** (AOP) |
| alexanderFaustino | Categoría y Unidad de medida (RF-INV-15, 16, 17) | P1 con `@OneToMany` en Categoría y Unidad + **P3** Usuarios y roles (backend) + **P4** frontend de usuarios y roles |
| mijaelino21-debug | Proveedor (RF-INV-20, 21, 22) | P1 con `@OneToMany` en Proveedor + **P5** Spring Security y control de acceso |

Cada integrante trabaja en una rama `feature/...` y lleva su parte a `main` por Pull Request.

## Roles reales (P5, ya implementados)

El rol ALMACENERO que se había planeado no se llegó a implementar; el equipo terminó con estos tres:

| Rol | Permisos |
|---|---|
| ADMINISTRADOR | Todo: productos, categorías, unidades de medida, proveedores, usuarios, roles y la bitácora de auditoría (`GET /api/auditoria`) |
| MEDICO | Solo lectura de productos, categorías, unidades de medida y proveedores |
| RECEPCIONISTA | Puede iniciar sesión pero no tiene ningún permiso asignado todavía en ningún módulo (sirve para demostrar que el control de acceso por rol funciona también cuando un rol no tiene nada concedido) |

Usuario inicial: `admin` / `admin123`. También hay usuarios de demostración `medico`/`medico123` y
`recepcion`/`recepcion123` (los crea `InicializadorRoles` si no existen). Ver el README para el
detalle de qué ve y qué puede hacer cada rol en el panel y en la API.

Control de acceso en dos niveles:
- **Método** (`@PreAuthorize`, `SecurityConfig` con `@EnableMethodSecurity`): en `ProductoController`
  (los métodos de `ProductoService` son públicos y no se llaman entre sí a través de `this`, así que
  se pueden proteger con `@PreAuthorize` directamente).
- **URL** (`SecurityConfig.filterChain`, `authorizeHttpRequests`): para `/usuarios/**`, `/roles/**`,
  `/api/usuarios/**`, `/api/roles/**`, `/api/auditoria/**` (solo ADMINISTRADOR) y para la
  lectura/escritura de `/api/categorias`, `/api/unidades-medida` y `/api/proveedores`.

Para `/api/**`, una petición sin sesión responde `401` en JSON y sin permiso responde `403` en JSON
(`SecurityConfig.authenticationEntryPoint()`/`accessDeniedHandler()`); el resto de rutas (las
plantillas Thymeleaf) sigue el flujo normal de `formLogin` y redirige a `/login`. Ambos handlers
distinguen por el prefijo de la URL a propósito: registrar el entry point de la API con
`defaultAuthenticationEntryPointFor` hacía que Spring lo usara también como *fallback* general
(el primer mapeo registrado gana cuando ninguno coincide) y rompía el redirect a `/login` de las
páginas Thymeleaf — por eso el filtro se arma a mano en vez de con ese método.

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

- Operaciones acordadas: `REGISTRAR`, `MODIFICAR`, `ACTIVAR`, `DESACTIVAR` (eliminación lógica) y
  `ELIMINAR` (borrado físico; hoy solo lo usa `ProveedorServiceImpl.eliminar`).
  Entidad en singular y sin tildes: `Producto`, `Categoria`, `UnidadMedida`, `Proveedor`, `Usuario`,
  `Rol`. Cuando un método "cambia el estado" sin distinguir activar/desactivar en el código (p. ej.
  `cambiarEstado` en Usuario, Rol y Proveedor, que reciben el estado nuevo o alternan un booleano),
  se audita como `MODIFICAR` con `detalle = "Cambio de estado"`: la anotación es estática y no puede
  leer el resultado para decidir entre `ACTIVAR`/`DESACTIVAR`.
- El id se toma del objeto devuelto (`getId()`); si el método no devuelve la entidad, del primer
  argumento `Long`. Conviene devolver la entidad.
- Poner `@Transactional` en el método: la auditoría se guarda después del commit, así que una
  operación revertida no queda registrada (sin transacción también se audita, pero en el momento).
- El método debe ser `public` y llamarse desde otro bean (el controlador). Una llamada interna
  `this.metodo()` no pasa por el aspecto.
- Si el servicio implementa una interfaz (como `ProveedorServiceImpl`), la anotación va en el
  método de la **clase**, no en la interfaz.
- Solo se auditan operaciones que terminan sin excepción.
- El usuario sale de `UsuarioActualProvider.obtenerUsuario()`: lee el usuario autenticado del
  `SecurityContextHolder` y solo devuelve `"sistema"` si no hay sesión (p. ej. una tarea en
  segundo plano, que hoy no existe, pero deja la puerta abierta).

## Pendientes del equipo

### Generales

- Inconsistencias: paquetes `model`/`models`/`entity`/`impl` mezclados, tres estilos de Lombok y
  tres convenciones de estado (`Boolean estado`, `Boolean activo`, `String "ACTIVO"`).
- Se agregó H2 (`com.h2database:h2`, scope test) solo para pruebas, con
  `src/test/resources/application.properties` (H2 en memoria, modo MySQL, `ddl-auto=create-drop`).
  `contextLoads` y las pruebas de integración ya no necesitan MySQL. No afecta a la app ni a
  `src/main/resources/application.properties`.
- El commit `7759922` (ramas `feature/productos` y `feature/integracion`) expone una contraseña
  de MySQL en `application.properties`. Si esa contraseña se usa en otro lugar, conviene cambiarla.
