#!/bin/sh
set -e

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_DIR="$BASE_DIR/.mvn/wrapper"
WRAPPER_PROPERTIES="$WRAPPER_DIR/maven-wrapper.properties"
WRAPPER_JAR="$WRAPPER_DIR/maven-wrapper.jar"

if [ -z "$JAVA_HOME" ]; then
  JAVA_CMD="java"
else
  JAVA_CMD="$JAVA_HOME/bin/java"
fi

if ! command -v "$JAVA_CMD" >/dev/null 2>&1; then
  echo "Error: JAVA_HOME is not set and no 'java' command could be found on the PATH." >&2
  echo "Install Java 17+ and make sure it is on your PATH, then try again." >&2
  exit 1
fi

WRAPPER_URL=$(grep '^wrapperUrl=' "$WRAPPER_PROPERTIES" | cut -d'=' -f2-)

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Downloading Maven Wrapper..."
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL -o "$WRAPPER_JAR" "$WRAPPER_URL"
  elif command -v wget >/dev/null 2>&1; then
    wget -q -O "$WRAPPER_JAR" "$WRAPPER_URL"
  else
    echo "Error: need either curl or wget installed to download the Maven Wrapper jar." >&2
    exit 1
  fi
fi

exec "$JAVA_CMD" \
  -Dmaven.multiModuleProjectDirectory="$BASE_DIR" \
  -cp "$WRAPPER_JAR" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
