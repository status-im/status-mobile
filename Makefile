.PHONY: react-native test setup

help: ##@other Show this help
	@perl -e '$(HELP_FUN)' $(MAKEFILE_LIST)

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

setup: ##@prepare Install all the requirements for status-react
	./scripts/setup

prepare: ##@prepare Install dependencies and prepare workspace
	lein deps
	npm install
	./re-natal deps
	./re-natal use-figwheel
	./re-natal enable-source-maps

prepare-ios: prepare ##@prepare Install iOS specific dependencies
	mvn -f modules/react-native-status/ios/RCTStatus dependency:unpack
	cd ios && pod install && cd ..

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
	./scripts/bundle-status-go.sh ios
	./scripts/bundle-status-go.sh android
	$(MAKE) prod-build
	rm -r ./modules/react-native-status/ios/RCTStatus/Statusgo.framework/ 2> /dev/null || true
	rm ./modules/react-native-status/android/libs/status-im/status-go/local/status-go-local.aar 2> /dev/null

#----------------
# Dev builds
#----------------
dev-android-real: ##@dev build for Android real device
	./re-natal use-android-device real
	./re-natal use-figwheel

dev-android-avd: ##@dev build for Android AVD simulator
	./re-natal use-android-device avd
	./re-natal use-figwheel

dev-android-genymotion: ##@dev build for Android Genymotion simulator
	./re-natal use-android-device genymotion
	./re-natal use-figwheel

dev-ios-real: ##@dev build for iOS real device
	./re-natal use-ios-device real
	./re-natal use-figwheel

dev-ios-simulator: ##@dev build for iOS simulator
	./re-natal use-ios-device simulator
	./re-natal use-figwheel

#--------------
# REPL
# -------------

repl: ##@repl Start REPL for iOS and Android
	lein figwheel-repl ios android

repl-ios: ##@repl Start REPL for iOS
	lein figwheel-repl ios

repl-android: ##@repl Start REPL for Android
	lein figwheel-repl android

#--------------
# Run
# -------------
run-android: ##@run Run Android build
	react-native run-android --appIdSuffix debug

run-ios: ##@run Run iOS build
	react-native run-ios

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

android-ports-avd: ##@other Add reverse proxy to Android Device/Simulator
	adb -e reverse tcp:8081 tcp:8081
	adb -e reverse tcp:3449 tcp:3449
	adb -e reverse tcp:4567 tcp:4567

android-ports-real: ##@other Add reverse proxy to Android Device/Simulator
	adb -d reverse tcp:8081 tcp:8081
	adb -d reverse tcp:3449 tcp:3449
	adb -d reverse tcp:4567 tcp:4567


startdev-%:
	$(eval SYSTEM := $(word 2, $(subst -, , $@)))
	$(eval DEVICE := $(word 3, $(subst -, , $@)))
	case "$(SYSTEM)" in \
	  "android") ${MAKE} prepare && ${MAKE} android-ports-$(DEVICE);; \
	  "ios")      ${MAKE} prepare-ios;; \
	esac
	${MAKE} dev-$(SYSTEM)-$(DEVICE)
	${MAKE} -j2 react-native repl-$(SYSTEM)
