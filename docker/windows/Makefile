__toolversion = $(shell $(GIT_ROOT)/scripts/toolversion $(1))

GIT_COMMIT = $(shell git rev-parse --short HEAD)
GIT_ROOT = $(shell git rev-parse --show-toplevel)

# WARNING: Remember to change the tag when updating the image
IMAGE_TAG = 1.1.0
IMAGE_NAME = statusteam/status-build-windows:$(IMAGE_TAG)

build:
	docker build \
		--build-arg="RNATIVE_VERSION=$(call __toolversion, react_native_desktop)" \
		--build-arg="CONAN_VERSION=$(call __toolversion, conan)" \
		--build-arg="CMAKE_VERSION=$(call __toolversion, cmake)" \
		--label="commit=$(GIT_COMMIT)" \
		-t $(IMAGE_NAME) .; \

push: build
	docker push $(IMAGE_NAME)
