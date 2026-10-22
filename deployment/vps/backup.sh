#!/usr/bin/env sh
set -eu
umask 077
cd "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
env_file=${ENV_FILE:-.env}
backup_directory=${BACKUP_DIRECTORY:-/var/backups/scalaris/fourbubbles}
mkdir -p "$backup_directory"
output="$backup_directory/fourbubbles-$(date -u +%Y%m%dT%H%M%SZ).dump"
docker compose --env-file "$env_file" -f compose.yaml exec -T postgres \
    pg_dump -U fourbubbles_demo -d fourbubbles_demo -Fc > "$output"
test -s "$output"
docker compose --env-file "$env_file" -f compose.yaml exec -T postgres \
    pg_restore --list < "$output" > /dev/null
find "$backup_directory" -maxdepth 1 -type f -name 'fourbubbles-*.dump' -mtime +14 -delete
printf 'Backup verified: %s\n' "$output"
