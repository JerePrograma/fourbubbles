# Demo sintética de FourBubbles en Scalaris

Topología nueva y aislada de los entornos operativos: perfil `prod,vps-demo`, PostgreSQL propio, bootstrap de administrador y visor `REPORT_VIEWER`. Nunca habilita `dev`, no importa personas reales, no conecta proveedores ni envía mensajes.

## Arranque en el VPS

Desde un checkout actualizado en `/srv/apps/fourbubbles-demo`:

```sh
cd deployment/vps
sh init-env.sh
docker compose --env-file .env -f compose.yaml config --quiet
docker compose --env-file .env -f compose.yaml build --pull
docker compose --env-file .env -f compose.yaml up -d --wait --wait-timeout 240
python3 seed_demo.py --confirm-synthetic-demo
```

La red externa Docker `proxy` debe existir. Frontend conecta a esa red con alias único `fourbubbles-demo-web` y escucha en **8080**; también se publica exclusivamente en `127.0.0.1:18092`. DB y backend no publican puertos host. La red DB es interna.

`init-env.sh` conserva cualquier `.env` existente y genera secretos solo en la primera instalación, con permisos 0600. Administrador y visor usan contraseñas distintas. El bootstrap únicamente crea cuentas ausentes; no rota ni cambia contraseñas persistidas al reiniciar. La contraseña del administrador permanece privada. Se puede compartir la cuenta de visor para explorar los datos sintéticos; el backend rechaza sus escrituras.

La semilla usa la API existente y las reglas de negocio; crea tres personas ficticias identificadas como demo, tres pedidos, dos ciclos simulados completos y una reserva pendiente. Se puede repetir sin duplicar clientes, pedidos o ciclos. Los contactos `DEMO-*`, emails `example.invalid` y domicilios marcados como ficticios no representan personas reales. Las anotaciones de ciclos declaran explícitamente la simulación.

### Entrada pública de solo consulta

El build de este Compose habilita «Entrar a la demo». Los argumentos `PUBLIC_DEMO_USERNAME` y `PUBLIC_DEMO_PASSWORD` proceden **únicamente** de las variables `APP_DEMO_VIEWER_*` ya existentes; no se rota `.env` ni se cambia su contraseña en DB. Esa credencial del visor pasa a ser deliberadamente pública en el bundle JavaScript. Nunca se pasan las credenciales del administrador al build.

Antes de publicarlo, ejecutar la semilla para comprobar el rol exacto `REPORT_VIEWER` y el rechazo de escrituras. El cliente también rechaza y revoca una sesión inesperada o privilegiada antes de instalarla como sesión de demo. La entrada manual y el cierre de sesión conservan sus flujos originales. Los builds normales carecen del botón; construir el Dockerfile sin argumentos también lo deshabilita. El flag solo acepta `true`/`false`, el usuario público no admite nombres administrativos y la contraseña pública mantiene el mínimo de 24 caracteres.

Después de modificar únicamente el frontend, reconstruir sin imprimir credenciales:

```sh
docker compose --env-file .env -f compose.yaml build frontend
docker compose --env-file .env -f compose.yaml up -d --no-deps --wait frontend
```

## Proxy y rutas públicas

El proxy exterior conserva los prefijos públicos; Nginx resuelve la SPA y reescribe la ruta API internamente:

```caddyfile
redir /fourbubbles /fourbubbles/ 308
handle /fourbubbles/* {
    reverse_proxy fourbubbles-demo-web:8080
}
handle /fourbubbles-api/* {
    reverse_proxy fourbubbles-demo-web:8080
}
```

El frontend se compila con base `/fourbubbles/` y API `/fourbubbles-api/api`. BrowserRouter toma la base compilada y los assets existen físicamente bajo `/fourbubbles/`. La cookie tiene nombre `fourbubbles_demo_refresh`, ruta `/fourbubbles-api/api/auth`, `Secure`, `HttpOnly` y `SameSite=Strict`; no interfiere con otros proyectos. El Nginx interno conserva el protocolo HTTPS reenviado, bloquea Actuator y documentación pública y usa usuario sin privilegios y filesystem de solo lectura. `/health` comprueba readiness del backend y su DB; la raíz `/` no publica la aplicación.

Si Cloudflare Pages sirve `scalaris.com.ar`, sus proxies por rutas deben reenviar estos dos prefijos hacia el origen VPS fijo antes de alcanzar el Caddy anterior. No se modifica DNS con este paquete.

## Recursos

