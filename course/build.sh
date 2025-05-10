#!/bin/bash

set -e

PLUGIN_ID="course-plugin"
JAR_NAME="${PLUGIN_ID}-1.0.0.jar"
ZIP_NAME="${PLUGIN_ID}.zip"

echo "▶️  Bauen des Plugins..."
./gradlew clean pluginJar

echo "📦 Erstelle ${ZIP_NAME}..."

rm -f $ZIP_NAME
zip -r $ZIP_NAME \
  plugin.json \
  icon.svg \
  sml \
  template \
  build/libs/$JAR_NAME

echo "✅ Plugin-Paket erstellt: $ZIP_NAME"

echo "▶️  Copy ${ZIP_NAME} to Plugins dir..."
cp -f "${ZIP_NAME}" "/Users/art/Library/Application Support/NoCodeDesigner/plugins"