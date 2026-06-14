.PHONY: setup build test install app list-emulators start-emulator run-emulator

ANDROID_HOME := /opt/android-sdk
JAVA_HOME := /usr/lib/jvm/java-21-openjdk-amd64
GRADLE := JAVA_HOME=$(JAVA_HOME) ./gradlew
ADB := $(ANDROID_HOME)/platform-tools/adb

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
	$(ADB) install app/build/outputs/apk/debug/app-debug.apk

list-emulators:
	$(ANDROID_HOME)/emulator/emulator -list-avds

start-emulator:
	$(ANDROID_HOME)/emulator/emulator -avd $$($(ANDROID_HOME)/emulator/emulator -list-avds | head -1) -no-window -no-audio &

run-emulator: build install-apk
	$(ADB) shell am start -n igrek.songbook/.activity.SplashScreenActivity
