.PHONY: nix-add-gcroots clean nix-clean run-metro test release _list _fix-node-perms _tmpdir-rm

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
			 print "Usage: make [target]\n\nSee STARTING_GUIDE.md for more info.\n\n"; \
			 for (sort keys %help) { \
				 print "${WHITE}$$_:${RESET}\n"; \
				 for (@{$$help{$$_}}) { \
					 $$sep = " " x (32 - length $$_->[0]); \
					 print "  ${YELLOW}$$_->[0]${RESET}$$sep${GREEN}$$_->[1]${RESET}\n"; \
				 }; \
				 print "\n"; \
			 }
HOST_OS := $(shell uname | tr '[:upper:]' '[:lower:]')

# This can come from Jenkins
ifndef BUILD_TAG
export BUILD_TAG := $(shell git rev-parse --short HEAD)
endif

# We don't want to use /run/user/$UID because it runs out of space too easilly.
export TMPDIR = /tmp/tmp-status-mobile-$(BUILD_TAG)
# This has to be specified for both the Node.JS server process and the Qt process.
export REACT_SERVER_PORT ?= 5001
# Fix for ERR_OSSL_EVP_UNSUPPORTED error.
export NODE_OPTIONS += --openssl-legacy-provider
# The path can be anything, but home is usually safest.
export KEYSTORE_PATH ?= $(HOME)/.gradle/status-im.keystore

# Our custom config is located in nix/nix.conf
export NIX_USER_CONF_FILES = $(PWD)/nix/nix.conf
# Location of symlinks to derivations that should not be garbage collected
export _NIX_GCROOTS = /nix/var/nix/gcroots/per-user/$(USER)/status-mobile
# Defines which variables will be kept for Nix pure shell, use semicolon as divider
export _NIX_KEEP ?= TMPDIR,BUILD_ENV,\
	BUILD_TYPE,BUILD_NUMBER,COMMIT_HASH,\
	ANDROID_GRADLE_OPTS,ANDROID_ABI_SPLIT,ANDROID_ABI_INCLUDE,\
	STATUS_GO_SRC_OVERRIDE,STATUS_GO_IPFS_GATEWAY_URL

# Useful for Android release builds
TMP_BUILD_NUMBER := $(shell ./scripts/version/gen_build_no.sh | cut -c1-10)

# MacOS root is read-only, read nix/README.md for details
UNAME_S := $(shell uname -s)
ifeq ($(UNAME_S),Darwin)
export NIX_IGNORE_SYMLINK_STORE=1
endif

#----------------
# Nix targets
#----------------

# WARNING: This has to be located right before all the targets.
SHELL := ./nix/scripts/shell.sh

shell: export TARGET ?= default
shell: ##@prepare Enter into a pre-configured shell
ifndef IN_NIX_SHELL
	@ENTER_NIX_SHELL
else
	@echo "${YELLOW}Nix shell is already active$(RESET)"
endif

nix-repl: SHELL := /bin/sh
nix-repl: ##@nix Start an interactive Nix REPL
	nix repl default.nix

nix-gc-protected: SHELL := /bin/sh
nix-gc-protected:
	@echo -e "$(YELLOW)The following paths are protected:$(RESET)" && \
	ls -1 $(_NIX_GCROOTS) | sed 's/^/ - /'

nix-upgrade: SHELL := /bin/sh
nix-upgrade: ##@nix Upgrade Nix interpreter to current version.
	nix/scripts/upgrade.sh

nix-gc: export TARGET := nix
nix-gc: nix-gc-protected ##@nix Garbage collect all packages older than 20 days from /nix/store
	nix-store --gc

nix-clean: export TARGET := default
nix-clean: ##@nix Remove all status-mobile build artifacts from /nix/store
	nix/scripts/clean.sh

nix-purge: SHELL := /bin/sh
nix-purge: ##@nix Completely remove Nix setup, including /nix directory
	nix/scripts/purge.sh

nix-update-gradle: export TARGET := gradle
nix-update-gradle: ##@nix Update maven nix expressions based on current gradle setup
	nix/deps/gradle/generate.sh

nix-update-clojure: export TARGET := clojure
nix-update-clojure: ##@nix Update maven Nix expressions based on current clojure setup
	nix/deps/clojure/generate.sh

nix-update-gems: export TARGET := default
nix-update-gems: ##@nix Update Ruby gems in fastlane/Gemfile.lock and fastlane/gemset.nix
	fastlane/update.sh

