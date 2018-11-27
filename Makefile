.PHONY: react-native test setup

help: ##@other Show this help
	@perl -e '$(HELP_FUN)' $(MAKEFILE_LIST)

DO_SPACE_URL = https://status-go.ams3.digitaloceanspaces.com
GITHUB_URL = https://github.com/status-im/status-go/releases/download
RCTSTATUS_DIR = modules/react-native-status/ios/RCTStatus
ANDROID_LIBS_DIR = android/app/libs
STATUS_GO_VER = $(shell cat STATUS_GO_VERSION)

STATUS_GO_IOS_ARCH = $(RCTSTATUS_DIR)/status-go-ios-$(STATUS_GO_VER).zip
STATUS_GO_DRO_ARCH = $(ANDROID_LIBS_DIR)/status-go-$(STATUS_GO_VER).aar

OS := $(shell uname)

# This is a code for automatic help generator.
# It supports ANSI colors and categories.
# To add new item into help output, simply add comments
# starting with '##'. To add category, use @category.
GREEN  := $(shell tput -Txterm setaf 2)
WHITE  := $(shell tput -Txterm setaf 7)
YELLOW := $(shell tput -Txterm setaf 3)
RESET  := $(shell tput -Txterm sgr0)
HELP_FUN = \
		   %help; \
		   while(<>) { push @{$$help{$$2 // 'options'}}, [$$1, $$3] if /^([a-zA-Z\-]+)\s*:.*\#\#(?:@([a-zA-Z\-]+))?\s(.*)$$/ }; \
		   print "Usage: make [target]\n\n"; \
		   for (sort keys %help) { \
			   print "${WHITE}$$_:${RESET}\n"; \
			   for (@{$$help{$$_}}) { \
				   $$sep = " " x (32 - length $$_->[0]); \
				   print "  ${YELLOW}$$_->[0]${RESET}$$sep${GREEN}$$_->[1]${RESET}\n"; \
			   }; \
			   print "\n"; \
		   }

# Main targets

clean: ##@prepare Remove all output folders
	git clean -dxf -f -e android/local.properties

setup: ##@prepare Install all the requirements for status-react
	./scripts/setup

prepare-desktop: ##@prepare Install desktop platform dependencies and prepare workspace
	scripts/prepare-for-platform.sh desktop
	npm install

$(STATUS_GO_IOS_ARCH):
	curl --fail --silent --location \
		"${DO_SPACE_URL}/status-go-ios-$(STATUS_GO_VER).zip" \
		--output "$(STATUS_GO_IOS_ARCH)"; \
	if [ $$? -ne 0 ]; then \
		echo "Failed to download from DigitalOcean Bucket, checking GitHub..."; \
		curl --fail --silent --location \
			"$(GITHUB_URL)/$(STATUS_GO_VER)/status-go-ios.zip" \
			--output "$(STATUS_GO_IOS_ARCH)"; \
		if [ $$? -ne 0 ]; then \
			echo "Failed to download from GitHub!"; \
		fi \
	fi

$(STATUS_GO_DRO_ARCH):
	mkdir -p $(ANDROID_LIBS_DIR)
	curl --fail --silent --location \
		"${DO_SPACE_URL}/status-go-android-$(STATUS_GO_VER).aar" \
		--output "$(STATUS_GO_DRO_ARCH)"; \
	if [ $$? -ne 0 ]; then \
		echo "Failed to download from DigitalOcean Bucket, checking GitHub..."; \
		curl --fail --silent --location \
			"$(GITHUB_URL)/$(STATUS_GO_VER)/status-go-android.aar" \
			--output "$(STATUS_GO_DRO_ARCH)"; \
		if [ $$? -ne 0 ]; then \
			echo "Failed to download from GitHub!"; \
		fi \
	fi

prepare-ios: $(STATUS_GO_IOS_ARCH) ##@prepare Install and prepare iOS-specific dependencies
	scripts/prepare-for-platform.sh ios
	npm install
	unzip -q -o "$(STATUS_GO_IOS_ARCH)" -d "$(RCTSTATUS_DIR)"
ifeq ($(OS),Darwin)
	cd ios && pod install
endif

prepare-android: $(STATUS_GO_DRO_ARCH) ##@prepare Install and prepare Android-specific dependencies
	scripts/prepare-for-platform.sh android
	npm install
	cd android && ./gradlew react-native-android:installArchives

prepare-mobile: prepare-android prepare-ios ##@prepare Install and prepare mobile platform specific dependencies

#----------------
# Release builds
#----------------
release: release-android release-ios ##@build build release for Android and iOS

release-android: prod-build-android ##@build build release for Android
	react-native run-android --variant=release

release-ios: prod-build-ios ##@build build release for iOS release
	@echo "Build in XCode, see https://status.im/build_status/ for instructions"

release-desktop: prod-build-desktop ##@build build release for desktop release
	scripts/build-desktop.sh

release-windows-desktop: prod-build-desktop ##@build build release for desktop release
	TARGET_SYSTEM_NAME=Windows scripts/build-desktop.sh

prod-build:
	scripts/run-pre-build-check.sh android
	scripts/run-pre-build-check.sh ios
	lein prod-build

prod-build-android:
	rm ./modules/react-native-status/android/libs/status-im/status-go/local/status-go-local.aar 2> /dev/null || true
	scripts/run-pre-build-check.sh android
	lein prod-build-android

prod-build-ios:
	rm -r ./modules/react-native-status/ios/RCTStatus/Statusgo.framework/ 2> /dev/null || true
	scripts/run-pre-build-check.sh ios
	lein prod-build-ios

full-prod-build: ##@build build prod for both Android and iOS
	./scripts/bundle-status-go.sh ios android
	$(MAKE) prod-build
	rm -r ./modules/react-native-status/ios/RCTStatus/Statusgo.framework/ 2> /dev/null || true
	rm ./modules/react-native-status/android/libs/status-im/status-go/local/status-go-local.aar 2> /dev/null

prod-build-desktop:
	git clean -qdxf -f ./index.desktop.js desktop/
	scripts/run-pre-build-check.sh desktop
	lein prod-build-desktop

#--------------
# REPL
# -------------

watch-ios-real: ##@watch Start development for iOS real device
	scripts/run-pre-build-check.sh ios
	clj -R:dev build.clj watch --platform ios --ios-device real

watch-ios-simulator: ##@watch Start development for iOS simulator
	scripts/run-pre-build-check.sh ios
	clj -R:dev build.clj watch --platform ios --ios-device simulator

watch-android-real: ##@watch Start development for Android real device
	scripts/run-pre-build-check.sh android
	clj -R:dev build.clj watch --platform android --android-device real

watch-android-avd: ##@watch Start development for Android AVD
	scripts/run-pre-build-check.sh android
	clj -R:dev build.clj watch --platform android --android-device avd

watch-android-genymotion: ##@watch Start development for Android Genymotion
	scripts/run-pre-build-check.sh android
	clj -R:dev build.clj watch --platform android --android-device genymotion

watch-desktop: ##@watch Start development for Desktop
	scripts/run-pre-build-check.sh desktop
	clj -R:dev build.clj watch --platform desktop

#--------------
# Run
# -------------
run-android: ##@run Run Android build
	scripts/run-pre-build-check.sh android
	react-native run-android --appIdSuffix debug

run-desktop: ##@run Run Desktop build
	scripts/run-pre-build-check.sh desktop
	react-native run-desktop

SIMULATOR=
run-ios: ##@run Run iOS build
	scripts/run-pre-build-check.sh ios
ifneq ("$(SIMULATOR)", "")
	react-native run-ios --simulator="$(SIMULATOR)"
else
	react-native run-ios
endif

#--------------
# Tests
#--------------

test: ##@test Run tests once in NodeJS
	lein with-profile test doo node test once

test-auto: ##@test Run tests in interactive (auto) mode in NodeJS
	lein with-profile test doo node test

#--------------
# Other
#--------------
react-native: ##@other Start react native packager
	@scripts/start-react-native.sh

geth-connect: ##@other Connect to Geth on the device
	adb forward tcp:8545 tcp:8545
	build/bin/geth attach http://localhost:8545

android-ports: ##@other Add proxies to Android Device/Simulator
	adb reverse tcp:8081 tcp:8081
	adb reverse tcp:3449 tcp:3449
	adb reverse tcp:4567 tcp:4567
	adb forward tcp:5561 tcp:5561


startdev-%:
	$(eval SYSTEM := $(word 2, $(subst -, , $@)))
	$(eval DEVICE := $(word 3, $(subst -, , $@)))
	${MAKE} prepare-${SYSTEM}
	${MAKE} watch-$(SYSTEM)-$(DEVICE)
