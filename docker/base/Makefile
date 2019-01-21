GIT_COMMIT = $(shell git rev-parse --short HEAD)
GIT_ROOT = $(shell git rev-parse --show-toplevel)

# Software Versions, URLs, and Checksums
NVM_VERSION = v0.33.11
NVM_INSTALL_SCRIPT = nvm_$(NVM_VERSION)_install.sh
NVM_NODE_VERSION = v10.14.0
LEIN_VERSION = stable

# WARNING: Remember to change the tag when updating the image
IMAGE_TAG = 1.0.0
IMAGE_NAME = statusteam/status-build-base:$(IMAGE_TAG)

build:
	docker build \
		--build-arg="LEIN_VERSION=$(LEIN_VERSION)" \
		--build-arg="NVM_VERSION=$(NVM_VERSION)" \
		--build-arg="NVM_NODE_VERSION=$(NVM_NODE_VERSION)" \
		--label="commit=$(GIT_COMMIT)" \
		-t $(IMAGE_NAME) .

push: build
	docker push $(IMAGE_NAME)
