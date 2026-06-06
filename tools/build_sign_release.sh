#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

KEY_ROOT="${MG4_KEY_ROOT:-/Users/jan/Projekts/MG4-360-Camera-App/tools}"
KEY_FILE="${KEY_ROOT}/platform.pk8"
CERT_FILE="${KEY_ROOT}/platform.x509.pem"

if [[ ! -f "${KEY_FILE}" ]]; then
  echo "Missing key file: ${KEY_FILE}" >&2
  exit 1
fi

if [[ ! -f "${CERT_FILE}" ]]; then
  echo "Missing cert file: ${CERT_FILE}" >&2
  exit 1
fi

if [[ ! -x ./gradlew ]]; then
  echo "gradlew not found in project root." >&2
  echo "Build the release APK first, or add a Gradle wrapper." >&2
  exit 1
fi

APKSIGNER="$(ls "$HOME"/Library/Android/sdk/build-tools/*/apksigner 2>/dev/null | tail -n 1 || true)"
if [[ -z "${APKSIGNER}" ]]; then
  echo "Could not find apksigner in ~/Library/Android/sdk/build-tools" >&2
  exit 1
fi

UNSIGNED_APK="app/build/outputs/apk/release/app-release-unsigned.apk"
SIGNED_APK="app/build/outputs/apk/release/MG4-Dashcam-Trigger-release.apk"

./gradlew --no-daemon clean assembleRelease

if [[ ! -f "${UNSIGNED_APK}" ]]; then
  echo "Unsigned APK not found: ${UNSIGNED_APK}" >&2
  exit 1
fi

"${APKSIGNER}" sign \
  --key "${KEY_FILE}" \
  --cert "${CERT_FILE}" \
  --out "${SIGNED_APK}" \
  "${UNSIGNED_APK}"

echo "Signed APK created:"
echo "  ${SIGNED_APK}"
