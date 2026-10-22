#!/usr/bin/env sh
set -eu
umask 077
cd "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
if [ -e .env ]; then
    printf '.env already exists; credentials preserved\n'
    exit 0
fi
version=$(git -C ../.. rev-parse --short=12 HEAD)
APP_DEMO_VERSION="$version" python3 - <<'PY'
import base64
import os
import pathlib
import secrets

values = {
    "APP_VERSION": "vps-" + os.environ["APP_DEMO_VERSION"],
    "FRONTEND_HOST_PORT": "18092",
    "POSTGRES_PASSWORD": secrets.token_hex(32),
    "JWT_SECRET_BASE64": base64.b64encode(secrets.token_bytes(48)).decode(),
    "APP_DEMO_ADMIN_USERNAME": "demo-admin",
    "APP_DEMO_ADMIN_PASSWORD": secrets.token_hex(32),
    "APP_DEMO_VIEWER_USERNAME": "demo",
    "APP_DEMO_VIEWER_PASSWORD": secrets.token_hex(24),
}
with pathlib.Path(".env").open("x", encoding="utf-8") as target:
    target.write("".join(f"{key}={value}\n" for key, value in values.items()))
os.chmod(".env", 0o600)
print("Created private .env with separate administrator/viewer credentials")
PY
