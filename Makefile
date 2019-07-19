.PHONY: add-gcroots clean clean-nix disable-githooks react-native-android react-native-ios react-native-desktop test release _list _fix-perms

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

# Defines which variables will be kept for Nix pure shell, use semicolon as divider
export _NIX_KEEP ?= BUILD_ENV
export NIX_CONF_DIR = $(PWD)/nix

export REACT_SERVER_PORT ?= 5001 # any value different from default 5000 will work; this has to be specified for both the Node.JS server process and the Qt process

# WARNING: This has to be located right before the targets
ifdef IN_NIX_SHELL
SHELL := env bash
else
SHELL := ./nix/shell.sh
endif

# Main targets

_fix-perms: SHELL := /bin/sh
_fix-perms: ##@prepare Fix permissions so that directory can be cleaned
	$(shell test -d node_modules && chmod -R 744 node_modules)
	$(shell test -d node_modules.tmp && chmod -R 744 node_modules.tmp)

clean: SHELL := /bin/sh
clean: _fix-perms ##@prepare Remove all output folders
	git clean -dxf -f

watchman-clean: export _NIX_ATTR := targets.watchman.shell
watchman-clean:
	watchman watch-del $${STATUS_REACT_HOME}

shell: ##@prepare Enter into a pre-configured shell
ifndef IN_NIX_SHELL
	@ENTER_NIX_SHELL
else
	@echo "${YELLOW}Nix shell is already active$(RESET)"
endif

add-gcroots: SHELL := /bin/sh
add-gcroots: ##@nix Add Nix GC roots to avoid status-react expressions being garbage collected
	scripts/add-gcroots.sh

clean-nix: SHELL := /bin/sh
clean-nix: ##@nix Remove complete nix setup
	sudo rm -rf /nix ~/.nix-profile ~/.nix-defexpr ~/.nix-channels ~/.cache/nix ~/.status .nix-gcroots

update-npm-nix: SHELL := /bin/sh
update-npm-nix: ##@nix Update node2nix expressions based on current package.json
	nix/desktop/realm-node/generate-nix.sh

update-gradle-nix: SHELL := /bin/sh
update-gradle-nix: ##@nix Update maven nix expressions based on current gradle setup
	nix/mobile/android/maven-and-npm-deps/maven/generate-nix.sh

update-lein-nix: SHELL := /bin/sh
update-lein-nix: ##@nix Update maven nix expressions based on current lein setup
	nix/tools/lein/generate-nix.sh nix/lein

disable-githooks: SHELL := /bin/sh
disable-githooks:
	@rm -f ${env.WORKSPACE}/.git/hooks/pre-commit && \
	sed -i'~' -e 's|\[rasom/lein-githooks|;; [rasom/lein-githooks|' \
		-e 's|:githooks|;; :githooks|' \
		-e 's|:pre-commit|;; :pre-commit|' project.clj; \
	rm project.clj~

#----------------
# Release builds
#----------------
release: release-android release-ios ##@build build release for Android and iOS

release-android: SHELL := /bin/sh
release-android: export TARGET_OS ?= android
release-android: export BUILD_ENV ?= prod
release-android: ##@build build release for Android
	scripts/release-$(TARGET_OS).sh

release-ios: export TARGET_OS ?= ios
release-ios: export BUILD_ENV ?= prod
release-ios: watchman-clean ##@build build release for iOS release
	# Open XCode inside the Nix context
	@git clean -dxf -f target/ios && \
	$(MAKE) prod-build-ios && \
	echo "Build in XCode, see https://status.im/build_status/ for instructions" && \
	scripts/copy-translations.sh && \
	open ios/StatusIm.xcworkspace

release-desktop: export TARGET_OS ?= $(HOST_OS)
release-desktop: ##@build build release for desktop release
	@$(MAKE) prod-build-desktop && \
	scripts/copy-translations.sh && \
	scripts/build-desktop.sh; \
	$(MAKE) watchman-clean

release-windows-desktop: export TARGET_OS ?= windows
release-windows-desktop: ##@build build release for desktop release
	@$(MAKE) prod-build-desktop && \
	scripts/copy-translations.sh && \
	scripts/build-desktop.sh; \
	$(MAKE) watchman-clean

prod-build-android: SHELL := /bin/sh
prod-build-android: export TARGET_OS ?= android
prod-build-android: export BUILD_ENV ?= prod
prod-build-android:
	# Call nix-build to build the 'targets.mobile.prod-build' attribute and copy the index.android.js file to the project root
	@git clean -dxf -f ./index.$(TARGET_OS).js && \
	_NIX_RESULT_PATH=$(shell . ~/.nix-profile/etc/profile.d/nix.sh && nix-build --argstr target-os $(TARGET_OS) --pure --no-out-link --show-trace -A targets.mobile.prod-build) && \
	[ -n "$${_NIX_RESULT_PATH}" ] && cp -av $${_NIX_RESULT_PATH}/* .

prod-build-ios: export TARGET_OS ?= ios
prod-build-ios: export BUILD_ENV ?= prod
prod-build-ios:
	@git clean -dxf -f ./index.$(TARGET_OS).js && \
	lein prod-build-ios && \
	node prepare-modules.js

prod-build-desktop: export TARGET_OS ?= $(HOST_OS)
prod-build-desktop: export BUILD_ENV ?= prod
prod-build-desktop:
	git clean -qdxf -f ./index.desktop.js desktop/ && \
	lein prod-build-desktop && \
	node prepare-modules.js

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

# TODO: Migrate this to a Nix recipe, much the same way as nix/mobile/android/targets/release-android.nix
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

test: export _NIX_ATTR := targets.leiningen.shell
test: ##@test Run tests once in NodeJS
	lein with-profile test doo node test once

test-auto: export _NIX_ATTR := targets.leiningen.shell
test-auto: ##@test Run tests in interactive (auto) mode in NodeJS
	lein with-profile test doo node test

coverage: ##@test Run tests once in NodeJS generating coverage
	@scripts/run-coverage.sh

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

geth-connect: export _NIX_ATTR := targets.mobile.android.adb.shell
geth-connect: export TARGET_OS ?= android
geth-connect: ##@other Connect to Geth on the device
	adb forward tcp:8545 tcp:8545 && \
	build/bin/geth attach http://localhost:8545

android-ports: export _NIX_ATTR := targets.mobile.android.adb.shell
android-ports: export TARGET_OS ?= android
android-ports: ##@other Add proxies to Android Device/Simulator
	adb reverse tcp:8081 tcp:8081 && \
	adb reverse tcp:3449 tcp:3449 && \
	adb reverse tcp:4567 tcp:4567 && \
	adb forward tcp:5561 tcp:5561

android-logcat: export _NIX_ATTR := targets.mobile.android.adb.shell
android-logcat: export TARGET_OS ?= android
android-logcat:
	adb logcat | grep -e StatusModule -e ReactNativeJS -e StatusNativeLogs

android-install: export _NIX_ATTR := targets.mobile.android.adb.shell
android-install: export TARGET_OS ?= android
android-install: export BUILD_TYPE ?= release
android-install:
	adb install android/app/build/outputs/apk/$(BUILD_TYPE)/app-$(BUILD_TYPE).apk

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