nix-update-pods: export TARGET := ios
nix-update-pods: ##@nix Update CocoaPods in ios/Podfile.lock
	cd ios && pod update

#----------------
# General targets
#----------------

_fix-node-perms: SHELL := /bin/sh
_fix-node-perms: ##@prepare Fix permissions so that directory can be cleaned
	$(shell test -d node_modules && chmod -R 744 node_modules)
	$(shell test -d node_modules.tmp && chmod -R 744 node_modules.tmp)

$(TMPDIR): SHELL := /bin/sh
$(TMPDIR): ##@prepare Create a TMPDIR for temporary files
	@mkdir -p "$(TMPDIR)"
# Make sure TMPDIR exists every time make is called
_tmpdir-mk: $(TMPDIR)
-include _tmpdir-mk

_tmpdir-rm: SHELL := /bin/sh
_tmpdir-rm: ##@prepare Remove TMPDIR
	rm -fr "$(TMPDIR)"

_install-hooks: SHELL := /bin/sh
_install-hooks: ##@prepare Create prepare-commit-msg git hook symlink
	@ln -s -f ../../scripts/hooks/prepare-commit-msg .git/hooks
-include _install-hooks

# Remove directories and ignored files
clean: SHELL := /bin/sh
clean: _fix-node-perms _tmpdir-rm ##@prepare Remove all output folders
	git clean -dXf

# Remove directories, ignored and non-ignored files
purge: SHELL := /bin/sh
purge: _fix-node-perms _tmpdir-rm ##@prepare Remove all output folders
	git clean -dxf


watchman-clean: export TARGET := watchman
watchman-clean: ##@prepare Delete repo directory from watchman
	watchman watch-del $${STATUS_MOBILE_HOME}

pod-install: export TARGET := ios
pod-install: ##@prepare Run 'pod install' to install podfiles and update Podfile.lock
	cd ios && pod install; cd --

update-fleets: ##@prepare Download up-to-date JSON file with current fleets state
	curl -s https://fleets.status.im/ \
		| sed 's/"warning": "/"warning": "DO NOT EDIT! /' \
		> resources/config/fleets.json

$(KEYSTORE_PATH): export TARGET := keytool
$(KEYSTORE_PATH):
	@./scripts/generate-keystore.sh

keystore: $(KEYSTORE_PATH) ##@prepare Generate a Keystore for signing Android APKs

fdroid-max-watches: SHELL := /bin/sh
fdroid-max-watches: ##@prepare Bump max_user_watches to avoid ENOSPC errors
	sysctl fs.inotify.max_user_watches=524288

fdroid-nix-dir: SHELL := /bin/sh
fdroid-nix-dir: ##@prepare Create /nix directory for F-Droid Vagrant builders
	mkdir -m 0755 /nix
	chown vagrant /nix

fdroid-fix-tmp: SHELL := /bin/sh
fdroid-fix-tmp: ##@prepare Fix TMPDIR permissions so Vagrant user is the owner
	chown -R vagrant "$(TMPDIR)"

fdroid-build-env: fdroid-max-watches fdroid-nix-dir fdroid-fix-tmp ##@prepare Setup build environment for F-Droud build

fdroid-pr: export TARGET := android-sdk
fdroid-pr: ##@prepare Create F-Droid release PR
ifndef APK
	$(error APK env var not defined)
endif
	scripts/fdroid-pr.sh "$(APK)"

