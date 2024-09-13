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

# We don't want to use /run/user/$UID because it runs out of space too easily.
export TMPDIR = /tmp/tmp-status-mobile-$(BUILD_TAG)
# This has to be specified for both the Node.JS server process and the Qt process.
export REACT_SERVER_PORT ?= 5001
# Default metro port used by scripts/run-android.sh.
export RCT_METRO_PORT ?= 8081
# Fix for ERR_OSSL_EVP_UNSUPPORTED error.
export NODE_OPTIONS += --openssl-legacy-provider
# The path can be anything, but home is usually safest.
export KEYSTORE_PATH ?= $(HOME)/.gradle/status-im.keystore

# Our custom config is located in nix/nix.conf
export NIX_USER_CONF_FILES = $(PWD)/nix/nix.conf
# Location of symlinks to derivations that should not be garbage collected
export _NIX_GCROOTS = ./.nix-gcroots
# Defines which variables will be kept for Nix pure shell, use semicolon as divider
export _NIX_KEEP ?= TMPDIR,BUILD_ENV,\
	BUILD_TYPE,BUILD_NUMBER,COMMIT_HASH,\
	ANDROID_GRADLE_OPTS,ANDROID_ABI_SPLIT,ANDROID_ABI_INCLUDE,\
	STATUS_GO_SRC_OVERRIDE,STATUS_GO_IPFS_GATEWAY_URL

# Useful for Android release builds
TMP_BUILD_NUMBER := $(shell ./scripts/version/gen_build_no.sh | cut -c1-10)

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
nix-update-gradle: export ORG_GRADLE_PROJECT_hermesEnabled := false
nix-update-gradle: export ORG_GRADLE_PROJECT_universalApk := false
nix-update-gradle: ##@nix Update maven nix expressions based on current gradle setup
	nix/deps/gradle/generate.sh

nix-update-clojure: export TARGET := clojure
nix-update-clojure: ##@nix Update maven Nix expressions based on current clojure setup
	nix/deps/clojure/generate.sh

nix-update-gems: export TARGET := fastlane
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
clean: _fix-node-perms _tmpdir-rm ios-clean android-clean ##@prepare Remove all output folders
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

FLEETS_FILE := ./resources/config/fleets.json
update-fleets: ##@prepare Download up-to-date JSON file with current fleets state
	curl -s https://fleets.status.im/ \
		| sed 's/"warning": "/"warning": "DO NOT EDIT! /' \
		> $(FLEETS_FILE)
	echo >> $(FLEETS_FILE)

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

ios-simulator-cache-clean: SHELL := /bin/sh
ios-simulator-cache-clean: ##@prepare Clean iOS Simulator Caches
	rm -rf ~/Library/Developer/CoreSimulator/Cache

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
build-android: export ORG_GRADLE_PROJECT_versionCode ?= $(TMP_BUILD_NUMBER)
build-android: ##@build Build unsigned Android APK
	@scripts/build-android.sh

release-android: export TARGET := keytool
release-android: export KEYSTORE_PATH ?= $(HOME)/.gradle/status-im.keystore
release-android: keystore build-android ##@build Build signed Android APK
	@scripts/sign-android.sh result/app-arm64-v8a-release-unsigned.apk

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
	@scripts/run-metro.sh

run-re-frisk: export TARGET := clojure
run-re-frisk: ##@run Start re-frisk server
	yarn shadow-cljs run re-frisk-remote.core/start

# TODO: Migrate this to a Nix recipe, much the same way as nix/mobile/android/targets/release-android.nix
run-android: export TARGET := android
# Disabled for debug builds to avoid 'maximum call stack exceeded' errors.
# https://github.com/status-im/status-mobile/issues/18493
run-android: export ORG_GRADLE_PROJECT_hermesEnabled := false
run-android: export ORG_GRADLE_PROJECT_universalApk := false
run-android: ##@run Build Android APK and start it on the device
	@scripts/run-android.sh

