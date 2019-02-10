__toolversion = $(shell $(GIT_ROOT)/scripts/toolversion $(1))

GIT_COMMIT = $(shell git rev-parse --short HEAD)
GIT_ROOT = $(shell git rev-parse --show-toplevel)

# WARNING: Remember to change the tag when updating the image
DEPS_HASH = $(shell $(GIT_ROOT)/scripts/gen-deps-hash.sh -d leiningen -d node -d yarn)
IMAGE_TAG = 1.1.0-$(DEPS_HASH)
IMAGE_NAME = statusteam/status-build-base:$(IMAGE_TAG)

get-image-tag:
	@echo $(IMAGE_TAG)

build: nvm_install.sh
	docker build \
		--build-arg="LEIN_VERSION=$(call __toolversion, leiningen)" \
		--build-arg="NODE_VERSION=$(call __toolversion, node)" \
		--build-arg="YARN_VERSION=$(call __toolversion, yarn)" \
		--label="commit=$(GIT_COMMIT)" \
		-t $(IMAGE_NAME) .

nvm_install.sh:
	cp $(GIT_ROOT)/scripts/3rd-party/nvm/$(call  __toolversion, nvm)/install.sh nvm_install.sh

push: build
	docker push $(IMAGE_NAME)
