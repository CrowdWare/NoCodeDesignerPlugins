#!/bin/bash

set -e

PLUGIN_ID="bootstrap5"
JAR_NAME="bootstrap5-plugin-1.0.0.jar"
ZIP_NAME="${PLUGIN_ID}-plugin.zip"

echo "▶️  Bauen des Plugins..."
./gradlew clean pluginJar

echo "📦 Erstelle ${ZIP_NAME}..."

rm -f $ZIP_NAME
zip -r $ZIP_NAME \
  plugin.json \
  icon.svg \
  templates \
  build/libs/$JAR_NAME

echo "✅ Plugin-Paket erstellt: $ZIP_NAME"