SIMULATOR=iPhone 13
# TODO: fix IOS_STATUS_GO_TARGETS to be either amd64 or arm64 when RN is upgraded
run-ios: export TARGET := ios
run-ios: export IOS_STATUS_GO_TARGETS := ios/arm64;iossimulator/amd64
run-ios: ##@run Build iOS app and start it in on the simulator
	@scripts/run-ios.sh "${SIMULATOR}"

show-ios-devices: ##@other shows connected ios device and its name
	xcrun xctrace list devices

# TODO: fix IOS_STATUS_GO_TARGETS to be either amd64 or arm64 when RN is upgraded
run-ios-device: export TARGET := ios
run-ios-device: export IOS_STATUS_GO_TARGETS := ios/arm64;iossimulator/amd64
run-ios-device: ##@run iOS app and start it on the first connected iPhone
	@scripts/run-ios-device.sh

#--------------
# Tests
#--------------

# Get all clojure files, including untracked, excluding removed
define find_all_clojure_files
$$(comm -23 <(sort <(git ls-files --cached --others --exclude-standard)) <(sort <(git ls-files --deleted)) | grep -E '((\.clj-kondo\/status-im)|(src).*\.clj[sc]?$$)|((\.clj-kondo\/(status-im|config))|(src).*\.edn$$)|shadow-cljs\.edn')
endef

lint: export TARGET := clojure
lint: export CLJ_LINTER_PRINT_WARNINGS ?= false
lint: ##@test Run code style checks
	@sh scripts/lint/re-frame-in-quo-components.sh && \
	sh scripts/lint/direct-require-component-outside-quo.sh && \
	sh scripts/lint/require-i18n-resource-first.sh && \
	clj-kondo --config .clj-kondo/config.edn --cache false --fail-level error --lint src $(if $(filter $(CLJ_LINTER_PRINT_WARNINGS),true),,| grep -v ': warning: ') && \
	ALL_CLOJURE_FILES=$(call find_all_clojure_files) && \
	scripts/lint/translations.clj && \
	zprint '{:search-config? true}' -sfc $$ALL_CLOJURE_FILES && \
	sh scripts/lint/trailing-newline.sh && \
	node_modules/.bin/prettier --check .

# NOTE: We run the linter twice because of https://github.com/kkinnear/zprint/issues/271
lint-fix: export TARGET := clojure
lint-fix: ##@test Run code style checks and fix issues
	ALL_CLOJURE_FILES=$(call find_all_clojure_files) && \
	zprint '{:search-config? true}' -sw $$ALL_CLOJURE_FILES && \
	zprint '{:search-config? true}' -sw $$ALL_CLOJURE_FILES && \
	clojure-lsp --ns-exclude-regex ".*/\.clj-kondo/.*|.*/src/status_im/core\.cljs|.*/src/test_helpers/component_tests_preload\.cljs$$" clean-ns && \
	sh scripts/lint/trailing-newline.sh --fix && \
	node_modules/.bin/prettier --write .

shadow-server: export TARGET := clojure
shadow-server:##@ Start shadow-cljs in server mode for watching
	yarn shadow-cljs server

_test-clojure: export TARGET := clojure
_test-clojure: export WATCH ?= false
_test-clojure: status-go-library
_test-clojure:
ifeq ($(WATCH), true)
	yarn node-pre-gyp rebuild && \
	yarn shadow-cljs compile mocks && \
	nodemon --exec "yarn shadow-cljs compile test && node --require ./test-resources/override.js $$SHADOW_OUTPUT_TO" -e cljs
else
	yarn node-pre-gyp rebuild && \
	yarn shadow-cljs compile mocks && \
	yarn shadow-cljs compile test && \
	node --require ./test-resources/override.js "$$SHADOW_OUTPUT_TO"
endif

test: export SHADOW_OUTPUT_TO := target/test/test.js
test: export SHADOW_NS_REGEXP := .*-test$$
test: ##@test Run all Clojure tests
test: _test-clojure

