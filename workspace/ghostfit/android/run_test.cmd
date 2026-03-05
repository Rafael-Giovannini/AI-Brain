@echo off
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
set ANDROID_HOME=C:\Users\rafael.giovannini\AppData\Local\Android\Sdk
cd /d %~dp0
call gradlew.bat :app:testDebugUnitTest --tests "app.ghostfit.domain.AdManagerTest" --no-daemon
