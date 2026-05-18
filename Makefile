.PHONY: setup build test install app

JAVA_HOME := /usr/lib/jvm/java-21-openjdk-amd64
GRADLE := JAVA_HOME=$(JAVA_HOME) ./gradlew

setup:
	python3 -m venv venv &&\
	. venv/bin/activate &&\
	pip install --upgrade pip setuptools &&\
	pip install -r requirements-dev.txt

mkdocs-local:
	mkdocs serve

mkdocs-push:
	mkdocs gh-deploy --force --clean --remote-branch gh-pages --remote-name public --no-history --verbose

# Build a debug APK
build:
	$(GRADLE) assembleDebug

# Build a release APK
build-release:
	$(GRADLE) assembleRelease

# Run unit tests
test:
	$(GRADLE) testDebugUnitTest

# Build and Install a debug APK on a connected device
install:
	$(GRADLE) installDebug

install-apk:
	adb install app/build/outputs/apk/debug/app-debug.apk

list-emulators:
	/opt/ext/android-sdk/emulator/emulator  -list-avds

start-emulator:
	/opt/ext/android-sdk/emulator/emulator -avd Pixel3_API_25_N_7.1_no_store

run:
	adb shell am start -n igrek.songbook/.activity.SplashScreenActivity
