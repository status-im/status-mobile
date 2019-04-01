.PHONY: add-gcroots clean clean-nix react-native-android react-native-ios react-native-desktop test release _list

help: ##@other Show this help
	@perl -e '$(HELP_FUN)' $(MAKEFILE_LIST)

# This is a code for automatic help generator.
# It supports ANSI colors and categories.
# To add new item into help output, simply add comments
# starting with '##'. To add category, use @category.
GREEN  := $(shell tput -Txterm setaf 2)
RED    := $(shell tput -Txterm setaf 1)
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
HOST_OS := $(shell uname | tr '[:upper:]' '[:lower:]')

export NIX_CONF_DIR = $(PWD)/nix

export REACT_SERVER_PORT ?= 5001 # any value different from default 5000 will work; this has to be specified for both the Node.JS server process and the Qt process

# WARNING: This has to be located right before the targets
ifdef IN_NIX_SHELL
SHELL := env bash
else
SHELL := ./nix/shell.sh
endif

# Main targets

clean: SHELL := /bin/sh
clean: ##@prepare Remove all output folders
	git clean -dxf -f

clean-nix: SHELL := /bin/sh
clean-nix: ##@prepare Remove complete nix setup
	sudo rm -rf /nix ~/.nix-profile ~/.nix-defexpr ~/.nix-channels ~/.cache/nix ~/.status .nix-gcroots

add-gcroots: ##@prepare Add Nix GC roots to avoid status-react expressions being garbage collected
	scripts/add-gcroots.sh

shell: ##@prepare Enter into a pre-configured shell
ifndef IN_NIX_SHELL
	@ENTER_NIX_SHELL
else
	@echo "Nix shell is already active"
endif

#----------------
# Release builds
#----------------
release: release-android release-ios ##@build build release for Android and iOS

release-android: export TARGET_OS ?= android
release-android: ##@build build release for Android
	@$(MAKE) prod-build-android && \
	react-native run-android --variant=release

release-ios: export TARGET_OS ?= ios
release-ios: ##@build build release for iOS release
	# Open XCode inside the Nix context
	@$(MAKE) prod-build-ios && \
	echo "Build in XCode, see https://status.im/build_status/ for instructions" && \
	open ios/StatusIm.xcworkspace

release-desktop: export TARGET_OS ?= $(HOST_OS)
release-desktop: ##@build build release for desktop release
	@$(MAKE) prod-build-desktop && \
	scripts/build-desktop.sh

release-windows-desktop: export TARGET_OS ?= windows
release-windows-desktop: ##@build build release for desktop release
	@$(MAKE) prod-build-desktop && \
	scripts/build-desktop.sh

prod-build: export TARGET_OS ?= all
prod-build:
	scripts/prepare-for-platform.sh android && \
	scripts/prepare-for-platform.sh ios && \
	lein prod-build

prod-build-android: export TARGET_OS ?= android
prod-build-android:
	lein prod-build-android

prod-build-ios: export TARGET_OS ?= ios
prod-build-ios:
	lein prod-build-ios

prod-build-desktop: export TARGET_OS ?= $(HOST_OS)
prod-build-desktop:
	git clean -qdxf -f ./index.desktop.js desktop/ && \
	lein prod-build-desktop

#--------------
# REPL
# -------------

_watch-%: ##@watch Start development for device
	$(eval SYSTEM := $(word 2, $(subst -, , $@)))
	$(eval DEVICE := $(word 3, $(subst -, , $@)))
	clj -R:dev build.clj watch --platform $(SYSTEM) --$(SYSTEM)-device $(DEVICE)

watch-ios-real: export TARGET_OS ?= ios
watch-ios-real: _watch-ios-real ##@watch Start development for iOS real device

watch-ios-simulator: export TARGET_OS ?= ios
watch-ios-simulator: _watch-ios-simulator ##@watch Start development for iOS simulator

watch-android-real: export TARGET_OS ?= android
watch-android-real: _watch-android-real ##@watch Start development for Android real device

watch-android-avd: export TARGET_OS ?= android
watch-android-avd: _watch-android-avd ##@watch Start development for Android AVD

