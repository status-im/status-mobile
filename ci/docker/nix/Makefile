__toolversion = $(shell $(GIT_ROOT)/scripts/toolversion $(1))

GIT_COMMIT = $(shell git rev-parse --short HEAD)
GIT_ROOT = $(shell git rev-parse --show-toplevel)

ANDROID_SDK_VERSION = $(call  __toolversion, android-sdk)
ANDROID_SDK_CHECKSUM = $(call  __toolversion, -c android-sdk)
ANDROID_SDK_URL = https://dl.google.com/android/repository/sdk-tools-linux-$(ANDROID_SDK_VERSION).zip
ANDROID_SDK_ARCHIVE = sdk-tools-linux-$(ANDROID_SDK_VERSION).zip
SDK_PLATFORM_VERSION=$(call  __toolversion, android-sdk-platform)
SDK_BUILD_TOOLS_VERSION=$(call  __toolversion, android-sdk-build-tools)

JENKINS_UID = 1001
JENKINS_GID = 1001

NIX_VERSION = 2.2.1
NIX_INSTALL = nix-install-$(NIX_VERSION).sh
NIX_SHA = 0039df605d2324f721f0c99ab76fc58fa60f791d84b30196eed805e8e65b1e51
NIX_URL = https://nixos.org/releases/nix/nix-$(NIX_VERSION)/install

# WARNING: Remember to change the tag when updating the image
DEPS_HASH = $(shell $(GIT_ROOT)/scripts/gen-deps-hash.sh -b $(NIX_SHA) \
					-d android-sdk \
					-d android-sdk-platform \
					-d android-sdk-build-tools \
					-d nix \
					-d StatusImAppImage \
					-d StatusIm-Windows-base-image)

IMAGE_TAG = jenkins-1.0.0-$(DEPS_HASH)
IMAGE_NAME = statusteam/nix:$(IMAGE_TAG)

build: $(NIX_INSTALL) $(ANDROID_SDK_ARCHIVE)
	docker build \
		--build-arg="JENKINS_UID=$(JENKINS_UID)" \
		--build-arg="JENKINS_GID=$(JENKINS_GID)" \
		--build-arg="NIX_VERSION=$(NIX_VERSION)" \
		--build-arg="ANDROID_SDK_VERSION=$(ANDROID_SDK_VERSION)" \
		--build-arg="SDK_BUILD_TOOLS_VERSION=$(SDK_BUILD_TOOLS_VERSION)" \
		--build-arg="SDK_PLATFORM_VERSION=$(SDK_PLATFORM_VERSION)" \
		--build-arg="DEPS_HASH=$(DEPS_HASH)" \
		-t $(IMAGE_NAME) .

$(NIX_INSTALL):
	wget -q "$(NIX_URL)" -O "$(NIX_INSTALL)"
	echo "$(NIX_SHA)  $(NIX_INSTALL)" | sha256sum --check
	chmod +x "$(NIX_INSTALL)"

$(ANDROID_SDK_ARCHIVE):
	wget -q "$(ANDROID_SDK_URL)" -O "$(ANDROID_SDK_ARCHIVE)"
	echo "$(ANDROID_SDK_CHECKSUM)  $(ANDROID_SDK_ARCHIVE)" | md5sum --check

push: build
	docker push $(IMAGE_NAME)
