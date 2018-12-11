FROM ubuntu:16.04 AS qt_build

ARG QT_VERSION

RUN apt-get update && apt-get -y install \
      git libx11-xcb1 libxss1 libasound2 libfontconfig1 libdbus-1-3 && \
    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# This variable defines which parts of QT will be extracted from the .run file.
# You can list available packages in a Qt .run file by using:
#   extract-qt-installer --list-packages qt-installer.run
ENV QT_CI_PACKAGES qt.qt5.5112.gcc_64,qt.qt5.5112.qtwebengine.gcc_64

# We run this as part of a separate container to reduce number of stages.
# We also remove some of the static-linked binaries since they are so big.
# Crucial ones: qmake,rcc,moc,uic,qdbuscpp2xml,qdbusxml2cpp,lrelease,lupdate,lconvert
ADD qt-opensource-linux-x64-${QT_VERSION}.run /tmp/
RUN git clone --depth=1 https://github.com/benlau/qtci.git /tmp/qtci && \
    /tmp/qtci/bin/extract-qt-installer --disable-progress-report \
        /tmp/qt-opensource-linux-x64-${QT_VERSION}.run /opt/qt-all && \
    find /opt/qt-all/${QT_VERSION}/gcc_64/bin -type f -regextype posix-egrep -regex \
        '.*\/(qdoc|qgltf|linguist|designer|assistant|qhelpconverter|qmlprofiler)$' -delete

FROM ubuntu:16.04

ARG QT_VERSION

COPY --from=qt_build /opt/qt-all/${QT_VERSION}/gcc_64 /opt/qt
RUN ln -s /opt/qt/mkspecs /usr/local/mkspecs && \
    ln -s /opt/qt/plugins /usr/local/plugins

ENV LANG en_US.UTF-8
ENV LC_ALL en_US.UTF-8
ENV LANGUAGE en_US.UTF-8
ENV LEIN_HOME /var/tmp/lein
ENV NPM_CONFIG_CACHE /var/tmp/npm

# We have to do this because Jenkins doesn't let us
# https://issues.jenkins-ci.org/browse/JENKINS-49076
ENV PATH /opt/qt/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

RUN apt-get update && apt-get -q -y --no-install-recommends install curl software-properties-common && \
    curl -sL https://deb.nodesource.com/setup_10.x | bash - && \
    curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | apt-key add - && \
    echo "deb https://dl.yarnpkg.com/debian/ stable main" | tee /etc/apt/sources.list.d/yarn.list && \
    add-apt-repository -y ppa:longsleep/golang-backports && \
    apt-get remove -y software-properties-common && \
    apt-get update && \
    DEBIAN_FRONTEND=noninteractive \
      apt-get -q -y --no-install-recommends install \
      wget git unzip golang-go nodejs yarn file s3cmd \
      python python3-pip python3-setuptools python3-wheel \
      apt-transport-https locales openjdk-8-jdk-headless \
      extra-cmake-modules build-essential gcc g++ fuse \
      libx11-xcb1 libxss1 libasound2 libgl-dev libsm6 libxrandr2 python-dev \
      libjasper-dev libegl1-mesa libxcomposite-dev libxcursor-dev && \
    locale-gen en_US.UTF-8 && \
    npm install -g yarn@1.12.3 && \
    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* /usr/share/man

RUN cd /tmp && wget -q https://cmake.org/files/v3.12/cmake-3.12.2-Linux-x86_64.tar.gz && \
    tar xf cmake-3.12.2-Linux-x86_64.tar.gz --strip 1 -C /usr/local && \
    rm -fr /usr/local/doc/cmake && rm cmake-3.12.2-Linux-x86_64.tar.gz

RUN curl -sL https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein -o /usr/bin/lein && \
    chmod +x /usr/bin/lein && /usr/bin/lein version

RUN cd /tmp && \
    git clone --depth=1 https://github.com/status-im/react-native-desktop.git && \
    cp -r /tmp/react-native-desktop/react-native-cli /opt && \
    cd /opt/react-native-cli && \
    npm update && npm install -g && \
    cd && rm -r /tmp/react-native-desktop

# These are the UID and GID values used by Jenkins
RUN addgroup --gid 1002 jenkins && \
    adduser --shell /bin/bash \
      --disabled-password --gecos "" \
      --uid 1001 --gid 1002 jenkins

# Install NVM for Jenkins
RUN su jenkins -c 'curl -s -o- https://raw.githubusercontent.com/creationix/nvm/v0.33.11/install.sh | bash'

LABEL source="https://github.com/status-im/status-react/tree/develop/desktop/docker" \
      description="Image for building Linux Desktop version of Status app." \
      maintainer="jakub@status.im"
