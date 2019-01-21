__toolversion = $(shell $(GIT_ROOT)/scripts/toolversion $(1))

GIT_COMMIT = $(shell git rev-parse --short HEAD)
GIT_ROOT = $(shell git rev-parse --show-toplevel)

# WARNING: Remember to change the tag when updating the image
IMAGE_TAG = 1.1.1
IMAGE_NAME = statusteam/windows-desktop-ubuntu:$(IMAGE_TAG)

REACT_NATIVE_DESKTOP_COMMIT_SHA = 2a76c435a27f0bf6c089f74222212b3a58e10385

build:
	rm -rf ./scripts && \
	mkdir -p ./scripts/3rd-party && \
	cp -R ../../scripts/3rd-party/ ./scripts/ && \
	docker build \
		--build-arg="REACT_NATIVE_DESKTOP_COMMIT_SHA=$(REACT_NATIVE_DESKTOP_COMMIT_SHA)" \
		--build-arg="CONAN_VERSION=$(call __toolversion,conan)" \
		--build-arg="LEIN_VERSION=$(call __toolversion,lein)" \
		--build-arg="NODE_VERSION=$(call __toolversion,node)" \
		--build-arg="NVM_VERSION=$(call __toolversion,nvm)" \
		--label="commit=$(GIT_COMMIT)" \
		-t $(IMAGE_NAME) .; \
	rm -rf ./scripts

push: build
	docker push $(IMAGE_NAME)
