__toolversion = $(shell $(GIT_ROOT)/scripts/toolversion $(1))

GIT_COMMIT = $(shell git rev-parse --short HEAD)
GIT_ROOT = $(shell git rev-parse --show-toplevel)

# WARNING: Remember to update `ci/Jenkinsfile.*` with the same IMAGE_TAG value
BASE_IMAGE_TAG = $(shell cd $(GIT_ROOT)/docker/base && make get-image-tag)
DEPS_HASH = $(shell $(GIT_ROOT)/scripts/gen-deps-hash.sh -b $(BASE_IMAGE_TAG) -d cmake -d conan -d golang -d node -d react_native_desktop -d windows_base_image)
IMAGE_TAG = 1.1.1-$(DEPS_HASH)
IMAGE_NAME = statusteam/status-build-windows:$(IMAGE_TAG)

WIN_BASE_IMAGE_VER = $(call __toolversion, windows_base_image)
WIN_BASE_IMAGE_DM5 = $(call __toolversion, -c windows_base_image)
WIN_BASE_IMAGE_ZIP = StatusIm-Windows-base-image_$(WIN_BASE_IMAGE_VER).zip
WIN_BASE_IMAGE_URL = https://desktop-app-files.ams3.digitaloceanspaces.com/$(WIN_BASE_IMAGE_ZIP)

build: $(WIN_BASE_IMAGE_ZIP)
	@ if [ "${DEPS_HASH}" = "" ]; then \
		echo "DEPS_HASH not set"; \
		exit 1; \
	fi

	docker build \
		--build-arg="BASE_IMAGE_TAG=$(BASE_IMAGE_TAG)" \
		--build-arg="CONAN_VERSION=$(call __toolversion, conan)" \
		--build-arg="CMAKE_VERSION=$(call __toolversion, cmake)" \
		--build-arg="GOLANG_VERSION=$(call __toolversion, golang)" \
		--build-arg="NODE_VERSION=$(call __toolversion, node)" \
		--build-arg="RNATIVE_VERSION=$(call __toolversion, react_native_desktop)" \
		--build-arg="WIN_BASE_IMAGE_VERSION=$(WIN_BASE_IMAGE_VER)" \
		--label="commit=$(GIT_COMMIT)" \
		-t $(IMAGE_NAME) .

$(WIN_BASE_IMAGE_ZIP):
	wget -q "$(WIN_BASE_IMAGE_URL)" -O "$(WIN_BASE_IMAGE_ZIP)"
	echo "$(WIN_BASE_IMAGE_DM5)  $(WIN_BASE_IMAGE_ZIP)" | md5sum --check

push: build
	docker push $(IMAGE_NAME)
