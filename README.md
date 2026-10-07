# LAB-INVENTARIO-SPRINGBOOT

API REST para el módulo de **Inventario y Almacén hospitalario**, desarrollada con Spring Boot 4,
JPA/Hibernate y MySQL. Proyecto de la Evaluación 02 del curso Desarrollo de Aplicaciones Web (Tecsup).

- Arquitectura en capas: Controller → Service → Repository.
- Entidades relacionadas: un **Producto** pertenece a una **Categoría** y a una **Unidad de medida**
  (obligatorias) y puede tener un **Proveedor** (opcional).
- Validaciones con Bean Validation y reglas de negocio en la capa de servicio.
- Manejo global de excepciones con respuestas JSON uniformes.
- **Auditoría con AOP**: cada registro, modificación, activación y desactivación queda en una
  bitácora consultable (usuario, fecha y hora, operación, entidad e id del registro).
- **Spring Security con roles** (ADMINISTRADOR, MEDICO, RECEPCIONISTA): login con usuario y
  contraseña, control de acceso por rol a nivel de método y de URL, y usuarios/roles gestionables
  desde el propio panel.
- Panel web de prueba en `http://localhost:8080` (pestañas de Productos, Categorías, Unidades de
  medida, Proveedores y Auditoría; Usuarios y Roles solo para ADMINISTRADOR).

**Tecnologías:** Java 21, Spring Boot 4.1, Spring Data JPA, Hibernate, MySQL, Spring Security,
Thymeleaf, Lombok, AspectJ, JUnit 5, Mockito y H2 (solo para pruebas).

## Requisitos

- JDK 21
- MySQL o MariaDB (por ejemplo, el de XAMPP)
- No hace falta instalar Maven: el proyecto trae el wrapper (`mvnw` / `mvnw.cmd`).

## Cómo ejecutar

La conexión se configura en `src/main/resources/application.properties`
(por defecto `jdbc:mysql://localhost:3306/inventario_hospital`, usuario `root` sin contraseña).
Hibernate crea y actualiza las tablas al arrancar (`ddl-auto=update`).

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

Si tu MySQL usa otro puerto u otras credenciales, puedes indicarlos al arrancar sin editar el
archivo (no edites `application.properties`: el equipo usa el puerto 3306). Por ejemplo, con MySQL
en el puerto 3307 y creando la base si no existe:

```bash
mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.datasource.url=jdbc:mysql://localhost:3307/inventario_hospital?createDatabaseIfNotExist=true"
```

Si usas un puerto distinto al del equipo de forma habitual (por ejemplo, XAMPP en otra PC), conviene
guardar ese comando en un `CLAUDE.local.md` propio (está en `.gitignore`) en vez de tocar
`application.properties`.

La API queda en `http://localhost:8080/api` y el panel web en `http://localhost:8080` (pide
iniciar sesión primero; ver la sección de **Seguridad y roles**).

## Seguridad y roles

El panel web (`http://localhost:8080`) pide iniciar sesión en `/login`. Después de autenticarse,
`/` muestra el panel y se adapta según el rol: el usuario y su rol se ven arriba, con un botón
**Cerrar sesión**; **Usuarios** y **Roles** solo aparecen para ADMINISTRADOR; la pestaña
**Auditoría** y todos los formularios/botones de escritura (registrar, editar, activar, desactivar)
se ocultan para quien no sea ADMINISTRADOR.

`InicializadorRoles` crea al arrancar los roles `ADMINISTRADOR`, `MEDICO` y `RECEPCIONISTA`, y estos
usuarios de prueba si no existen:

| Usuario | Contraseña | Rol | Qué puede hacer |
|---|---|---|---|
| `admin` | `admin123` | ADMINISTRADOR | Todo: productos, categorías, unidades de medida, proveedores, usuarios, roles y la auditoría |
| `medico` | `medico123` | MEDICO | Solo consultar productos, categorías, unidades de medida y proveedores (sin escritura) |
| `recepcion` | `recepcion123` | RECEPCIONISTA | Puede iniciar sesión, pero no tiene ningún permiso concedido todavía en ningún módulo |

Un usuario con `estado = false`, o cuyo rol tenga `estado = false`, no puede iniciar sesión (login
rechazado con un mensaje distinto al de usuario/contraseña incorrectos).

`GET /api/sesion` devuelve los datos de la sesión activa (lo usa el panel):

```json
{ "username": "admin", "nombre": "Administrador del Sistema", "rol": "ADMINISTRADOR" }
```

Para cualquier ruta `/api/**`, una petición sin sesión responde `401` y sin permiso responde `403`,
ambas en JSON con el mismo formato que los demás errores (ver abajo). El resto de páginas (las que
no empiezan con `/api/`) redirige a `/login` en vez de devolver JSON.