xcode-clean: SHELL := /bin/sh
xcode-clean: XCODE_HOME := $(HOME)/Library/Developer/Xcode
xcode-clean: ##@prepare Clean XCode derived data and archives
	rm -fr $(XCODE_HOME)/DerivedData/StatusIm-* $(XCODE_HOME)/Archives/*/StatusIm*

#----------------
# Release builds
#----------------
release: release-android release-ios ##@build Build release for Android and iOS

build-fdroid: export BUILD_ENV = prod
build-fdroid: export BUILD_TYPE = release
build-fdroid: export ANDROID_ABI_SPLIT = false
build-fdroid: export ANDROID_ABI_INCLUDE = armeabi-v7a;arm64-v8a;x86;x86_64
build-fdroid: ##@build Build release for F-Droid
	@scripts/build-android.sh

build-android: export BUILD_ENV ?= prod
build-android: export BUILD_TYPE ?= nightly
build-android: export BUILD_NUMBER ?= $(TMP_BUILD_NUMBER)
build-android: export ANDROID_ABI_SPLIT ?= false
build-android: export ANDROID_ABI_INCLUDE ?= armeabi-v7a;arm64-v8a;x86
build-android: ##@build Build unsigned Android APK
	@scripts/build-android.sh

release-android: export TARGET := keytool
release-android: export KEYSTORE_PATH ?= $(HOME)/.gradle/status-im.keystore
release-android: keystore build-android ##@build Build signed Android APK
	@scripts/sign-android.sh result/app-release-unsigned.apk

release-ios: export TARGET := ios
release-ios: export IOS_STATUS_GO_TARGETS := ios/arm64
release-ios: export BUILD_ENV ?= prod
release-ios: watchman-clean ios-clean jsbundle ##@build Build release for iOS release
	xcodebuild \
		-scheme StatusIm \
		-configuration Release \
		-workspace ios/StatusIm.xcworkspace \
		-destination 'generic/platform=iOS' \
		-UseModernBuildSystem=N clean archive

jsbundle: SHELL := /bin/sh
jsbundle: export BUILD_ENV ?= prod
jsbundle: ##@build Build JavaScript and Clojurescript bundle for iOS and Android
	nix/scripts/build.sh targets.mobile.jsbundle

#--------------
# status-go lib
#--------------

status-go-android: SHELL := /bin/sh
status-go-android: ##@status-go Compile status-go for Android app
	nix/scripts/build.sh targets.status-go.mobile.android

status-go-ios: SHELL := /bin/sh
status-go-ios: ##@status-go Compile status-go for iOS app
	nix/scripts/build.sh targets.status-go.mobile.ios

status-go-library: SHELL := /bin/sh
status-go-library: ##@status-go Compile status-go for node-js
	nix/scripts/build.sh targets.status-go.library

#--------------
# Watch, Build & Review changes
#--------------

run-clojure: export TARGET := clojure
run-clojure: ##@run Watch for and build Clojure changes for mobile
	yarn shadow-cljs watch mobile

run-metro: export TARGET := clojure
run-metro: ##@run Start Metro to build React Native changes
	@scripts/start-react-native.sh

run-re-frisk: export TARGET := clojure
run-re-frisk: ##@run Start re-frisk server
	yarn shadow-cljs run re-frisk-remote.core/start

# TODO: Migrate this to a Nix recipe, much the same way as nix/mobile/android/targets/release-android.nix
run-android: export TARGET := android
# INFO: If it's empty (no devices attached, parsing issues, script error) - for Nix it's the same as not set.
run-android: export ANDROID_ABI_INCLUDE ?= $(shell ./scripts/adb_devices_arch.sh)
run-android: ##@run Build Android APK and start it on the device
	npx react-native run-android --appIdSuffix debug

SIMULATOR=iPhone 11 Pro
run-ios: export TARGET := ios
run-ios: export IOS_STATUS_GO_TARGETS := iossimulator/amd64
run-ios: ##@run Build iOS app and start it in a simulator/device
ifneq ("$(SIMULATOR)", "")
	npx react-native run-ios --simulator="$(SIMULATOR)"
else
	npx react-native run-ios
endif

show-ios-devices: ##@other shows connected ios device and its name
	xcrun xctrace list devices

run-ios-device: export TARGET := ios
run-ios-device: export IOS_STATUS_GO_TARGETS := ios/arm64
run-ios-device: ##@run iOS app and start it on a connected device by its name
ifndef DEVICE_NAME
	$(error Usage: make run-ios-device DEVICE_NAME=your-device-name)
endif
	react-native run-ios --device "$(DEVICE_NAME)"

#--------------
# Tests
#--------------

# Get all clojure files, including untracked, excluding removed
define find_all_clojure_files
$$(comm -23 <(sort <(git ls-files --cached --others --exclude-standard)) <(sort <(git ls-files --deleted)) | grep -e \.clj$$ -e \.cljs$$ -e \.cljc$$ -e \.edn)
endef

lint: export TARGET := clojure
lint: ##@test Run code style checks
	@sh scripts/lint-re-frame-in-quo-components.sh && \
	clj-kondo --config .clj-kondo/config.edn --cache false --fail-level error --lint src && \
	ALL_CLOJURE_FILES=$(call find_all_clojure_files) && \
	zprint '{:search-config? true}' -sfc $$ALL_CLOJURE_FILES && \
	sh scripts/lint-trailing-newline.sh && \
	yarn prettier

# NOTE: We run the linter twice because of https://github.com/kkinnear/zprint/issues/271
lint-fix: export TARGET := clojure
lint-fix: ##@test Run code style checks and fix issues
	ALL_CLOJURE_FILES=$(call find_all_clojure_files) && \
	zprint '{:search-config? true}' -sw $$ALL_CLOJURE_FILES && \
	zprint '{:search-config? true}' -sw $$ALL_CLOJURE_FILES && \
	sh scripts/lint-trailing-newline.sh --fix && \
	yarn prettier

shadow-server: export TARGET := clojure
shadow-server:##@ Start shadow-cljs in server mode for watching
	yarn shadow-cljs server

test-watch: export TARGET := clojure
test-watch: ##@ Watch tests and re-run no changes to cljs files
	yarn install
	nodemon --exec 'yarn shadow-cljs compile mocks && yarn shadow-cljs compile test && node --require ./test-resources/override.js target/test/test.js' -e cljs

test-watch-for-repl: export TARGET := clojure
test-watch-for-repl: ##@ Watch tests and support REPL connections
	yarn install
	rm -f target/test/test.js
	concurrently --kill-others --prefix-colors 'auto' --names 'build,repl' \
		'yarn shadow-cljs compile mocks && yarn shadow-cljs watch test --verbose' \
		'until [ -f ./target/test/test.js ] ; do sleep 1 ; done ; node --require ./test-resources/override.js ./target/test/test.js --repl'

test: export TARGET := clojure
test: ##@test Run tests once in NodeJS
	# Here we create the gyp bindings for nodejs
	yarn install
	yarn shadow-cljs compile mocks && \
	yarn shadow-cljs compile test && \
	node --require ./test-resources/override.js target/test/test.js

android-test: jsbundle
android-test: export TARGET := android
android-test:
	cd android && ./gradlew test

component-test-watch: export TARGET := clojure
component-test-watch: export COMPONENT_TEST := true
component-test-watch: export BABEL_ENV := test
component-test-watch: ##@ Watch tests and re-run no changes to cljs files
	yarn install
	nodemon --exec 'yarn shadow-cljs compile component-test && jest --config=test/jest/jest.config.js' -e cljs

component-test: export TARGET := clojure
component-test: export COMPONENT_TEST := true
component-test: export BABEL_ENV := test
component-test: ##@test Run component tests once in NodeJS
	yarn install
	yarn shadow-cljs compile component-test && \
	jest --config=test/jest/jest.config.js

#--------------
# Other
#--------------

geth-connect: export TARGET := android-sdk
geth-connect: ##@other Connect to Geth on the device
	adb forward tcp:8545 tcp:8545 && \
	build/bin/geth attach http://localhost:8545

ios-clean: SHELL := /bin/sh
ios-clean: ##@prepare Clean iOS build artifacts
	git clean -dxf -f target/ios

android-clean: export TARGET := gradle
android-clean: ##@prepare Clean Gradle state
	git clean -dxf -f ./android/app/build; \
	[[ -d android/.gradle ]] && cd android && ./gradlew clean

android-ports: export TARGET := android-sdk
android-ports: ##@other Add proxies to Android Device/Simulator
	adb reverse tcp:8081 tcp:8081 && \
	adb reverse tcp:3449 tcp:3449 && \
	adb reverse tcp:4567 tcp:4567 && \
	adb forward tcp:5561 tcp:5561

android-devices: export TARGET := android-sdk
android-devices: ##@other Invoke adb devices
	adb devices

android-logcat: export TARGET := android-sdk
android-logcat: ##@other Read status-mobile logs from Android phone using adb
	adb logcat | grep -e RNBootstrap -e ReactNativeJS -e ReactNative -e StatusModule -e StatusNativeLogs -e 'F DEBUG   :' -e 'Go      :' -e 'GoLog   :' -e 'libc    :'

android-install: export TARGET := android-sdk
android-install: export BUILD_TYPE ?= release
android-install: ##@other Install APK on device using adb
	adb install result/app-$(BUILD_TYPE).apk

_list: SHELL := /bin/sh
_list:
	@$(MAKE) -pRrq -f $(lastword $(MAKEFILE_LIST)) : 2>/dev/null | awk -v RS= -F: '/^# File/,/^# Finished Make data base/ {if ($$1 !~ "^[#.]") {print $$1}}' | sort | egrep -v -e '^[^[:alnum:]]' -e '^$@$$'

#--------------
# REPLs
#--------------

repl-clojure: export TARGET := clojure
repl-clojure: ##@repl Start Clojure repl for mobile App
	yarn shadow-cljs cljs-repl mobile

repl-nix: nix-repl ##@repl Start an interactive Nix REPL
