__toolversion = $(shell $(GIT_ROOT)/scripts/toolversion $(1))

GIT_COMMIT = $(shell git rev-parse --short HEAD)

# WARNING: Change version in Dockerfile too
QT_VERSION = 5.11.2
QT_VER_MJR = 5.11
QT_ARCHIVE = qt-opensource-linux-x64-$(QT_VERSION).run
QT_MD5SUM  = 974fda61267cfb6e45984ee5f0a285f8
QT_URL = https://download.qt.io/archive/qt
QT_CI_COMMIT_SHA = 55ffd9f225708b3aa1443851cfa8dead2c1f9959
REACT_NATIVE_DESKTOP_COMMIT_SHA = 2a76c435a27f0bf6c089f74222212b3a58e10385

# WARNING: Remember to change the tag when updating the image
IMAGE_TAG = 1.1.1
IMAGE_NAME = statusteam/linux-desktop-ubuntu:$(IMAGE_TAG)

build: $(QT_ARCHIVE)
	rm -rf ./scripts && \
	mkdir -p ./scripts/3rd-party && \
	cp -R ../../scripts/3rd-party/ ./scripts/ && \
	docker build \
		--build-arg="QT_VERSION=$(QT_VERSION)" \
		--build-arg="QT_CI_COMMIT_SHA=$(QT_CI_COMMIT_SHA)" \
		--build-arg="REACT_NATIVE_DESKTOP_COMMIT_SHA=$(REACT_NATIVE_DESKTOP_COMMIT_SHA)" \
		--build-arg="LEIN_VERSION=$(call __toolversion,lein)" \
		--build-arg="NODE_VERSION=$(call __toolversion,node)" \
		--build-arg="NVM_VERSION=$(call __toolversion,nvm)" \
		--build-arg="YARN_VERSION=$(call __toolversion,yarn)" \
		--label="commit=$(GIT_COMMIT)" \
		-t $(IMAGE_NAME) .; \
	rm -rf ./scripts

$(QT_ARCHIVE):
	wget $(QT_URL)/$(QT_VER_MJR)/$(QT_VERSION)/$(QT_ARCHIVE)
	echo "$(QT_MD5SUM)  $(QT_ARCHIVE)" | md5sum --check

push: build
	docker push $(IMAGE_NAME)
