#!/usr/bin/env sh
set -eu
cd "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
env_file=${ENV_FILE:-.env}
if [ "$#" -ne 2 ] || [ "$2" != "--replace-demo-data" ]; then
    printf 'Usage: %s backup.dump --replace-demo-data\n' "$0" >&2
    exit 2
fi
test -r "$1"
case "$1" in /*) ;; *) printf 'Use an absolute backup path\n' >&2; exit 2 ;; esac
docker compose --env-file "$env_file" -f compose.yaml exec -T postgres pg_restore --list < "$1" > /dev/null
docker compose --env-file "$env_file" -f compose.yaml stop backend frontend
docker compose --env-file "$env_file" -f compose.yaml exec -T postgres \
    pg_restore -U fourbubbles_demo -d fourbubbles_demo --clean --if-exists --no-owner --exit-on-error < "$1"
docker compose --env-file "$env_file" -f compose.yaml up -d --wait --wait-timeout 240 backend frontend
