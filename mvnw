#!/usr/bin/env sh
set -e

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
MVN_VERSION="3.9.9"
MAVEN_DIR="$BASE_DIR/.mvn/apache-maven-$MVN_VERSION"
MAVEN_TGZ="$BASE_DIR/.mvn/apache-maven-$MVN_VERSION-bin.tar.gz"
MAVEN_URL="https://archive.apache.org/dist/maven/maven-3/$MVN_VERSION/binaries/apache-maven-$MVN_VERSION-bin.tar.gz"

if [ ! -x "$MAVEN_DIR/bin/mvn" ]; then
  echo "Maven $MVN_VERSION not found in .mvn, downloading..."
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$MAVEN_URL" -o "$MAVEN_TGZ"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$MAVEN_URL" -O "$MAVEN_TGZ"
  else
    echo "Need curl or wget to download Maven."
    exit 1
  fi
  tar -xzf "$MAVEN_TGZ" -C "$BASE_DIR/.mvn"
fi

exec "$MAVEN_DIR/bin/mvn" "$@"
