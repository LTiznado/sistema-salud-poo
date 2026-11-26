# Validación de la entrega — 26 de noviembre de 2026

## Comprobaciones realizadas

- JDK 17, Maven Wrapper y dependencias descargadas desde Maven Central.
- `mvnw.cmd clean verify`: **BUILD SUCCESS**.
- **29 pruebas: 0 fallos, 0 errores y 0 omitidas**:
  - `FlujosTest`: 25.
  - `FormFieldsTest`: 1.
  - `PasswordsTest`: 3.
- JAR con dependencias generado en `target/sistema-salud-poo-1.0.0.jar`.
- Arranque del JAR mediante `--check`, desde una base demo nueva: correcto.
- Segundo arranque con la misma base de archivo: correcto, sin duplicar cuentas.
- Revisión visual en Windows de la agenda integrada y del menú Administrador → Gestión de Citas. Se usó una instancia de prueba con sesión demo y base en memoria; no se automatizó la entrada de credenciales.
- Ajustados tamaño del texto, fondo y encaje de formularios en el escritorio Swing.
- Los formularios de agenda se construyen para los tres roles en una prueba automatizada ejecutada en el hilo de eventos.
- Revisión de archivos preparados para Git: sin base de datos local, dump original, clases antiguas ni configuración privada de NetBeans. Sin coincidencias en la búsqueda de patrones conocidos de tokens y claves privadas; no equivale a una auditoría exhaustiva.

## Casos cubiertos

Hash con sal aleatoria, contraseñas incorrectas y formato inválido; acceso por rol y estado activo; intentos de inyección SQL; alta de pacientes y reversión completa ante fallos; imposibilidad de apropiarse de un documento existente; creación inicial de administrador; cambio de contraseña sin recuperarla en pantalla; apellidos con apóstrofo; documentos de pasaporte y contraseñas largas; validación de fecha, correo y documento; edición y pegado en campos; reservas de citas; colisión simultánea por doctor y por paciente; dos reservas concurrentes; cancelación y liberación del turno; aislamiento de citas entre pacientes; restricción de listas y comprobantes; sesiones revocadas y doctores inactivos; turnos y motivos inválidos; reclamos persistidos.

## Límites comprobados y pendientes

- Las pruebas de integración usan **H2 en modo MySQL**. No se probó la instalación contra un servidor MySQL real y no se ejecutaron operaciones en bases de datos del usuario.
- El SQL suministrado es para una instalación nueva. No migra la base original de 2022 ni sus contraseñas en texto plano.
- Los formularios Swing originales de personas, usuarios y login conservan en parte su presentación. La revisión visual no fue una prueba manual exhaustiva de todos sus controles; los servicios y validaciones se cubren por pruebas automatizadas.
- El compilador conserva advertencias por mapas y controles genéricos sin tipar del proyecto original. El empaquetado informa el solapamiento esperado de `META-INF/MANIFEST.MF`; el manifiesto final se genera con `Principal.Main`.
- No se implementan cobros, historia clínica, notificaciones, calendarios de feriados ni tramitación administrativa de reclamos.
- Es una aplicación académica; no está validada para operar con datos clínicos reales en producción.