- Backend: 768 MiB, 1 CPU; heap máximo 448 MiB, metaspace 128 MiB y pool DB de ocho conexiones.
- PostgreSQL: 384 MiB, 0,5 CPU; shared buffers 64 MiB y máximo 30 conexiones.
- Frontend: 64 MiB, 0,25 CPU.
- Tope total declarado: 1216 MiB. No equivale a consumo ni capacidad garantizada.
- Rotación de logs: tres archivos de 10 MiB por contenedor.

Se recomienda construir fuera del VPS. Para transportar imágenes, usar las etiquetas generadas desde `APP_VERSION` y `docker save/load`; `up --no-build` reutiliza las imágenes cargadas.

## Respaldo, restauración y rollback

```sh
sh backup.sh
sh restore.sh /var/backups/scalaris/fourbubbles/ARCHIVO.dump --replace-demo-data
```

El respaldo conserva un dump privado PostgreSQL y valida su índice con `pg_restore --list`. Retiene unos 14 días. El administrador debe programar su ejecución diaria e incluir una copia fuera del VPS. Una restauración reemplaza solamente esta DB demo y exige la opción explícita; detiene backend/frontend y luego los reinicia con sus datos restaurados. Usar rutas absolutas para el archivo.

Antes de actualizar, ejecutar `backup.sh`, conservar las etiquetas de imágenes anteriores y anotar SHA y `APP_VERSION`. Si la nueva versión no modifica esquema, restituir la etiqueta previa y recrear backend/frontend. Si cambia el esquema, restaurar el dump con la versión de imágenes compatible; nunca editar migraciones ya publicadas ni eliminar el volumen como mecanismo de actualización.

## Verificación y límites

```sh
docker compose --env-file .env -f compose.yaml ps
docker compose --env-file .env -f compose.yaml exec -T postgres psql -U fourbubbles_demo -d fourbubbles_demo -tAc 'select count(*) from flyway_schema_history where success'
python3 seed_demo.py --confirm-synthetic-demo
```

La instalación exige once migraciones exitosas. La semilla comprueba además datos visibles, cookie segura/aislada, rechazo anónimo y escritura rechazada para el visor. Comprobar navegación, recarga de sesión y rutas estáticas mediante HTTPS después de conectar el proxy público.

El parche de React Router `6.30.1 → 6.30.6` elimina los tres hallazgos altos de `npm audit --omit=dev`. Permanecen dos avisos moderados de React Router: hidratación SSR (no usada en esta SPA) y redirección mediante rutas con barras invertidas. La aplicación usa rutas internas declaradas y UUID, sin aceptar URLs de redirección desde parámetros de consulta; esto limita la exposición, pero no sustituye una migración futura a una versión 7 corregida. No se afirma auditoría sin hallazgos ni producción comercial habilitada.

Esta configuración permite evaluar un entorno demo acotado. Siguen pendientes E2E completo, carga, privacidad para datos reales, object storage y hardening operacional general descrito en `docs/KNOWN_ISSUES.md`.

### Dependencias de backend y avisos con superficie ausente

Antes de evaluar la demo se actualizó Spring Boot a `3.5.16` y se fijaron parches de Tomcat `10.1.61`, Jackson BOM `2.21.8`, PostgreSQL JDBC `42.7.14`, Log4j `2.25.5` y Commons Lang `3.18.0`. Compilación y pruebas unitarias deben conservarse aprobadas al actualizar.

La consulta OSV del 21/10/2026 para **96 dependencias Maven de runtime** conserva dos avisos críticos de `spring-webmvc:6.2.19`: [SSE con FragmentsRendering](https://github.com/advisories/GHSA-j9f9-w8pj-32f8) y [XsltView](https://github.com/advisories/GHSA-pc63-qcmh-9cmg). El código de esta API REST no utiliza `SseEmitter`, `ServerSentEvent`, `FragmentsRendering`, `XsltView`, vistas XSLT ni endpoints `text/event-stream`. Los flujos afectados no están habilitados en la demo; el parche upstream publicado por los avisos pertenece a Spring Framework 7. Esta evaluación de alcance no borra los hallazgos del escáner ni habilita otros usos del framework. Reconsiderarla antes de incorporar vistas, streaming o actualizar Spring.

OSV para coordenadas Maven no escanea el sistema operativo de las imágenes ni demuestra ausencia de vulnerabilidades desconocidas. El estado de la demo y las validaciones concretas se registran en `validation-20261021.md`.
