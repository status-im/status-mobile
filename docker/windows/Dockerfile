FROM ubuntu:16.04

ARG REACT_NATIVE_DESKTOP_COMMIT_SHA

ENV LANG en_US.UTF-8
ENV LC_ALL en_US.UTF-8
ENV LANGUAGE en_US.UTF-8
ENV LEIN_HOME /var/tmp/lein
ENV NPM_CONFIG_CACHE /var/tmp/npm
# We have to do this because Jenkins doesn't let us
# https://issues.jenkins-ci.org/browse/JENKINS-49076
ENV PATH /usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

ARG CONAN_VERSION

RUN apt-get update && apt-get -q -y --no-install-recommends install curl software-properties-common && \
    add-apt-repository -y ppa:git-core/ppa && \
    add-apt-repository -y ppa:longsleep/golang-backports && \
    apt-get remove -y software-properties-common && \
    apt-get update && \
    DEBIAN_FRONTEND=noninteractive \
      apt-get -q -y --no-install-recommends install \
      wget git nsis unzip golang-go file jq s3cmd \
      python python3-pip python3-setuptools python3-wheel \
      apt-transport-https locales openjdk-8-jdk-headless \
      extra-cmake-modules build-essential fuse \
      libx11-xcb1 libxss1 libasound2 libgl-dev libsm6 libxrandr2 python-dev \
      libjasper-dev libegl1-mesa libxcomposite-dev libxcursor-dev && \
    locale-gen en_US.UTF-8 && \
    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* /usr/share/man && \
    python3 -m pip install --upgrade pip && \
    pip3 install conan==${CONAN_VERSION}

RUN cd /tmp && wget -q https://cmake.org/files/v3.12/cmake-3.12.2-Linux-x86_64.tar.gz && \
    tar xf cmake-3.12.2-Linux-x86_64.tar.gz --strip 1 -C /usr/local && \
    rm -fr /usr/local/doc/cmake && rm cmake-3.12.2-Linux-x86_64.tar.gz

ARG LEIN_VERSION
ENV LEIN_INSTALL=/usr/bin/

RUN mkdir -p $LEIN_INSTALL \
  && wget -q https://raw.githubusercontent.com/technomancy/leiningen/${LEIN_VERSION}/bin/lein-pkg \
  && mv lein-pkg $LEIN_INSTALL/lein \
  && chmod 0755 $LEIN_INSTALL/lein \
  && wget -q https://github.com/technomancy/leiningen/releases/download/${LEIN_VERSION}/leiningen-${LEIN_VERSION}-standalone.zip \
  && mkdir -p /usr/share/java \
  && mv leiningen-${LEIN_VERSION}-standalone.zip /usr/share/java/leiningen-${LEIN_VERSION}-standalone.jar \
  && $LEIN_INSTALL/lein version

ARG NVM_VERSION
ARG NODE_VERSION
ADD scripts/3rd-party/nvm/${NVM_VERSION}/install.sh /tmp/nvm/${NVM_VERSION}/

RUN /tmp/nvm/${NVM_VERSION}/install.sh && \
    mkdir -p /tmp/react-native-desktop && cd /tmp/react-native-desktop && \
      git --version && \
      git init && git remote add origin https://github.com/status-im/react-native-desktop.git && \
      git fetch --depth=1 origin ${REACT_NATIVE_DESKTOP_COMMIT_SHA} && \
      git reset --hard FETCH_HEAD && \
    cp -r /tmp/react-native-desktop/react-native-cli /opt && \
    cd /opt/react-native-cli && \
    export NVM_DIR="$HOME/.nvm" && \
    [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh" && \
    nvm install ${NODE_VERSION} && \
    nvm use ${NODE_VERSION} && \
    npm update && npm install -g && \
    cd && rm -r /tmp/react-native-desktop

# These are the UID and GID values used by Jenkins
RUN addgroup --gid 1002 jenkins && \
    adduser --shell /bin/bash \
      --disabled-password --gecos "" \
      --uid 1001 --gid 1002 jenkins

# Install NVM for Jenkins
RUN su jenkins -c '/tmp/nvm/${NVM_VERSION}/install.sh'

LABEL source="https://github.com/status-im/status-react/tree/develop/desktop/windows/docker" \
      description="Image for building Windows Desktop version of Status app." \
      maintainer="jakub@status.im"
