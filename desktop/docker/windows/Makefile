GIT_COMMIT = $(shell git rev-parse --short HEAD)

IMAGE_TAG = 1.0.0
IMAGE_NAME = statusteam/windows-desktop-ubuntu:$(IMAGE_TAG)

build:
	docker build \
		--label="commit=$(GIT_COMMIT)" \
		-t $(IMAGE_NAME) .

push: build
	docker push $(IMAGE_NAME)
