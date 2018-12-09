GIT_COMMIT = $(shell git rev-parse --short HEAD)
GIT_ROOT = $(shell git rev-parse --show-toplevel)

ANDROID_NDK_VERSION = r10e
ANDROID_NDK_CHECKSUM = 070be287539e3e7706f8dabfb6bf9879
ANDROID_NDK_ARCHIVE = android-ndk-$(ANDROID_NDK_VERSION)-linux-x86_64.zip
ANDROID_NDK_URL = https://dl.google.com/android/repository/$(ANDROID_NDK_ARCHIVE)

ANDROID_SDK_VERSION = 4333796
ANDROID_SDK_CHECKSUM = aa190cfd7299cd6a1c687355bb2764e4
ANDROID_SDK_URL = https://dl.google.com/android/repository/sdk-tools-linux-$(ANDROID_SDK_VERSION).zip
ANDROID_SDK_ARCHIVE = sdk-tools-linux-$(ANDROID_SDK_VERSION).zip

# WARNING: Remember to change the tag when updating the image
IMAGE_TAG = 1.0.0
IMAGE_NAME = statusteam/status-build-android:$(IMAGE_TAG)

build: $(ANDROID_NDK_ARCHIVE) $(ANDROID_SDK_ARCHIVE)
	docker build \
		--build-arg="ANDROID_NDK_VERSION=$(ANDROID_NDK_VERSION)" \
		--build-arg="ANDROID_SDK_VERSION=$(ANDROID_SDK_VERSION)" \
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
