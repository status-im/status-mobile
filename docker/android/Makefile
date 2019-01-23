__toolversion = $(shell $(GIT_ROOT)/scripts/toolversion $(1))

GIT_COMMIT = $(shell git rev-parse --short HEAD)
GIT_ROOT = $(shell git rev-parse --show-toplevel)

ANDROID_NDK_VERSION = $(call  __toolversion, android-ndk)
ANDROID_NDK_CHECKSUM = $(call  __toolversion, android-ndk_md5)
ANDROID_NDK_ARCHIVE = android-ndk-$(ANDROID_NDK_VERSION)-linux-x86_64.zip
ANDROID_NDK_URL = https://dl.google.com/android/repository/$(ANDROID_NDK_ARCHIVE)

ANDROID_SDK_VERSION = $(call  __toolversion, android-sdk)
ANDROID_SDK_CHECKSUM = $(call  __toolversion, android-sdk_md5)
ANDROID_SDK_URL = https://dl.google.com/android/repository/sdk-tools-linux-$(ANDROID_SDK_VERSION).zip
ANDROID_SDK_ARCHIVE = sdk-tools-linux-$(ANDROID_SDK_VERSION).zip

SDK_PLATFORM_VERSION=$(call  __toolversion, android-sdk-platform)
SDK_BUILD_TOOLS_VERSION=$(call  __toolversion, android-sdk-build-tools)

# WARNING: Remember to change the tag when updating the image
BASE_IMAGE_TAG = $(shell cd ../base && make get-image-tag)
DEPS_HASH = $(shell $(GIT_ROOT)/scripts/gen-deps-hash.sh -b $(BASE_IMAGE_TAG) -d android-ndk -d android-sdk -d android-sdk-platform -d android-sdk-build-tools)
IMAGE_TAG = 1.1.0-$(DEPS_HASH)
IMAGE_NAME = statusteam/status-build-android:$(IMAGE_TAG)

build: $(ANDROID_NDK_ARCHIVE) $(ANDROID_SDK_ARCHIVE)
	docker build \
        --build-arg="BASE_IMAGE_TAG=$(BASE_IMAGE_TAG)" \
        --build-arg="ANDROID_NDK_VERSION=$(ANDROID_NDK_VERSION)" \
        --build-arg="ANDROID_SDK_VERSION=$(ANDROID_SDK_VERSION)" \
        --build-arg="SDK_PLATFORM_VERSION=$(SDK_PLATFORM_VERSION)" \
        --build-arg="SDK_BUILD_TOOLS_VERSION=$(SDK_BUILD_TOOLS_VERSION)" \
        --label="commit=$(GIT_COMMIT)" \
        -t $(IMAGE_NAME) .

$(ANDROID_NDK_ARCHIVE):
	wget -q "$(ANDROID_NDK_URL)" -O "$(ANDROID_NDK_ARCHIVE)"
	echo "$(ANDROID_NDK_CHECKSUM)  $(ANDROID_NDK_ARCHIVE)" | md5sum --check

$(ANDROID_SDK_ARCHIVE):
	wget -q "$(ANDROID_SDK_URL)" -O "$(ANDROID_SDK_ARCHIVE)"
	echo "$(ANDROID_SDK_CHECKSUM)  $(ANDROID_SDK_ARCHIVE)" | md5sum --check

test: ## Run build inside the image as a test
	docker run -u $(shell id -u):$(shell id -g) \
        --name android-test --rm \
        --tmpfs /var/tmp:rw,size=1G,exec,mode=1777 \
        -v $(GIT_ROOT):/repo:rw \
        -w /repo $(IMAGE_NAME) \
        docker/android/build.sh

push: build
	docker push $(IMAGE_NAME)
