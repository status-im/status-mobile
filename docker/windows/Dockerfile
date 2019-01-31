ARG BASE_IMAGE_TAG

FROM statusteam/status-build-base:${BASE_IMAGE_TAG}

ARG RNATIVE_VERSION
ARG CONAN_VERSION
ARG CMAKE_VERSION
ARG GOLANG_VERSION
ARG NODE_VERSION
ENV PATH=/home/jenkins/.nvm/versions/node/v${NODE_VERSION}/bin:/usr/local/go/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

RUN add-apt-repository -y ppa:git-core/ppa \
 && apt-get update \
 && DEBIAN_FRONTEND=noninteractive \
      apt-get -q -y --no-install-recommends install \
              nsis fuse python python3-pip python3-setuptools python3-wheel \
              apt-transport-https openjdk-8-jdk-headless extra-cmake-modules build-essential \
              libx11-xcb1 libxss1 libasound2 libgl-dev libsm6 libxrandr2 python-dev \
              libjasper-dev libegl1-mesa libxcomposite-dev libxcursor-dev \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* /usr/share/man \
 && python3 -m pip install --upgrade pip \
 && pip3 install conan==${CONAN_VERSION}

RUN cd /tmp \
 && wget -q https://cmake.org/files/v${CMAKE_VERSION%.*}/cmake-${CMAKE_VERSION}-Linux-x86_64.tar.gz \
 && tar xf cmake-${CMAKE_VERSION}-Linux-x86_64.tar.gz --strip 1 -C /usr/local \
 && rm -fr /usr/local/doc/cmake \
 && rm cmake-${CMAKE_VERSION}-Linux-x86_64.tar.gz

RUN cd /tmp \
 && wget -q https://dl.google.com/go/go${GOLANG_VERSION}.linux-amd64.tar.gz \
 && tar -C /usr/local -xzf go${GOLANG_VERSION}.linux-amd64.tar.gz \
 && rm go${GOLANG_VERSION}.linux-amd64.tar.gz

RUN mkdir -p /tmp/react-native-desktop \
 && cd /tmp/react-native-desktop \
 && git --version \
 && git init \
 && git remote add origin https://github.com/status-im/react-native-desktop.git \
 && git fetch --depth=1 origin ${RNATIVE_VERSION} \
 && git reset --hard FETCH_HEAD \
 && cp -r /tmp/react-native-desktop/react-native-cli /opt \
 && cd /opt/react-native-cli \
 && npm update \
 && npm install -g --prefix /usr/local \
 && cd \
 && rm -r /tmp/react-native-desktop

LABEL source="https://github.com/status-im/status-react/tree/develop/docker/windows" \
      description="Image for building Windows Desktop version of Status app." \
      maintainer="jakub@status.im"
