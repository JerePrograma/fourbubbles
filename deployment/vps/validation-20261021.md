# Validación local de la demo VPS — 21/10/2026

Base Git: `ea616ef2604ccefb2986d9ba5b682257a0635043`, rama `main`. Cambios locales de hosting, bootstrap y parches de dependencias; esta evidencia precede al commit y al despliegue remoto.

## Controles ejecutados

| Control | Resultado real |
| --- | --- |
| Maven `clean package` y `package` después de parches | PASS; Java 21, Spring Boot 3.5.16 |
| Pruebas unitarias backend | 36 tests, cero fallos/errores, incluidos cuatro de bootstrap demo |
| Frontend `npm ci`, lint, test, build con base `/fourbubbles/` | PASS; 11 tests en cuatro archivos |
| `npm audit --omit=dev` después del parche Router | Cero altos/críticos; dos moderados descritos en README |
| Compose de VPS `config --quiet` | PASS |
| Docker frontend final | Construcción PASS; prefijos compilados correctos |
| Runtime backend final | Imagen con el JAR compilado localmente y Temurin 21; arranque y health PASS |
| Sintaxis shell/Python y `git diff --check` | PASS |
| Flyway | Once migraciones exitosas; Hibernate valida el esquema |
| `/health` | HTTP 200, `UP`, incluye readiness del backend y DB |
| `/fourbubbles/` | HTML 200 |
| Dos assets reales de `/fourbubbles/assets/` | HTTP 200, JS/CSS reales, sin fallback HTML |
| `/fourbubbles/production/metrics` | Fallback SPA 200 |
| Asset inexistente, raíz `/` y Actuator público | HTTP 404 |
| API prefijada `/fourbubbles-api/api` | Login, lectura y seed PASS |
| Acceso anónimo a clientes | HTTP 401 |
| Visor `REPORT_VIEWER` intentando crear cliente válido | HTTP 403 |
| Cookie de sesión | Nombre/ruta exclusivos, Secure, HttpOnly y SameSite Strict |
| Refresh y logout | Rotación PASS, refresh previo rechazado, logout revoca refresh |
| Seed repetido | Tres clientes ficticios, tres pedidos y dos ciclos completos; sin duplicación |
| Backup/restauración | Dump generado e índice validado; restauración real en la DB demo y servicios saludables |
| Seed y auth después de restaurar | PASS; mantienen tres pedidos y dos ciclos |

Los fixtures están marcados como sintéticos; ningún ciclo corresponde a actividad física real. No se importaron datos privados ni se conectaron proveedores.

## Consumo observado

Después de restauración y smoke, en reposo local:

- backend: aproximadamente 323 MiB de 768 MiB;
- PostgreSQL: aproximadamente 49 MiB de 384 MiB;
- frontend: aproximadamente 14 MiB de 64 MiB.

Es una muestra local puntual con datos pequeños, no una prueba de carga ni una medición del VPS.

## Auditoría Maven y límites

Consulta real a la API pública OSV para **96 coordenadas Maven de runtime**. Después de actualizar Tomcat, Jackson, JDBC, Log4j y Commons Lang, únicamente permanecen los dos avisos de Spring MVC detallados en el README: `GHSA-j9f9-w8pj-32f8` y `GHSA-pc63-qcmh-9cmg`, clasificados críticos por la base de avisos. Requieren SSE con FragmentsRendering o XsltView, capacidades que esta API no usa ni configura. La búsqueda de esos símbolos, vistas XSLT y endpoints `text/event-stream` en fuentes/configuración no produjo coincidencias. No se declara un escaneo sin hallazgos.

Las dos moderadas del frontend y los dos avisos MVC quedan documentados con su evaluación de alcance. No se ejecutó un escaneo de paquetes del sistema operativo de las imágenes ni una prueba de penetración.

## Artefactos locales de validación

- `scalaris/fourbubbles-backend:vps-validated`: `sha256:73e1c7006bb110f06ed82b5fa6fc83b8b1d97caaf37928b2bc086133d8cf48fd`.
- `scalaris/fourbubbles-frontend:vps-validated`: `sha256:95a54108a55dd30ef6577bcc5094867fd00f6ba5bb2a361c65efb57df74f86e7`.

El backend final se probó a partir del JAR que Maven compiló y testeó en esta PC, empaquetado en el mismo runtime Temurin. La construcción completa de fuente mediante el Dockerfile Maven debe ejecutarse y registrarse en el VPS para su commit definitivo; no se confunde el build anterior a los parches con una validación de la fuente final.

Credenciales, dump y resultados generados permanecen ignorados por Git. No se crearon commits ni se hizo push desde esta sub-tarea.

## Pendiente externo

Despliegue remoto, integración Caddy/Cloudflare conservando prefijos, programación diaria de backup, prueba HTTPS/navegador y registro de SHA publicado. El operador principal coordina estos pasos. Esta evidencia local no acredita producción remota ni uso de datos reales.

## Entrada pública de consulta

Se añadió posteriormente el botón exclusivo del build VPS «Entrar a la demo», utilizando como credencial deliberadamente pública la cuenta `REPORT_VIEWER` existente. `.env`, contraseñas persistidas y backend no se modificaron para este cambio. Se preservaron las funciones originales de login manual y logout.

Validaciones del cambio: lint PASS; 18 tests frontend PASS (siete nuevos para flags/configuración y aceptación exclusiva del rol de consulta, con revocación/rechazo de ADMIN, OPERATOR, roles mixtos o vacíos); build normal PASS; build público con base `/fourbubbles/` PASS. Las pruebas usan credenciales sintéticas expresamente públicas. La comprobación del botón mediante HTTPS queda a cargo de la activación remota del operador principal.

También pasó la construcción Docker del frontend con los argumentos de visor suministrados por Compose, sin imprimir la contraseña. Imagen local posterior de entrada pública: `sha256:d09c89e5a7e5a5ce3873e123e22929fcf876de8fb48323d1dc81066bf9975874`; reemplaza la etiqueta local del frontend de la validación inicial. No se iniciaron contenedores nuevos para este cambio.