# Note: we need to override the :output-to and :ns-regexp options because
# shadow-cljs has a bug where it will not read from the env vars to expand the
# configuration when the shadow-cljs mobile target is already running.
test-watch-for-repl: export TARGET := default
test-watch-for-repl: export SHADOW_OUTPUT_TO := target/test/test.js
test-watch-for-repl: export SHADOW_NS_REGEXP := .*-test$$
test-watch-for-repl: ##@test Watch all Clojure tests and support REPL connections
	rm -f "$$SHADOW_OUTPUT_TO" && \
	yarn install && shadow-cljs compile mocks && \
	concurrently --kill-others --prefix-colors 'auto' --names 'build,repl' \
		"yarn shadow-cljs watch test --verbose --config-merge '{:output-to \"$(SHADOW_OUTPUT_TO)\" :ns-regexp \"$(SHADOW_NS_REGEXP)\"}'" \
		"until [ -f $$SHADOW_OUTPUT_TO ] ; do sleep 1 ; done ; node --require ./test-resources/override.js $$SHADOW_OUTPUT_TO --repl"

test-unit: export SHADOW_OUTPUT_TO := target/unit_test/test.js
test-unit: export SHADOW_NS_REGEXP := ^(?!tests\.integration-test)(?!tests\.contract-test).*-test$$
test-unit: ##@test Run unit tests
test-unit: _test-clojure

test-integration: export SHADOW_OUTPUT_TO := target/integration_test/test.js
test-integration: export SHADOW_NS_REGEXP := ^tests\.integration-test.*$$
test-integration: ##@test Run integration tests
test-integration: _test-clojure

test-contract: export SHADOW_OUTPUT_TO := target/contract_test/test.js
test-contract: export SHADOW_NS_REGEXP := ^tests\.contract-test.*$$
test-contract: ##@test Run contract tests
test-contract: _test-clojure

test-android: jsbundle
test-android: export TARGET := android
test-android: ##@test Android Gradle test
	cd android && ./gradlew test

test-component-watch: export TARGET := clojure
test-component-watch: export COMPONENT_TEST := true
test-component-watch: export BABEL_ENV := test
test-component-watch: export JEST_USE_SILENT_REPORTER := false
test-component-watch: ##@ Watch tests and re-run no changes to cljs files
	@scripts/check-metro-shadow-process.sh
	rm -rf ./component-spec
	nodemon --exec 'yarn shadow-cljs compile component-test && jest --config=test/jest/jest.config.js --testEnvironment node ' -e cljs

test-component: export TARGET := clojure
test-component: export COMPONENT_TEST := true
test-component: export BABEL_ENV := test
test-component: export JEST_USE_SILENT_REPORTER := false
test-component: ##@test Run component tests once in NodeJS
	@scripts/check-metro-shadow-process.sh
	rm -rf ./component-spec
	yarn shadow-cljs compile component-test && \
	jest --clearCache && jest --config=test/jest/jest.config.js --testEnvironment node

# Reference: https://flow-storm.github.io/flow-storm-debugger/user_guide.html#_debugging_react_native_applications
run-flow-storm: export SHADOW_CLJS_BUILD_ID := :mobile
run-flow-storm: export TARGET := clojure
run-flow-storm: export GDK_DPI_SCALE := 1.0
run-flow-storm: ##@run Start FlowStorm debugger
	clj -Sforce -Sdeps '{:deps {com.github.jpmonettas/flow-storm-dbg {:mvn/version "3.7.5"}}}' \
		-X flow-storm.debugger.main/start-debugger \
		:port 7888 \
		:repl-type :shadow \
		:build-id $(SHADOW_CLJS_BUILD_ID)

#--------------
# Other
#--------------

geth-connect: export TARGET := android-sdk
geth-connect: ##@other Connect to Geth on the device
	adb forward tcp:8545 tcp:8545 && \
	build/bin/geth attach http://localhost:8545

ios-clean: SHELL := /bin/sh
ios-clean: ios-simulator-cache-clean
ios-clean: ##@prepare Clean iOS build artifacts
	git clean -dxf -f target/ios

