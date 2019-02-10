__toolversion = $(shell $(GIT_ROOT)/scripts/toolversion $(1))

GIT_COMMIT = $(shell git rev-parse --short HEAD)
GIT_ROOT = $(shell git rev-parse --show-toplevel)

# WARNING: Remember to update `ci/Jenkinsfile.*` with the same IMAGE_TAG value
BASE_IMAGE_TAG = $(shell cd $(GIT_ROOT)/docker/base && make get-image-tag)
DEPS_HASH = $(shell $(GIT_ROOT)/scripts/gen-deps-hash.sh -b $(BASE_IMAGE_TAG) -d cmake -d conan -d golang -d node -d react_native_desktop)
IMAGE_TAG = 1.1.0-$(DEPS_HASH)
IMAGE_NAME = statusteam/status-build-windows:$(IMAGE_TAG)

build:
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
		--label="commit=$(GIT_COMMIT)" \
		-t $(IMAGE_NAME) .

push: build
	docker push $(IMAGE_NAME)
