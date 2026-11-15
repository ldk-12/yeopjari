#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${ANDROID_HOME:?Set ANDROID_HOME}"
: "${JAVA_HOME:?Set JAVA_HOME}"
: "${ANDROID_CAR_JAR:?Set ANDROID_CAR_JAR to platform android.car API stub}"
: "${PLEOS_DEPS_DIR:?Set PLEOS_DEPS_DIR to approved dependency JAR directory}"
export PLEOS_DEPS_DIR
python3 scripts/check-dependencies.py
B="$ANDROID_HOME/build-tools/35.0.0"
A="$ANDROID_HOME/platforms/android-35/android.jar"
mkdir -p build/classes build/dex
"$JAVA_HOME/bin/javac" -proc:none -source 8 -target 8 -encoding UTF-8 -cp "$A:$ANDROID_CAR_JAR:$PLEOS_DEPS_DIR/*" -d build/classes android/src/main/java/app/pleos/coachboundary/MainActivity.java
"$B/d8" --lib "$A" --lib "$ANDROID_CAR_JAR" --min-api 30 --output build/dex build/classes/app/pleos/coachboundary/*.class "$PLEOS_DEPS_DIR"/*.jar > build/d8.log 2>&1
"$B/aapt2" link -o build/base.apk --manifest android/src/main/AndroidManifest.xml -I "$A" -A android/src/main/assets
python3 scripts/package-apk.py
"$B/zipalign" -f 4 build/unaligned.apk build/coach-boundary-unsigned.apk
"$B/zipalign" -c 4 build/coach-boundary-unsigned.apk
printf 'UNSIGNED APK built. Not installable until authorized release signing.\n'
