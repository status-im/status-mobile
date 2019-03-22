.PHONY: clean setup react-native test release _ensure-in-nix-shell _list

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
BOLD   := $(shell tput bold)
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

__toolversion = $(shell $(GIT_ROOT)/scripts/toolversion $(1))

# Main targets

clean: ##@prepare Remove all output folders
	git clean -dxf -f

setup: ##@prepare Install all the requirements for status-react
	@./scripts/setup

_ensure-in-nix-shell:
ifndef IN_NIX_SHELL
	$(error '$(RED)Please run '$(BOLD)$(MAKE) shell$(RESET)$(RED)' first$(RESET)')
endif

shell: ##@prepare Enter into a pre-configured shell
ifndef IN_NIX_SHELL
	@if command -v "nix" >/dev/null 2>&1 || [ -f ~/.nix-profile/etc/profile.d/nix.sh ]; then \
		echo "Configuring Nix shell..."; \
		if ! command -v "nix" >/dev/null 2>&1; then \
			. ~/.nix-profile/etc/profile.d/nix.sh; \
			NIX_CONF_DIR=$(PWD)/scripts/lib/setup/nix \
				nix-shell; \
		else \
			NIX_CONF_DIR=$(PWD)/scripts/lib/setup/nix \
				nix-shell; \
		fi \
	else \
		echo "Please run 'make setup' first"; \
	fi
else
	@echo "Nix shell is already active"
endif

#----------------
# Release builds
#----------------
release: release-android release-ios ##@build build release for Android and iOS

release-android: prod-build-android ##@build build release for Android
ifdef IN_NIX_SHELL
	react-native run-android --variant=release
endif

release-ios: prod-build-ios ##@build build release for iOS release
ifdef IN_NIX_SHELL
	@echo "Build in XCode, see https://status.im/build_status/ for instructions"
	# Open XCode inside the Nix context
	open ios/StatusIm.xcworkspace
endif

release-desktop: prod-build-desktop ##@build build release for desktop release
ifdef IN_NIX_SHELL
	scripts/build-desktop.sh
endif

release-windows-desktop: prod-build-desktop ##@build build release for desktop release
ifdef IN_NIX_SHELL
	TARGET_SYSTEM_NAME=Windows scripts/build-desktop.sh
endif

prod-build: _ensure-in-nix-shell
	scripts/prepare-for-platform.sh android
	scripts/prepare-for-platform.sh ios
	lein prod-build

prod-build-android: _ensure-in-nix-shell
	rm ./modules/react-native-status/android/libs/status-im/status-go/local/status-go-local.aar 2> /dev/null || true
	scripts/prepare-for-platform.sh android
	lein prod-build-android

prod-build-ios: _ensure-in-nix-shell
	rm -r ./modules/react-native-status/ios/RCTStatus/Statusgo.framework/ 2> /dev/null || true
	scripts/prepare-for-platform.sh ios
	lein prod-build-ios

full-prod-build: _ensure-in-nix-shell ##@build build prod for both Android and iOS
	./scripts/bundle-status-go.sh ios android
	$(MAKE) prod-build
	rm -r ./modules/react-native-status/ios/RCTStatus/Statusgo.framework/ 2> /dev/null || true
	rm ./modules/react-native-status/android/libs/status-im/status-go/local/status-go-local.aar 2> /dev/null

prod-build-desktop: _ensure-in-nix-shell
	git clean -qdxf -f ./index.desktop.js desktop/
	scripts/prepare-for-platform.sh desktop
	lein prod-build-desktop

#--------------
# REPL
# -------------

_watch-%: _ensure-in-nix-shell ##@watch Start development for device
	$(eval SYSTEM := $(word 2, $(subst -, , $@)))
	$(eval DEVICE := $(word 3, $(subst -, , $@)))
	scripts/prepare-for-platform.sh $(SYSTEM)
	clj -R:dev build.clj watch --platform $(SYSTEM) --$(SYSTEM)-device $(DEVICE)

