GIT_COMMIT = $(shell git rev-parse --short HEAD)

IMAGE_NAME = statusteam/linux-desktop-ubuntu:16.04
QT_MD5SUM = b8dd904894ac6b09076b5f61d8b9d71a
QT_ARCHIVE = qt-opensource-linux-x64-5.9.1.run

build: $(QT_ARCHIVE)
	docker build \
		--label "commit=$(GIT_COMMIT)" \
		-t $(IMAGE_NAME) .

$(QT_ARCHIVE):
	wget https://download.qt.io/archive/qt/5.9/5.9.1/$(QT_ARCHIVE)
	echo "$(QT_MD5SUM)  $(QT_ARCHIVE)" | md5sum --check

push: build
	docker push $(IMAGE_NAME)
