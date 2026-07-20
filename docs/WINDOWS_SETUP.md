# Puesta en marcha en Windows con PowerShell

Esta guía levanta el sistema completo desde `main` utilizando Docker Desktop. Es el camino recomendado para desarrollo y evaluación funcional.

## 1. Requisitos

Instalar y verificar:

- Git para Windows.
- Docker Desktop con motor Linux y Docker Compose v2.
- PowerShell 7 recomendado.

Comprobación:

```powershell
git --version
docker --version
docker compose version
```

Docker Desktop debe estar iniciado antes de continuar.

## 2. Descargar el repositorio

```powershell
Set-Location "$HOME\Documents"

if (Test-Path -LiteralPath '.\fourbubbles') {
    throw 'La carpeta fourbubbles ya existe. Renombrala, eliminála o actualizá ese clon.'
}

git clone https://github.com/JerePrograma/fourbubbles.git
Set-Location '.\fourbubbles'
git switch main
git pull --ff-only origin main
```

## 3. Crear configuración local

```powershell
Copy-Item -LiteralPath '.env.example' -Destination '.env' -ErrorAction Stop
```

Generar secretos sin herramientas externas:

```powershell
$JwtBytes = New-Object byte[] 48
[System.Security.Cryptography.RandomNumberGenerator]::Fill($JwtBytes)
$JwtSecret = [Convert]::ToBase64String($JwtBytes)

$AdminBytes = New-Object byte[] 24
[System.Security.Cryptography.RandomNumberGenerator]::Fill($AdminBytes)
$AdminPassword = [Convert]::ToBase64String($AdminBytes).Replace('+','A').Replace('/','B').TrimEnd('=')

$PostgresBytes = New-Object byte[] 24
[System.Security.Cryptography.RandomNumberGenerator]::Fill($PostgresBytes)
$PostgresPassword = [Convert]::ToBase64String($PostgresBytes).Replace('+','C').Replace('/','D').TrimEnd('=')

$EnvPath = Join-Path $PWD '.env'
$EnvContent = Get-Content -LiteralPath $EnvPath -Raw
$EnvContent = $EnvContent.Replace('POSTGRES_PASSWORD=change-me', "POSTGRES_PASSWORD=$PostgresPassword")
$EnvContent = $EnvContent.Replace('DB_PASSWORD=change-me', "DB_PASSWORD=$PostgresPassword")
$EnvContent = $EnvContent.Replace('JWT_SECRET_BASE64=REPLACE_WITH_AT_LEAST_32_RANDOM_BYTES_ENCODED_AS_BASE64', "JWT_SECRET_BASE64=$JwtSecret")
$EnvContent = $EnvContent.Replace('APP_DEV_ADMIN_PASSWORD=change-me-now', "APP_DEV_ADMIN_PASSWORD=$AdminPassword")
Set-Content -LiteralPath $EnvPath -Value $EnvContent -Encoding utf8

Write-Host "Usuario de desarrollo: admin"
Write-Host "Contraseña de desarrollo: $AdminPassword"
Write-Host 'Guardá esta contraseña. No se muestra desde la aplicación.'
```

La contraseña solo se usa para crear el administrador cuando la base está vacía. Modificar `.env` después no cambia automáticamente una cuenta ya creada.

## 4. Validar la configuración

```powershell
docker compose config --quiet
if ($LASTEXITCODE -ne 0) {
    throw 'docker-compose.yml o .env no son válidos.'
}
```

## 5. Construir e iniciar

```powershell
docker compose up --build -d
if ($LASTEXITCODE -ne 0) {
    throw 'Docker Compose no pudo iniciar el sistema.'
}
```

Ver estado:

```powershell
docker compose ps
```

Esperar a que `postgres` y `backend` indiquen `healthy`.

Seguir logs durante el primer arranque:

```powershell
docker compose logs -f backend
```

Salir de los logs con `Ctrl+C`; los contenedores continúan ejecutándose.

## 6. Verificación automática

```powershell
$Health = Invoke-RestMethod -Uri 'http://localhost:8081/api/actuator/health' -Method Get
$Health | ConvertTo-Json -Depth 10

if ($Health.status -ne 'UP') {
    throw 'El backend no está saludable.'
}
```

Abrir:

```powershell
Start-Process 'http://localhost:8080'
Start-Process 'http://localhost:8081/api/swagger-ui.html'
```

Accesos:

- Aplicación web: `http://localhost:8080`
- API directa: `http://localhost:8081/api`
- Swagger: `http://localhost:8081/api/swagger-ui.html`
- Salud: `http://localhost:8081/api/actuator/health`

## 7. Iniciar sesión

Usar:

- usuario: valor de `APP_DEV_ADMIN_USERNAME`, inicialmente `admin`;
- contraseña: valor generado para `APP_DEV_ADMIN_PASSWORD`.

## 8. Detener y volver a iniciar

Detener sin borrar datos:

```powershell
docker compose down
```

Volver a iniciar:

```powershell
docker compose up -d
```

Reconstruir después de actualizar código:

```powershell
git pull --ff-only origin main
docker compose up --build -d
```

## 9. Reinicio destructivo de desarrollo

Esto elimina la base local, usuarios, clientes, pedidos y pagos:

```powershell
docker compose down -v --remove-orphans
docker compose up --build -d
```

No usarlo sobre datos que deban conservarse.

## 10. Diagnóstico

Estado:

```powershell
docker compose ps
```

Logs completos:

```powershell
docker compose logs --tail 300
```

Logs por servicio:

```powershell
docker compose logs --tail 300 postgres
docker compose logs --tail 300 backend
docker compose logs --tail 300 frontend
```

Puertos ocupados:

```powershell
Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue |
    Where-Object LocalPort -In 5432, 8080, 8081 |
    Select-Object LocalAddress, LocalPort, OwningProcess
```

Migraciones aplicadas:

```powershell
docker compose exec postgres psql -U ropalista -d ropalista -c 'select installed_rank, version, description, success from flyway_schema_history order by installed_rank;'
```

## 11. Backup local

```powershell
$BackupDir = Join-Path $PWD 'backups'
New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null
$BackupFile = Join-Path $BackupDir ("ropalista-{0}.dump" -f (Get-Date -Format 'yyyyMMdd-HHmmss'))

docker compose exec -T postgres pg_dump -U ropalista -d ropalista --format=custom | Set-Content -LiteralPath $BackupFile -AsByteStream
Write-Host "Backup creado: $BackupFile"
```

Un archivo no se considera respaldo confiable hasta probar su restauración en otra base.

## 12. Ejecución sin Docker

Solo usar para desarrollo técnico. Requiere Java 21, Maven, Node.js 22 y PostgreSQL 16 instalados localmente.

Backend:

```powershell
Set-Location '.\backend'
$env:SPRING_PROFILES_ACTIVE = 'dev'
$env:DB_HOST = 'localhost'
$env:DB_PORT = '5432'
$env:DB_NAME = 'ropalista'
$env:DB_USER = 'ropalista'
$env:DB_PASSWORD = '<contraseña>'
$env:JWT_SECRET_BASE64 = '<base64-de-al-menos-32-bytes>'
$env:APP_DEV_ADMIN_USERNAME = 'admin'
$env:APP_DEV_ADMIN_PASSWORD = '<contraseña-admin>'
mvn spring-boot:run
```

Frontend, en otra terminal:

```powershell
Set-Location '.\frontend'
npm install
npm run dev
```