watch-ios-real: _watch-ios-real ##@watch Start development for iOS real device

watch-ios-simulator: _watch-ios-simulator ##@watch Start development for iOS simulator

watch-android-real: _watch-android-real ##@watch Start development for Android real device

watch-android-avd: _watch-android-avd ##@watch Start development for Android AVD

watch-android-genymotion: _watch-android-genymotion ##@watch Start development for Android Genymotion

watch-desktop: ##@watch Start development for Desktop
	scripts/prepare-for-platform.sh desktop
	clj -R:dev build.clj watch --platform desktop

#--------------
# Run
# -------------
_run-%: _ensure-in-nix-shell
	$(eval SYSTEM := $(word 2, $(subst -, , $@)))
	scripts/prepare-for-platform.sh $(SYSTEM)
	react-native run-$(SYSTEM)

run-android: _ensure-in-nix-shell ##@run Run Android build
	scripts/prepare-for-platform.sh android
	react-native run-android --appIdSuffix debug

run-desktop: _run-desktop ##@run Run Desktop build

SIMULATOR=
run-ios: _ensure-in-nix-shell ##@run Run iOS build
	scripts/prepare-for-platform.sh ios
ifneq ("$(SIMULATOR)", "")
	react-native run-ios --simulator="$(SIMULATOR)"
else
	react-native run-ios
endif

#--------------
# Tests
#--------------

test: _ensure-in-nix-shell ##@test Run tests once in NodeJS
	lein with-profile test doo node test once

test-auto: _ensure-in-nix-shell ##@test Run tests in interactive (auto) mode in NodeJS
	lein with-profile test doo node test

#--------------
# Other
#--------------
react-native: _ensure-in-nix-shell ##@other Start react native packager
	@scripts/start-react-native.sh

geth-connect: _ensure-in-nix-shell ##@other Connect to Geth on the device
	adb forward tcp:8545 tcp:8545
	build/bin/geth attach http://localhost:8545

android-ports: _ensure-in-nix-shell ##@other Add proxies to Android Device/Simulator
	adb reverse tcp:8081 tcp:8081
	adb reverse tcp:3449 tcp:3449
	adb reverse tcp:4567 tcp:4567
	adb forward tcp:5561 tcp:5561

android-logcat: _ensure-in-nix-shell
	adb logcat | grep -e StatusModule -e ReactNativeJS -e StatusNativeLogs

_list:
	@$(MAKE) -pRrq -f $(lastword $(MAKEFILE_LIST)) : 2>/dev/null | awk -v RS= -F: '/^# File/,/^# Finished Make data base/ {if ($$1 !~ "^[#.]") {print $$1}}' | sort | egrep -v -e '^[^[:alnum:]]' -e '^$@$$'

_unknown-startdev-target-%:
	@ echo "Unknown target device '$*'. Supported targets:"
	@ ${MAKE} _list | grep "watch-" | sed s/watch-/startdev-/
	@ exit 1

_startdev-%: _ensure-in-nix-shell
	$(eval SYSTEM := $(word 2, $(subst -, , $@)))
	$(eval DEVICE := $(word 3, $(subst -, , $@)))
	$(MAKE) prepare-${SYSTEM} || $(MAKE) _unknown-startdev-target-$@
	@ if [ -z "$(DEVICE)" ]; then \
		$(MAKE) watch-$(SYSTEM) || $(MAKE) _unknown-startdev-target-$@; \
	else \
		$(MAKE) watch-$(SYSTEM)-$(DEVICE) || $(MAKE) _unknown-startdev-target-$@; \
	fi

startdev-android-avd: _startdev-android-avd
startdev-android-genymotion: _startdev-android-genymotion
startdev-android-real: _startdev-android-real
startdev-desktop: _startdev-desktop
startdev-ios-real: _startdev-ios-real
startdev-ios-simulator: _startdev-ios-simulator
