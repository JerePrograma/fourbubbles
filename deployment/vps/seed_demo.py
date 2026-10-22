#!/usr/bin/env python3
"""Seed and verify only the isolated synthetic FourBubbles demo through its API."""
import argparse
import json
import pathlib
import re
import sys
import urllib.error
import urllib.parse
import urllib.request


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--env-file", default=str(pathlib.Path(__file__).with_name(".env")))
    parser.add_argument("--api-base", default="http://127.0.0.1:18092/fourbubbles-api/api")
    parser.add_argument("--confirm-synthetic-demo", action="store_true", required=True)
    args = parser.parse_args()
    parsed = urllib.parse.urlsplit(args.api_base)
    if parsed.scheme != "http" or parsed.hostname not in ("127.0.0.1", "localhost"):
        raise SystemExit("Use this script locally against the isolated demo loopback API")
    origin = urllib.parse.urlunsplit((parsed.scheme, parsed.netloc, "", "", ""))

    def web_get(path, expected=200):
        try:
            response = urllib.request.urlopen(origin + path, timeout=20)
        except urllib.error.HTTPError as error:
            if error.code == expected:
                return b"", ""
            raise RuntimeError(f"GET {path} returned HTTP {error.code}") from None
        if response.status != expected:
            raise RuntimeError(f"Unexpected status for GET {path}")
        return response.read(), response.headers.get("Content-Type", "")

    health, _ = web_get("/health")
    if json.loads(health).get("status") != "UP":
        raise RuntimeError("Frontend/backend/database readiness is not UP")
    html, content_type = web_get("/fourbubbles/")
    if "text/html" not in content_type or b'<div id="root"></div>' not in html:
        raise RuntimeError("The prefixed SPA did not load")
    assets = re.findall(r'(?:src|href)="(/fourbubbles/assets/[^\"]+)"', html.decode())
    if not assets:
        raise RuntimeError("The prefixed SPA did not expose real asset URLs")
    for asset in assets:
        body, asset_type = web_get(asset)
        if not body or "text/html" in asset_type:
            raise RuntimeError("A prefixed asset returned HTML or an empty response")
    deep_html, _ = web_get("/fourbubbles/production/metrics")
    if b'<div id="root"></div>' not in deep_html:
        raise RuntimeError("SPA deep-link fallback failed")
    web_get("/fourbubbles/assets/not-present.js", expected=404)
    web_get("/fourbubbles-api/api/actuator/health", expected=404)
    web_get("/", expected=404)
    env = {}
    for line in pathlib.Path(args.env_file).read_text(encoding="utf-8-sig").splitlines():
        if line.strip() and not line.lstrip().startswith("#") and "=" in line:
            key, value = line.split("=", 1)
            env[key.strip()] = value.strip()

    auth_cookies = []

    def call(path, method="GET", data=None, token=None, key=None, expected=200, cookie=None):
        headers = {"Content-Type": "application/json", "Origin": "https://scalaris.com.ar"}
        if token:
            headers["Authorization"] = "Bearer " + token
        if key:
            headers["Idempotency-Key"] = key
        if cookie:
            headers["Cookie"] = cookie
        request = urllib.request.Request(args.api_base.rstrip("/") + path,
                                         data=json.dumps(data).encode() if data is not None else None,
                                         headers=headers, method=method)
        try:
            response = urllib.request.urlopen(request, timeout=30)
        except urllib.error.HTTPError as error:
            if error.code == expected:
                return None
            # Response bodies can contain operational data. Keep logs redacted.
            raise RuntimeError(f"{method} {path.split('?')[0]} returned HTTP {error.code}") from None
        if response.status != expected:
            raise RuntimeError(f"Unexpected HTTP status for {method} {path.split('?')[0]}")
        if path in ("/auth/login", "/auth/refresh"):
            response_cookie = response.headers.get("Set-Cookie", "")
            for part in ("fourbubbles_demo_refresh=", "HttpOnly", "Secure", "SameSite=Strict",
                         "Path=/fourbubbles-api/api/auth"):
                if part not in response_cookie:
                    raise RuntimeError("The demo authentication cookie is not isolated and secure")
            auth_cookies.append(response_cookie.split(";", 1)[0])
        envelope = json.load(response)
        if not envelope.get("success"):
            raise RuntimeError("The API did not confirm success")
        return envelope.get("data")

    admin = call("/auth/login", "POST", {
        "username": env["APP_DEMO_ADMIN_USERNAME"], "password": env["APP_DEMO_ADMIN_PASSWORD"]})
    token = admin["accessToken"]
    clients = []
    orders = []
    for index, label in enumerate(("Uno", "Dos", "Tres"), 1):
        surname = "Demo Scalaris " + label
        existing = call("/clients?lastName=" + urllib.parse.quote(surname), token=token)["content"]
        client = next((item for item in existing if item["lastName"] == surname), None)
        if client is None:
            client = call("/clients", "POST", {
                "firstName": "Cliente ficticio", "lastName": surname,
                "phone": f"DEMO-{index:04d}", "whatsapp": f"DEMO-{index:04d}",
                "email": f"cliente{index}@example.invalid", "acquisitionSource": "DEMO_SINTETICA",
                "notes": "Persona, contacto y domicilio ficticios. Sin operaciones reales.",
                "addresses": [{"zoneCode": "MARCOS_PAZ", "street": "Calle Ejemplo (ficticia)",
                               "number": str(index), "locality": "Marcos Paz", "primaryAddress": True}]}, token)
        clients.append(client)
        marker = f"SCALARIS-DEMO-{index:03d}"
        candidates = call("/orders?clientId=" + client["id"], token=token)["content"]
        order = None
        for item in candidates:
            detail = call("/orders/" + item["id"], token=token)
            if (detail.get("notes") or "").startswith(marker):
                order = detail
                break
        if order is None:
            order = call("/orders", "POST", {
                "clientId": client["id"], "addressId": client["addresses"][0]["id"],
                "serviceCode": "ROPA_LISTA_12", "declaredWeightGrams": 2500,
                "exclusiveCycle": False, "notes": marker + " - recorrido ficticio de demostración",
                "items": [{"equivalenceCode": "TSHIRT", "physicalPieces": 2}]}, token)
        if order["status"] == "QUOTED":
            order = call("/orders/" + order["id"] + "/confirm-price", "POST", token=token)
        if order["status"] == "WAITING_CONFIRMATION":
            order = call("/orders/" + order["id"] + "/status", "PATCH", {"newStatus": "RESERVED"}, token)
        if index < 3:
            for before, after in (("RESERVED", "PICKUP_SCHEDULED"), ("PICKUP_SCHEDULED", "PICKED_UP")):
                if order["status"] == before:
                    order = call("/orders/" + order["id"] + "/status", "PATCH", {"newStatus": after}, token)
            if order["status"] == "PICKED_UP":
                call("/orders/" + order["id"] + "/reception", "POST", {
                    "actualWeightGrams": 2500,
                    "items": [{"equivalenceCode": "TSHIRT", "actualPhysicalPieces": 2,
                               "damageDetected": False, "stainDetected": False}]}, token, marker + "-reception")
                order = call("/orders/" + order["id"], token=token)
            if order["status"] == "CLASSIFIED":
                call("/orders/" + order["id"] + "/compatibility-profile", "PUT", {
                    "colorGroup": "LIGHT", "materialGroup": "COTTON", "maxTemperatureC": 40,
                    "dryerAllowed": True, "fragrancePolicy": "STANDARD", "softenerAllowed": True,
                    "hypoallergenic": False, "babyClothes": False, "petContact": False,
                    "heavySoil": False, "exclusiveCycle": False}, token)
        orders.append(order)

    order_ids = [order["id"] for order in orders[:2]]
    if all(order["status"] == "CLASSIFIED" for order in orders[:2]):
        call("/compatibility/evaluate", "POST", {"orderAId": order_ids[0], "orderBId": order_ids[1]}, token)
    for stage, machine_id, program_id in (
            ("wash", "94000000-0000-0000-0000-000000000001", "95000000-0000-0000-0000-000000000002"),
            ("dry", "94000000-0000-0000-0000-000000000002", "95000000-0000-0000-0000-000000000005")):
        cycle = call("/production/cycles", "POST", {
            "machineId": machine_id, "programId": program_id, "orderIds": order_ids,
            "notes": "DEMO SINTÉTICA SCALARIS: ciclo simulado, sin operación física real"},
            token, "scalaris-demo-v1-" + stage)
        if cycle["status"] == "PLANNED":
            cycle = call("/production/cycles/" + cycle["id"] + "/start", "POST",
                         {"observation": "Inicio simulado de demostración"}, token)
        if cycle["status"] == "RUNNING":
            call("/production/cycles/" + cycle["id"] + "/complete", "POST",
                 {"actualWeightGrams": 5000, "observation": "Fin simulado de demostración"}, token)
    for order_id in order_ids:
        if call("/orders/" + order_id, token=token)["status"] == "QUALITY_CONTROL":
            call("/production/orders/" + order_id + "/quality-control", "PATCH",
                 {"decision": "PASS", "observation": "Control ficticio para demostración"}, token)

    call("/clients", expected=401)
    viewer = call("/auth/login", "POST", {
        "username": env["APP_DEMO_VIEWER_USERNAME"], "password": env["APP_DEMO_VIEWER_PASSWORD"]})
    if viewer["roles"] != ["REPORT_VIEWER"]:
        raise RuntimeError("Demo viewer does not have exactly the read-only report role")
    viewer_token = viewer["accessToken"]
    original_cookie = auth_cookies[-1]
    refreshed = call("/auth/refresh", "POST", cookie=original_cookie)
    rotated_cookie = auth_cookies[-1]
    if rotated_cookie == original_cookie or not refreshed.get("accessToken"):
        raise RuntimeError("Refresh did not rotate the session")
    call("/auth/refresh", "POST", expected=401, cookie=original_cookie)
    call("/clients", "POST", {
        "firstName": "Demo", "lastName": "Write must be denied", "phone": "DEMO-DENIED",
        "whatsapp": "DEMO-DENIED", "addresses": [{"zoneCode": "MARCOS_PAZ", "street": "Ficticia",
        "number": "0", "locality": "Marcos Paz", "primaryAddress": True}]}, viewer_token, expected=403)
    metrics = call("/production/metrics", token=viewer_token)
    visible_orders = call("/orders", token=viewer_token)
    if metrics["completedCycles"] < 2 or visible_orders["totalElements"] < 3:
        raise RuntimeError("Demo data verification failed")
    call("/auth/logout", "POST", cookie=rotated_cookie)
    call("/auth/refresh", "POST", expected=401, cookie=rotated_cookie)
    print(json.dumps({"synthetic_clients": len(clients), "visible_orders": visible_orders["totalElements"],
                      "completed_cycles": metrics["completedCycles"], "anonymous": "rejected",
                      "viewer_write": "rejected", "cookie": "isolated_secure_httponly",
                      "prefixed_assets": len(assets), "spa_deep_link": "passed", "readiness": "UP",
                      "refresh_rotation_and_logout": "passed"}))


if __name__ == "__main__":
    try:
        main()
    except (RuntimeError, KeyError, urllib.error.URLError) as error:
        print("Synthetic demo seed failed: " + str(error), file=sys.stderr)
        raise SystemExit(1)
