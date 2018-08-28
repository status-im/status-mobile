.PHONY: react-native test setup

help: ##@other Show this help
	@perl -e '$(HELP_FUN)' $(MAKEFILE_LIST)

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
	git clean -qdxf -f android/ modules/react-native-status/ node_modules/ target/ desktop/modules/ desktop/node_modules/

setup: ##@prepare Install all the requirements for status-react
	./scripts/setup

prepare: ##@prepare Install dependencies and prepare workspace
	scripts/prepare-for-platform.sh mobile
	npm install

prepare-ios: prepare ##@prepare Install iOS specific dependencies
	mvn -f modules/react-native-status/ios/RCTStatus dependency:unpack
ifeq ($(OS),Darwin)
	cd ios && pod install
endif

prepare-android: prepare ##@prepare Install Android specific dependencies
	cd android && ./gradlew react-native-android:installArchives

#----------------
# Release builds
#----------------
release: release-android release-ios ##@build build release for Android and iOS

release-android: prod-build-android ##@build build release for Android
	react-native run-android --variant=release

release-ios: prod-build-ios ##@build build release for iOS release
	@echo "Build in XCode, see https://wiki.status.im/TBD for instructions"

prod-build:
	lein prod-build

prod-build-android:
	rm ./modules/react-native-status/android/libs/status-im/status-go/local/status-go-local.aar 2> /dev/null || true
	lein prod-build-android

prod-build-ios:
	rm -r ./modules/react-native-status/ios/RCTStatus/Statusgo.framework/ 2> /dev/null || true
	lein prod-build-ios

full-prod-build: ##@build build prod for both Android and iOS
	./scripts/bundle-status-go.sh ios android
	$(MAKE) prod-build
	rm -r ./modules/react-native-status/ios/RCTStatus/Statusgo.framework/ 2> /dev/null || true
	rm ./modules/react-native-status/android/libs/status-im/status-go/local/status-go-local.aar 2> /dev/null

#--------------
# REPL
# -------------

watch-ios-real: ##@watch Start development for iOS real device
	clj -R:dev build.clj watch --platform ios --ios-device real

watch-ios-simulator: ##@watch Start development for iOS simulator
	clj -R:dev build.clj watch --platform ios --ios-device simulator

watch-android-real: ##@watch Start development for Android real device
	clj -R:dev build.clj watch --platform android --android-device real

watch-android-avd: ##@watch Start development for Android AVD
	clj -R:dev build.clj watch --platform android --android-device avd

watch-android-genymotion: ##@watch Start development for Android Genymotion
	clj -R:dev build.clj watch --platform android --android-device genymotion

#--------------
# Run
# -------------
run-android: ##@run Run Android build
	react-native run-android --appIdSuffix debug

SIMULATOR=
run-ios: ##@run Run iOS build
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
	react-native start

geth-connect: ##@other Connect to Geth on the device
	adb forward tcp:8545 tcp:8545
	build/bin/geth attach http://localhost:8545

android-ports: ##@other Add proxies to Android Device/Simulator
	adb -e reverse tcp:8081 tcp:8081
	adb -e reverse tcp:3449 tcp:3449
	adb -e reverse tcp:4567 tcp:4567
	adb -e forward tcp:5561 tcp:5561


startdev-%:
	$(eval SYSTEM := $(word 2, $(subst -, , $@)))
	$(eval DEVICE := $(word 3, $(subst -, , $@)))
	${MAKE} prepare-${SYSTEM}
	${MAKE} watch-$(SYSTEM)-$(DEVICE)
