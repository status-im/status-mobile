GIT_COMMIT = $(shell git rev-parse --short HEAD)

# WARNING: Remember to change the tag when updating the image
IMAGE_TAG = 1.1.1
IMAGE_NAME = statusteam/windows-desktop-ubuntu:$(IMAGE_TAG)

REACT_NATIVE_DESKTOP_COMMIT_SHA = 2a76c435a27f0bf6c089f74222212b3a58e10385

build:
	docker build \
		--build-arg="REACT_NATIVE_DESKTOP_COMMIT_SHA=$(REACT_NATIVE_DESKTOP_COMMIT_SHA)" \
		--label="commit=$(GIT_COMMIT)" \
		-t $(IMAGE_NAME) .

push: build
	docker push $(IMAGE_NAME)
