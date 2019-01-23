__toolversion = $(shell $(GIT_ROOT)/scripts/toolversion $(1))

GIT_COMMIT = $(shell git rev-parse --short HEAD)
GIT_ROOT = $(shell git rev-parse --show-toplevel)

QT_VERSION = $(call __toolversion, qt)
QT_MD5SUM  = $(call __toolversion, qt_md5)
QT_ARCHIVE = qt-opensource-linux-x64-$(QT_VERSION).run
QT_URL = https://download.qt.io/archive/qt

# WARNING: Remember to update `ci/Jenkinsfile.*` with the same IMAGE_TAG value
BASE_IMAGE_TAG = $(shell cd ../base && make get-image-tag)
DEPS_HASH = $(shell $(GIT_ROOT)/scripts/gen-deps-hash.sh -b $(BASE_IMAGE_TAG) -d cmake -d node -d qt -d qtci -d react_native_desktop)
IMAGE_TAG = 1.1.0-$(DEPS_HASH)
IMAGE_NAME = statusteam/status-build-linux:$(IMAGE_TAG)

build: $(QT_ARCHIVE)
	@ if [ "${DEPS_HASH}" = "" ]; then \
		echo "DEPS_HASH not set"; \
		exit 1; \
	fi

	docker build \
		--build-arg="BASE_IMAGE_TAG=$(BASE_IMAGE_TAG)" \
		--build-arg="NODE_VERSION=$(call __toolversion, node)" \
		--build-arg="QT_VERSION=$(QT_VERSION)" \
		--build-arg="QT_CI_COMMIT=$(call __toolversion, qtci)" \
		--build-arg="RNATIVE_VERSION=$(call __toolversion, react_native_desktop)" \
		--build-arg="CMAKE_VERSION=$(call __toolversion, cmake)" \
		--label="commit=$(GIT_COMMIT)" \
		-t $(IMAGE_NAME) .

$(QT_ARCHIVE):
	wget $(QT_URL)/$(call __major_version__, $(QT_VERSION))/$(QT_VERSION)/$(QT_ARCHIVE)
	echo "$(QT_MD5SUM)  $(QT_ARCHIVE)" | md5sum --check

push: build
	docker push $(IMAGE_NAME)
