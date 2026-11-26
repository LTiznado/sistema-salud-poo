# Sistema de Salud — Java POO

Aplicación académica de escritorio para gestionar personas, usuarios y citas médicas. Conserva los formularios Swing del proyecto original y completa su conexión con una capa de servicios y acceso a datos mediante JDBC.

## Funciones

- Inicio de sesión por rol: administrador, doctor y paciente.
- Administración de personas, cuentas y roles; cambio de contraseña sin mostrar la anterior.
- Registro de pacientes en una transacción: persona, cuenta y rol se guardan juntos o se revierten.
- Agenda por turnos de 30 minutos, de 07:00 a 19:30, con persistencia y cancelación.
- Prevención de citas simultáneas para el mismo doctor o paciente, también con solicitudes concurrentes.
- Pacientes y doctores consultan únicamente sus citas. El administrador accede a la agenda completa.
- Comprobante de cita exportable a TXT y registro de reclamos con número de seguimiento.
- Modo demo local con datos ficticios y sin instalar un servidor de base de datos.

## Ejecutar en Windows

Requisito: **JDK 17** o superior, disponible mediante `JAVA_HOME` o `PATH`. La primera compilación requiere Internet para descargar Maven y las dependencias. Maven Wrapper está incluido.

```powershell
.\mvnw.cmd clean verify
.\ejecutar.cmd
```

También puedes abrir `pom.xml` en IntelliJ o NetBeans como proyecto Maven, y ejecutar `Principal.Main`.

Para usar el JAR desde cualquier sistema:

```sh
java -jar target/sistema-salud-poo-1.0.0.jar
```

El modo demo crea su base H2 en `data/` dentro del directorio de ejecución. Los cambios se conservan al cerrar y abrir. No abre un servidor web ni requiere puertos de entrada. Si lo ejecutas desde otra carpeta, se crea una base demo independiente en esa carpeta.

### Cuentas de demostración

| Rol | Documento | Contraseña ficticia |
|---|---|---|
| Administrador | `00000001` | `DemoSalud2026!` |
| Doctor | `00000002` | `DemoSalud2026!` |
| Paciente | `00000003` | `DemoSalud2026!` |

Estas cuentas solo se crean automáticamente en el modo demo; no se generan en una instalación MySQL. La base contiene identidades inventadas. No uses el modo demo para datos reales.

1. Selecciona el rol e inicia sesión.
2. Administrador: **Administrador → Gestión de Personas / Gestión de Usuarios / Gestión de Citas**.
3. Doctor: **Doctor → Gestión de Citas**.
4. Paciente: **Paciente → Agendar Cita / Consultar Cita**. Ambos accesos llevan a la agenda personal.
5. Para registrar un paciente nuevo usa **Registrarse** desde el inicio. Un documento ya existente requiere la atención del administrador; el autorregistro no permite apropiarse de una identidad registrada.
6. Para crear un doctor, el administrador registra primero a la persona y luego asigna el rol Doctor desde Gestión de Usuarios.
7. En la agenda puedes guardar el comprobante o registrar un reclamo. El comprobante no acredita un pago.

## MySQL: instalación nueva opcional

El modo MySQL está previsto para **una base nueva de MySQL 8.0.16 o superior**. No se ha probado contra un servidor MySQL real en esta entrega; las pruebas de integración usan H2 con modo de compatibilidad MySQL. No se incluye el dump original con datos personales y claves antiguas, ni se migra automáticamente una instalación anterior.

1. Crea una base vacía y un usuario específico con los permisos necesarios. No utilices `root` para la aplicación.
2. Con una herramienta MySQL y esa base seleccionada, ejecuta una sola vez `database/instalacion-mysql.sql`. Crea tablas y catálogos, sin cuentas ni contraseñas conocidas. No contiene `DROP TABLE`.
3. Configura las variables en la terminal que lanzará el programa:

```powershell
$env:SALUD_DB_MODE = 'mysql'
$env:SALUD_DB_URL = 'jdbc:mysql://localhost:3306/salud_poo?connectionTimeZone=LOCAL'
$env:SALUD_DB_USER = 'usuario_de_la_aplicacion'
# Configura SALUD_DB_PASSWORD con tu contraseña local; no la guardes en el repositorio.
```

4. Desde una terminal interactiva, crea el primer administrador. La contraseña se pide sin mostrarla; solo permite una instalación sin usuarios:

```powershell
java -cp target/sistema-salud-poo-1.0.0.jar Principal.CrearAdministrador
```

5. Ejecuta el programa con `ejecutar.cmd`. Para verificar únicamente la conexión:

```powershell
.\ejecutar.cmd --check
```

También se aceptan propiedades Java `salud.db.mode`, `salud.db.url`, `salud.db.user` y `salud.db.password`. Evita pasar contraseñas en argumentos de procesos. El programa no carga archivos `.env` automáticamente.

## Estructura

```text
src/main/java/
  Principal/     Arranque y alta inicial de administrador
  Conexion/      Conexión configurable e inicialización de demo
  Modelo/        Entidades originales y sesión
  Controlador/   Controladores de los formularios
  Servicio/      Registro transaccional y reglas de citas
  DAO/           Consultas JDBC parametrizadas
  Helpers/       Validaciones y protección de contraseñas
  Vista/         Formularios Swing y agenda integrada
src/main/resources/db/   Esquema y catálogos sin datos privados
src/test/java/         Pruebas de seguridad y de flujos
```

## Cambios respecto al proyecto de 2022

- Entrada corregida a `Principal.Main`, creada en el hilo de eventos de Swing.
- Compilación reproducible con Maven y dependencias declaradas: JCalendar, AbsoluteLayout, MySQL y H2.
- Credenciales de conexión externas al código. Se elimina el estado global de conexión y se cierran los recursos JDBC.
- Contraseñas con PBKDF2-HMAC-SHA256, 600 000 iteraciones y sal aleatoria de 16 bytes; no se aceptan claves heredadas en texto plano.
- Consultas parametrizadas, claves generadas por JDBC y restricción de documentos duplicados en todo el sistema (el acceso identifica por número).
- Registro y asignación de roles transaccionales; corrección de fecha vacía y doble clic sin fila seleccionada.
- Agenda integrada, permisos por sesión, comprobantes y reclamos persistidos.
- Se mantienen los `.form` y los formularios antiguos de citas/boleta/reclamos como referencia del trabajo original. La navegación operativa utiliza `GestionCitas`; esas pantallas antiguas no representan funciones adicionales completadas.
- No se incluyen clases compiladas antiguas, preferencias privadas de NetBeans, rutas de otro equipo ni el dump original.

## Validación y límites

Ejecuta `mvnw.cmd clean verify` para compilar, correr las pruebas y generar un JAR con dependencias. GitHub Actions ejecuta el mismo proceso con Java 17.

Las pruebas cubren hash y validación de contraseñas, acceso por rol, inyección SQL, registro y rollback, citas concurrentes, privacidad, cancelación, comprobantes, reclamos y construcción de la agenda Swing en el hilo de eventos. Consulta `docs/VALIDACION.md` para los resultados reales de la entrega.

Es un proyecto académico: no incluye pagos, historia clínica, notificaciones, gestión de especialidades/feriados, duración variable de consultas ni seguimiento administrativo de reclamos. Las consultas de los formularios se ejecutan en el hilo Swing, por lo que una base remota lenta puede bloquear temporalmente la interfaz. No está validado para uso clínico en producción. Las horas son locales al equipo y a la sesión de base de datos; no se gestionan zonas horarias múltiples.