## Endpoints

Todas las respuestas de error tienen el mismo formato:

```json
{ "timestamp": "2026-10-07T03:05:00Z", "status": 404, "error": "Not Found",
  "message": "Producto no encontrado con id: 99" }
```

Los errores de validación agregan el detalle por campo en `errores`:

```json
{ "status": 400, "error": "Bad Request", "message": "Datos inválidos",
  "errores": { "nombre": "El nombre del producto es obligatorio",
               "categoria": "La categoría es obligatoria" } }
```

### Productos (`/api/productos`)

| Método | Ruta | Descripción | Requisito |
|---|---|---|---|
| `POST` | `/api/productos` | Registrar un producto | RF-INV-01 |
| `PUT` | `/api/productos/{id}` | Modificar un producto | RF-INV-02 |
| `PATCH` | `/api/productos/{id}/activar` | Activar un producto | RF-INV-03 |
| `PATCH` | `/api/productos/{id}/desactivar` | Desactivar un producto (eliminación lógica) | RF-INV-03 |
| `GET` | `/api/productos` | Buscar y filtrar productos | RF-INV-14 |
| `GET` | `/api/productos/{id}` | Consultar un producto por id | |

**Registrar** (`POST /api/productos`). Las relaciones se envían solo con su `id`:

```bash
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{
        "nombre": "Paracetamol 500 mg tableta x 100",
        "tipoProducto": "Medicamento",
        "codigo": "MED-101",
        "marca": "Genérico",
        "stockMinimo": 20, "puntoReposicion": 40, "stockMaximo": 200,
        "manejaLote": true, "manejaVencimiento": true,
        "categoria": { "id": 1 },
        "unidadMedida": { "id": 2 },
        "proveedor": { "id": 1 }
      }'
```

Respuesta `201 Created` con el producto, sus relaciones completas y `"activo": true`.

**Modificar** (`PUT /api/productos/{id}`). Se envía el producto completo; el campo `activo` se ignora:

```bash
curl -X PUT http://localhost:8080/api/productos/1 \
  -H "Content-Type: application/json" \
  -d '{ "nombre": "Paracetamol 500 mg tableta x 100", "tipoProducto": "Medicamento",
        "stockMinimo": 30, "puntoReposicion": 60, "stockMaximo": 250,
        "manejaLote": true, "manejaVencimiento": true,
        "categoria": { "id": 1 }, "unidadMedida": { "id": 2 } }'
```

**Activar / desactivar** (sin cuerpo):

```bash
curl -X PATCH http://localhost:8080/api/productos/1/desactivar
curl -X PATCH http://localhost:8080/api/productos/1/activar
```

Si el producto ya está en ese estado responde `409`, por ejemplo
`"El producto 'Paracetamol 500 mg tableta x 100' ya está inactivo"`.

**Buscar y filtrar** (`GET /api/productos`). Todos los filtros son opcionales y se combinan entre sí;
sin filtros devuelve todos los productos ordenados por id.

| Parámetro | Tipo | Filtra por |
|---|---|---|
| `nombre` | texto | nombre que contiene el texto (sin distinguir mayúsculas) |
| `tipo` | texto | tipo de producto que contiene el texto |
| `activo` | `true` / `false` | estado |
| `categoriaId` | número | categoría |
| `proveedorId` | número | proveedor |

```bash
curl "http://localhost:8080/api/productos?nombre=para&tipo=medicamento&activo=true&categoriaId=1"
```

**Consultar por id:**

```bash
curl http://localhost:8080/api/productos/1
```

