#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
GRADLE_VERSION="8.8"
GRADLE_ZIP="/tmp/gradle-${GRADLE_VERSION}-bin.zip"
GRADLE_DIR="/tmp/gradle-${GRADLE_VERSION}"
JAVA17_HOME="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"

if [ ! -x "${JAVA17_HOME}/bin/java" ]; then
  echo "Java 17 not found at ${JAVA17_HOME}" >&2
  exit 1
fi

if [ ! -x "${GRADLE_DIR}/bin/gradle" ]; then
  curl -L --silent --show-error "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o "${GRADLE_ZIP}"
  rm -rf "${GRADLE_DIR}"
  unzip -q "${GRADLE_ZIP}" -d /tmp
fi

cd "${ROOT_DIR}"
JAVA_HOME="${JAVA17_HOME}" "${GRADLE_DIR}/bin/gradle" build --no-daemon

