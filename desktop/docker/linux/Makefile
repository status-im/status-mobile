GIT_COMMIT = $(shell git rev-parse --short HEAD)

# WARNING: Change version in Dockerfile too
QT_VERSION = 5.11.2
QT_VER_MJR = 5.11
QT_ARCHIVE = qt-opensource-linux-x64-$(QT_VERSION).run
QT_MD5SUM  = 974fda61267cfb6e45984ee5f0a285f8
QT_URL = https://download.qt.io/archive/qt

# WARNING: Remember to change the tag when updating the image
IMAGE_TAG = 1.1.0
IMAGE_NAME = statusteam/linux-desktop-ubuntu:$(IMAGE_TAG)

build: $(QT_ARCHIVE)
	docker build \
		--build-arg="QT_VERSION=$(QT_VERSION)" \
		--label="commit=$(GIT_COMMIT)" \
		-t $(IMAGE_NAME) .

$(QT_ARCHIVE):
	wget $(QT_URL)/$(QT_VER_MJR)/$(QT_VERSION)/$(QT_ARCHIVE)
	echo "$(QT_MD5SUM)  $(QT_ARCHIVE)" | md5sum --check

push: build
	docker push $(IMAGE_NAME)
