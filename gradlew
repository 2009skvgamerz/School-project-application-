#!/usr/bin/env bash

# Self-bootstrapping Gradle Wrapper script

set -e

DIR="$(cd "$(dirname "$0")" && pwd)"

if command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
fi

GRADLE_VERSION="8.10.2"
GRADLE_HOME="$HOME/.gradle/wrapper/dists/gradle-$GRADLE_VERSION-bin"

if [ ! -d "$GRADLE_HOME" ]; then
    echo "Downloading Gradle $GRADLE_VERSION..."
    mkdir -p "$GRADLE_HOME"
    TMP_ZIP="/tmp/gradle-$GRADLE_VERSION-bin.zip"
    if command -v curl >/dev/null 2>&1; then
        curl -sSL -o "$TMP_ZIP" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
    elif command -v wget >/dev/null 2>&1; then
        wget -q -O "$TMP_ZIP" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
    else
        echo "Error: Neither curl nor wget nor gradle is installed." >&2
        exit 1
    fi
    unzip -q -o "$TMP_ZIP" -d "$GRADLE_HOME"
    rm -f "$TMP_ZIP"
fi

GRADLE_BIN=$(find "$GRADLE_HOME" -name "gradle" -type f -perm /111 2>/dev/null | head -n 1)

if [ -z "$GRADLE_BIN" ] || [ ! -x "$GRADLE_BIN" ]; then
    GRADLE_BIN=$(find "$GRADLE_HOME" -name "gradle" -type f | head -n 1)
    chmod +x "$GRADLE_BIN" 2>/dev/null || true
fi

if [ -x "$GRADLE_BIN" ]; then
    exec "$GRADLE_BIN" "$@"
else
    echo "Could not find gradle executable in $GRADLE_HOME" >&2
    exit 1
fi
