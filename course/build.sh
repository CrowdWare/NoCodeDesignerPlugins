#!/bin/bash

set -e

PLUGIN_ID="course-plugin"
JAR_NAME="${PLUGIN_ID}-1.0.0.jar"
ZIP_NAME="${PLUGIN_ID}.zip"

echo "▶️  Bauen des Plugins..."
./gradlew clean pluginJar

echo "🧹 Lösche temporäre Dateien in template/..."
rm -rf template/.gradle
rm -rf template/.idea
rm -rf template/.kotlin
rm -rf template/build
rm -rf template/app/build
rm -rf template/app/.cxx
rm -rf template/app/.gradle
rm -rf template/app/.idea

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