### Auditoría (`/api/auditoria`)

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/auditoria` | Consultar la bitácora, del registro más reciente al más antiguo |

La bitácora es de solo lectura: los registros los crea automáticamente el aspecto de auditoría.

| Parámetro | Tipo | Descripción |
|---|---|---|
| `entidad` | texto | `Producto`, `Categoria`, `UnidadMedida`, `Proveedor`, `Usuario`, `Rol` |
| `operacion` | texto | `REGISTRAR`, `MODIFICAR`, `ACTIVAR`, `DESACTIVAR`, `ELIMINAR` |
| `usuario` | texto | usuario que contiene el texto |
| `desde`, `hasta` | fecha `AAAA-MM-DD` | rango de fechas, ambos días incluidos |
| `pagina` | número | página, empieza en 0 (por defecto 0) |
| `tamanio` | número | registros por página, de 1 a 200 (por defecto 50) |

```bash
curl "http://localhost:8080/api/auditoria?entidad=Producto&operacion=DESACTIVAR&desde=2026-10-01&hasta=2026-10-31"
```

```json
{
  "contenido": [
    { "id": 3, "usuario": "admin", "fechaHora": "2026-10-06T22:03:47",
      "operacion": "DESACTIVAR", "entidad": "Producto", "registroId": 39,
      "detalle": "Eliminación lógica" }
  ],
  "pagina": 0, "tamanio": 50, "total": 1, "totalPaginas": 1
}
```

El usuario registrado es el que tiene la sesión activa; si no hay sesión (no debería pasar detrás
de Spring Security) queda como `sistema`. Solo ADMINISTRADOR puede consultar `/api/auditoria`.

### Otros módulos

| Ruta base | Operaciones | Lectura | Escritura |
|---|---|---|---|
| `/api/categorias` | listar, consultar por id, registrar, modificar | ADMINISTRADOR, MEDICO | ADMINISTRADOR |
| `/api/unidades-medida` | listar, consultar por id, registrar | ADMINISTRADOR, MEDICO | ADMINISTRADOR |
| `/api/proveedores` | listar, consultar por id, registrar, modificar, activar/desactivar, eliminar, productos del proveedor | ADMINISTRADOR, MEDICO | ADMINISTRADOR |
| `/api/usuarios` | listar, consultar por id, registrar, modificar, cambiar estado | ADMINISTRADOR | ADMINISTRADOR |
| `/api/roles` | listar, consultar por id, registrar, modificar, cambiar estado | ADMINISTRADOR | ADMINISTRADOR |

## Reglas de negocio de producto

- **Obligatorios:** `nombre` (máx. 150), `tipoProducto` (máx. 100), `categoria` y `unidadMedida`.
  El proveedor es opcional. Los demás textos respetan la longitud de su columna.
- **Código único:** dos productos no pueden tener el mismo `codigo` (`409`).
- **Stock coherente:** `stockMinimo ≤ puntoReposicion ≤ stockMaximo`, y ninguno negativo.
  Solo se comparan los valores informados.
- **Relaciones activas:** no se puede asignar una categoría, unidad de medida o proveedor inactivos.
  Si un producto ya tenía una relación que después se inactivó, se puede seguir editando sin cambiarla.
- **Estado:** todo producto se registra activo. El estado solo cambia con `activar` / `desactivar`;
  el `PUT` ignora el campo `activo`. Desactivar es la eliminación lógica (no hay `DELETE`).
- **Productos antiguos:** si un producto registrado antes de que la categoría y la unidad fueran
  obligatorias no las tiene, para cambiar su estado primero hay que editarlo y asignárselas.
- **Auditoría:** registrar, modificar, activar y desactivar quedan en la bitácora con usuario,
  fecha y hora, operación, entidad e id. Solo se auditan las operaciones que se confirmaron en la
  base de datos; un intento fallido no deja registro.

## Datos de demostración

`src/main/resources/datos-demo.sql` carga 5 categorías, 5 unidades de medida, 3 proveedores
ficticios y 15 productos hospitalarios (medicamentos, insumos y material médico; 3 de ellos inactivos).

- **No** se ejecuta automáticamente al arrancar.
- Las tablas deben existir: arranca la app al menos una vez antes de cargarlo.
- Se puede ejecutar varias veces: solo borra y vuelve a crear sus propios registros.

```bash
mysql -u root -P 3306 --default-character-set=utf8mb4 inventario_hospital < src/main/resources/datos-demo.sql
```

Con XAMPP en Windows, el cliente está en `C:\xampp\mysql\bin\mysql.exe`. Desde PowerShell, que no
admite `<`:

```powershell
Get-Content -Raw -Encoding UTF8 src\main\resources\datos-demo.sql | C:\xampp\mysql\bin\mysql.exe -uroot -P3306 --default-character-set=utf8mb4 inventario_hospital
```

Cambia el puerto (`3306`) por el de tu MySQL.

## Pruebas

Las pruebas usan una base **H2 en memoria** (`src/test/resources/application.properties`), así que
**no necesitan MySQL**.

```bash
# Todas las pruebas
mvnw.cmd test            # Windows
./mvnw test              # Linux / macOS

# Solo las pruebas unitarias de ProductoService (Mockito)
mvnw.cmd test -Dtest=ProductoServiceTest

# Solo la prueba de integración de la auditoría
mvnw.cmd test -Dtest=AuditoriaIntegracionTest
```

| Clase | Tipo | Qué verifica |
|---|---|---|
| `ProductoServiceTest` | Unitaria (Mockito) | Registro, modificación, activación/desactivación, búsqueda, regla de stock y relaciones obligatorias e inactivas |
| `AuditoriaIntegracionTest` | Integración (Spring + H2) | Registrar genera su auditoría, desactivar se registra como eliminación lógica y una operación fallida no se audita |
| `LabInventarioSpringbootApplicationTests` | Integración | El contexto de Spring arranca |
