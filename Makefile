.PHONY: setup build install app

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
	./gradlew assembleDebug

# Build and Install a debug APK on a connected device
install:
	./gradlew installDebug

install-apk:
	adb install app/build/outputs/apk/debug/app-debug.apk

list-emulators:
	/opt/ext/android-sdk/emulator/emulator  -list-avds

start-emulator:
	/opt/ext/android-sdk/emulator/emulator -avd Pixel3_API_25_N_7.1_no_store

run:
	adb shell am start -n igrek.songbook/.activity.SplashScreenActivity
