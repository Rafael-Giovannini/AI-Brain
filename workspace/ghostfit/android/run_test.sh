#!/usr/bin/env bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
export ANDROID_HOME="/c/Users/rafael.giovannini/AppData/Local/Android/Sdk"
cd "$(dirname "$0")" || exit 1
exec ./gradlew :app:testDebugUnitTest --tests "app.ghostfit.domain.AdManagerTest" --no-daemon