android-clean: SHELL := /bin/sh
android-clean: ##@prepare Clean Gradle state
	git clean -dxf -f ./android/app/build; \
	rm -rf android/.gradle \
	rm -rf android/build \
	rm -rf ~/.gradle


android-ports: export FLOWSTORM_PORT ?= 7722
android-ports: export TARGET := android-sdk
android-ports: export RCT_METRO_PORT ?= 8081
android-ports: ##@other Add proxies to Android Device/Simulator
	adb reverse tcp:$(RCT_METRO_PORT) tcp:$(RCT_METRO_PORT) && \
	adb reverse tcp:3449 tcp:3449 && \
	adb reverse tcp:4567 tcp:4567 && \
	adb reverse tcp:$(FLOWSTORM_PORT) tcp:$(FLOWSTORM_PORT) && \
	adb forward tcp:5561 tcp:5561

android-devices: export TARGET := android-sdk
android-devices: ##@other Invoke adb devices
	adb devices

android-pull-geth: export TARGET := android-sdk
android-pull-geth: export VERSION ?= debug
android-pull-geth:
	adb pull "/storage/emulated/0/Android/data/im.status.ethereum$$( [ "$(VERSION)" = "release" ] || echo ".$(VERSION)" )/files/Download/geth.log"

android-tail-geth: export TARGET := android-sdk
android-tail-geth: export VERSION ?= debug
android-tail-geth:
	adb shell 'while true; do cat; sleep 1; done < /storage/emulated/0/Android/data/im.status.ethereum$$( [ "$(VERSION)" = "release" ] || echo ".$(VERSION)" )/files/Download/geth.log'

android-clean-geth: export TARGET := android-sdk
android-clean-geth: export VERSION ?= debug
android-clean-geth:
	adb shell 'rm /storage/emulated/0/Android/data/im.status.ethereum$$( [ "$(VERSION)" = "release" ] || echo ".$(VERSION)" )/files/Download/geth.log'


android-logcat: export TARGET := android-sdk
android-logcat: ##@other Read status-mobile logs from Android phone using adb
	adb logcat | grep -e RNBootstrap -e ReactNativeJS -e ReactNative -e StatusModule -e StatusNativeLogs -e 'F DEBUG   :' -e 'Go      :' -e 'GoLog   :' -e 'libc    :'

android-install: export TARGET := android-sdk
android-install: export BUILD_TYPE ?= release
android-install: ##@other Install APK on device using adb
	adb install result/app-$(BUILD_TYPE).apk

generate-autolink-android: export TARGET := clojure
generate-autolink-android: ##@other Generate autolinking.json
	@scripts/generate_autolink_android.sh

_list: SHELL := /bin/sh
_list:
	@$(MAKE) -pRrq -f $(lastword $(MAKEFILE_LIST)) : 2>/dev/null | awk -v RS= -F: '/^# File/,/^# Finished Make data base/ {if ($$1 !~ "^[#.]") {print $$1}}' | sort | egrep -v -e '^[^[:alnum:]]' -e '^$@$$'

patch-file: export TARGET := default
patch-file: ##@other Generates patch file for npm deps
	@scripts/patch-npm-lib.sh

#--------------
# REPLs
#--------------

repl-clojure: export TARGET := clojure
repl-clojure: ##@repl Start Clojure repl for mobile App
	yarn shadow-cljs cljs-repl mobile

repl-nix: nix-repl ##@repl Start an interactive Nix REPL

#--------------
# Dev Automation Flows
#--------------

auto-login: export TARGET := default
auto-login: ##@auto runs flow for login or onboarding app on simulator/emulator
	maestro test "maestro/create-account-or-login.yaml"

auto-custom: export TARGET := default
auto-custom: ##@auto runs any custom maestro automation flow on simulator/emulator
ifndef FLOW
	$(error Usage: make automate FLOW=your-maestro-flow-file.yaml)
endif
	maestro test "$(FLOW)"