watch-android-genymotion: export TARGET_OS ?= android
watch-android-genymotion: _watch-android-genymotion ##@watch Start development for Android Genymotion

watch-desktop: export TARGET_OS ?= $(HOST_OS)
watch-desktop: ##@watch Start development for Desktop
	clj -R:dev build.clj watch --platform desktop

desktop-server: export TARGET_OS ?= $(HOST_OS)
desktop-server:
	node ubuntu-server.js

#--------------
# Run
# -------------
_run-%:
	$(eval SYSTEM := $(word 2, $(subst -, , $@)))
	npx react-native run-$(SYSTEM)

run-android: export TARGET_OS ?= android
run-android: ##@run Run Android build
	npx react-native run-android --appIdSuffix debug

run-desktop: export TARGET_OS ?= $(HOST_OS)
run-desktop: _run-desktop ##@run Run Desktop build

SIMULATOR=
run-ios: export TARGET_OS ?= ios
run-ios: ##@run Run iOS build
ifneq ("$(SIMULATOR)", "")
	npx react-native run-ios --simulator="$(SIMULATOR)"
else
	npx react-native run-ios
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
react-native-desktop: export TARGET_OS ?= $(HOST_OS)
react-native-desktop: ##@other Start react native packager
	@scripts/start-react-native.sh

react-native-android: export TARGET_OS ?= android
react-native-android: ##@other Start react native packager for Android client
	@scripts/start-react-native.sh

react-native-ios: export TARGET_OS ?= ios
react-native-ios: ##@other Start react native packager for Android client
	@scripts/start-react-native.sh

geth-connect: export TARGET_OS ?= android
geth-connect: ##@other Connect to Geth on the device
	adb forward tcp:8545 tcp:8545 && \
	build/bin/geth attach http://localhost:8545

android-ports: export TARGET_OS ?= android
android-ports: ##@other Add proxies to Android Device/Simulator
	adb reverse tcp:8081 tcp:8081 && \
	adb reverse tcp:3449 tcp:3449 && \
	adb reverse tcp:4567 tcp:4567 && \
	adb forward tcp:5561 tcp:5561

android-logcat: export TARGET_OS ?= android
android-logcat:
	adb logcat | grep -e StatusModule -e ReactNativeJS -e StatusNativeLogs

_list: SHELL := /bin/sh
_list:
	@$(MAKE) -pRrq -f $(lastword $(MAKEFILE_LIST)) : 2>/dev/null | awk -v RS= -F: '/^# File/,/^# Finished Make data base/ {if ($$1 !~ "^[#.]") {print $$1}}' | sort | egrep -v -e '^[^[:alnum:]]' -e '^$@$$'

_unknown-startdev-target-%: SHELL := /bin/sh
_unknown-startdev-target-%:
	@ echo "Unknown target device '$*'. Supported targets:"; \
	${MAKE} _list | grep "watch-" | sed s/watch-/startdev-/; \
	exit 1

_startdev-%:
	$(eval SYSTEM := $(word 2, $(subst -, , $@)))
	$(eval DEVICE := $(word 3, $(subst -, , $@)))
	@ if [ -z "$(DEVICE)" ]; then \
		$(MAKE) watch-$(SYSTEM) || $(MAKE) _unknown-startdev-target-$@; \
	else \
		$(MAKE) watch-$(SYSTEM)-$(DEVICE) || $(MAKE) _unknown-startdev-target-$@; \
	fi

startdev-android-avd: export TARGET_OS ?= android
startdev-android-avd: _startdev-android-avd
startdev-android-genymotion: export TARGET_OS ?= android
startdev-android-genymotion: _startdev-android-genymotion
startdev-android-real: export TARGET_OS ?= android
startdev-android-real: _startdev-android-real
startdev-desktop: export TARGET_OS ?= $(HOST_OS)
startdev-desktop: _startdev-desktop
startdev-ios-real: export TARGET_OS ?= ios
startdev-ios-real: _startdev-ios-real
startdev-ios-simulator: export TARGET_OS ?= ios
startdev-ios-simulator: _startdev-ios-simulator
