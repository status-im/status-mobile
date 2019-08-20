.PHONY: nix-add-gcroots clean nix-clean disable-githooks react-native-android react-native-ios react-native-desktop test release _list _fix-node-perms

help: SHELL := /bin/sh
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
# We don't want to use /run/user/$UID because it runs out of space too easilly
export TMPDIR = /tmp

export REACT_SERVER_PORT ?= 5001 # any value different from default 5000 will work; this has to be specified for both the Node.JS server process and the Qt process

#----------------
# Nix targets
#----------------

# WARNING: This has to be located right before the targets
ifdef IN_NIX_SHELL
SHELL := env bash
else
SHELL := ./nix/shell.sh
endif

shell: ##@prepare Enter into a pre-configured shell
ifndef IN_NIX_SHELL
	@ENTER_NIX_SHELL
else
	@echo "${YELLOW}Nix shell is already active$(RESET)"
endif

nix-clean: ##@nix Remove all status-react build artifacts from /nix/store
	nix/clean.sh

nix-purge: SHELL := /bin/sh
nix-purge: ##@nix Completely remove the complete Nix setup
	sudo rm -rf /nix ~/.nix-profile ~/.nix-defexpr ~/.nix-channels ~/.cache/nix ~/.status .nix-gcroots

nix-add-gcroots: export TARGET_OS := none
nix-add-gcroots: ##@nix Add Nix GC roots to avoid status-react expressions being garbage collected
	scripts/add-nix-gcroots.sh

nix-update-gradle: ##@nix Update maven nix expressions based on current gradle setup
	nix/mobile/android/maven-and-npm-deps/maven/generate-nix.sh

nix-update-lein: export TARGET_OS := none
nix-update-lein: ##@nix Update maven nix expressions based on current lein setup
	nix/tools/lein/generate-nix.sh nix/lein

update-gems-nix: export TARGET_OS := android
update-gems-nix: ##@nix Update Ruby gems in fastlane/Gemfile.lock and fastlane/gemset.nix
	fastlane/update.sh

#----------------
# General targets
#----------------

_fix-node-perms: SHELL := /bin/sh
_fix-node-perms: ##@prepare Fix permissions so that directory can be cleaned
	$(shell test -d node_modules && chmod -R 744 node_modules)
	$(shell test -d node_modules.tmp && chmod -R 744 node_modules.tmp)

clean: SHELL := /bin/sh
clean: _fix-node-perms ##@prepare Remove all output folders
	git clean -dxf -f

watchman-clean: export _NIX_ATTR := targets.watchman.shell
watchman-clean: ##@prepare Delete repo directory from watchman 
	watchman watch-del $${STATUS_REACT_HOME}

disable-githooks: SHELL := /bin/sh
disable-githooks: ##@prepare Disables lein githooks
	@rm -f ${env.WORKSPACE}/.git/hooks/pre-commit && \
	sed -i'~' -e 's|\[rasom/lein-githooks|;; [rasom/lein-githooks|' \
		-e 's|:githooks|;; :githooks|' \
		-e 's|:pre-commit|;; :pre-commit|' project.clj; \
	rm project.clj~

#----------------
# Release builds
#----------------
release: release-android release-ios ##@build build release for Android and iOS

release-android: export TARGET_OS ?= android
release-android: export BUILD_ENV ?= prod
release-android: export BUILD_TYPE ?= nightly
release-android: export BUILD_NUMBER ?= 9999
release-android: export NDK_ABI_FILTERS ?= armeabi-v7a;arm64-v8a;x86
release-android: export STORE_FILE ?= ~/.gradle/status-im.keystore
release-android: ##@build build release for Android
	nix/build.sh targets.mobile.$(TARGET_OS).release \
		--arg env '{NDK_ABI_FILTERS="$(NDK_ABI_FILTERS)";}' \
		--argstr build-type $(BUILD_TYPE) \
		--argstr build-number $(BUILD_NUMBER) \
		--argstr keystore-file $(STORE_FILE) \
		--option extra-sandbox-paths $(STORE_FILE)

