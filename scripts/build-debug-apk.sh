#!/usr/bin/env bash
set -e

echo "=== Building Debug APK ==="

if [ -f "./gradlew" ]; then
    chmod +x ./gradlew
    ./gradlew assembleDebug
else
    gradle assembleDebug
fi

echo ""
echo "=== Debug APK successfully created ==="
echo "Location: app/build/outputs/apk/debug/app-debug.apk"
