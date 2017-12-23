.PHONY: react-native test

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

prepare: ##@prepare Install dependencies and prepare workspace
	lein deps
	npm install
	./re-natal deps
	./re-natal use-figwheel
	lein re-frisk use-re-natal
	./re-natal enable-source-maps

prepare-ios: prepare ##@prepare Install iOS specific dependencies
	cd ios && pod install && cd ..

#----------------
# Release builds
#----------------
release: release-android release-ios ##@build build release for Android and iOS

release-android: prod-build ##@build build release for Android
	react-native run-android --variant=release

release-ios: prod-build ##@build build release for iOS release
	@echo "Build in XCode, see https://wiki.status.im/TBD for instructions"

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

dev-ios-real: prod-build ##@dev build for iOS real device
	./re-natal use-ios-device real
	./re-natal use-figwheel

dev-ios-simulator: prod-build ##@dev build for iOS simulator
	./re-natal use-ios-device simulator
	./re-natal use-figwheel

prod-build:
	lein prod-build

#--------------
# REPL
# -------------

repl: ##@repl Start REPL for iOS and Android
	BUILD_IDS="ios,android" lein repl

repl-ios: ##@repl Start REPL for iOS
	BUILD_IDS="ios" lein repl

repl-android: ##@repl Start REPL for Android
	BUILD_IDS="android" lein repl

#--------------
# Run
# -------------
run-android: ##@run Run Android build
	react-native run-android

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

android-ports: ##@other Add reverse proxy to Android Device/Simulator
	adb reverse tcp:8081 tcp:8081
	adb reverse tcp:3449 tcp:3449
	adb reverse tcp:4567 tcp:4567