release-ios: export TARGET_OS ?= ios
release-ios: export BUILD_ENV ?= prod
release-ios: watchman-clean ##@build build release for iOS release
	# Open XCode inside the Nix context
	@git clean -dxf -f target/ios && \
	$(MAKE) jsbundle-ios && \
	echo "Build in XCode, see https://status.im/build_status/ for instructions" && \
	scripts/copy-translations.sh && \
	open ios/StatusIm.xcworkspace

release-desktop: export TARGET_OS ?= $(HOST_OS)
release-desktop: ##@build build release for desktop release based on TARGET_OS
	@$(MAKE) jsbundle-desktop && \
	scripts/copy-translations.sh && \
	scripts/build-desktop.sh; \
	$(MAKE) watchman-clean

release-windows-desktop: export TARGET_OS ?= windows
release-windows-desktop: ##@build build release for windows desktop release
	@$(MAKE) jsbundle-desktop && \
	scripts/copy-translations.sh && \
	scripts/build-desktop.sh; \
	$(MAKE) watchman-clean

prod-build-android: jsbundle-android ##@legacy temporary legacy alias for jsbundle-android
	@echo "${YELLOW}This a deprecated target name, use jsbundle-android.$(RESET)"

jsbundle-android: SHELL := /bin/sh
jsbundle-android: export TARGET_OS ?= android
jsbundle-android: export BUILD_ENV ?= prod
jsbundle-android: ##@jsbundle Compile JavaScript and Clojure into index.android.js 
	# Call nix-build to build the 'targets.mobile.jsbundle' attribute and copy the index.android.js file to the project root
	@git clean -dxf ./index.$(TARGET_OS).js
	nix/build.sh targets.mobile.jsbundle && \
	mv result/index.$(TARGET_OS).js ./

prod-build-ios: jsbundle-ios ##@legacy temporary legacy alias for jsbundle-ios
	@echo "${YELLOW}This a deprecated target name, use jsbundle-ios.$(RESET)"

jsbundle-ios: export TARGET_OS ?= ios
jsbundle-ios: export BUILD_ENV ?= prod
jsbundle-ios: ##@jsbundle Compile JavaScript and Clojure into index.ios.js 
	@git clean -dxf -f ./index.$(TARGET_OS).js && \
	lein jsbundle-ios && \
	node prepare-modules.js

prod-build-desktop: jsbundle-desktop ##@legacy temporary legacy alias for jsbundle-desktop
	@echo "${YELLOW}This a deprecated target name, use jsbundle-desktop.$(RESET)"

jsbundle-desktop: export TARGET_OS ?= $(HOST_OS)
jsbundle-desktop: export BUILD_ENV ?= prod
jsbundle-desktop: ##@jsbundle Compile JavaScript and Clojure into index.desktop.js 
	git clean -qdxf -f ./index.desktop.js desktop/ && \
	lein jsbundle-desktop && \
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
react-native-desktop: export _NIX_PURE ?= true
react-native-desktop: ##@other Start react native packager
	@scripts/start-react-native.sh

react-native-android: export TARGET_OS ?= android
react-native-android: export _NIX_PURE ?= true
react-native-android: ##@other Start react native packager for Android client
	@scripts/start-react-native.sh

react-native-ios: export TARGET_OS ?= ios
react-native-ios: export _NIX_PURE ?= true
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
android-logcat: ##@other Read status-react logs from Android phone using adb
	adb logcat | grep -e RNBootstrap -e ReactNativeJS -e ReactNative -e StatusModule -e StatusNativeLogs

android-install: export _NIX_ATTR := targets.mobile.android.adb.shell
android-install: export TARGET_OS ?= android
android-install: export BUILD_TYPE ?= release
android-install:
	adb install result/app.apk

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

startdev-android-avd: export TARGET_OS = android
startdev-android-avd: _startdev-android-avd
startdev-android-genymotion: export TARGET_OS = android
startdev-android-genymotion: _startdev-android-genymotion
startdev-android-real: export TARGET_OS = android
startdev-android-real: _startdev-android-real
startdev-desktop: export TARGET_OS ?= $(HOST_OS)
startdev-desktop: _startdev-desktop
startdev-ios-real: export TARGET_OS = ios
startdev-ios-real: _startdev-ios-real
startdev-ios-simulator: export TARGET_OS = ios
startdev-ios-simulator: _startdev-ios-simulator
