#!/usr/bin/env bash
set -e

echo "=== Building Debug APK ==="

if [ ! -f "debug.keystore" ] && [ -f "debug.keystore.base64" ]; then
    echo "Restoring debug.keystore from debug.keystore.base64..."
    base64 -d debug.keystore.base64 > debug.keystore 2>/dev/null || base64 --decode debug.keystore.base64 > debug.keystore
fi

if [ -f "./gradlew" ]; then
    chmod +x ./gradlew
    ./gradlew assembleDebug
else
    gradle assembleDebug
fi

echo ""
echo "=== Debug APK successfully created ==="
echo "Location: app/build/outputs/apk/debug/app-debug.apk"

