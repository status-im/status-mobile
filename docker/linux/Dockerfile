FROM statusteam/status-build-base:1.1.0 AS qt_build

ARG QT_VERSION
ARG RNATIVE_VERSION
ARG QT_CI_COMMIT

ENV XDG_RUNTIME_DIR=/tmp/runtime-root

RUN apt-get update \
 && add-apt-repository -y ppa:git-core/ppa \
 && apt-get update \
 && apt-get -y install git libx11-xcb1 libxss1 libasound2 libfontconfig1 libdbus-1-3 \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# This variable defines which parts of QT will be extracted from the .run file.
# You can list available packages in a Qt .run file by using:
#   extract-qt-installer --list-packages qt-installer.run
ENV QT_CI_PACKAGES qt.qt5.5112.gcc_64,qt.qt5.5112.qtwebengine.gcc_64

# We run this as part of a separate container to reduce number of stages.
# We also remove some of the static-linked binaries since they are so big.
# Crucial ones: qmake,rcc,moc,uic,qdbuscpp2xml,qdbusxml2cpp,lrelease,lupdate,lconvert
# NOTE: Unfortunately GitHub doesn't support cloning a specific SHA1, so we need to
# resort to git fetch (https://stackoverflow.com/questions/3489173/how-to-clone-git-repository-with-specific-revision-changeset)
ADD qt-opensource-linux-x64-${QT_VERSION}.run /tmp/
RUN mkdir -p /tmp/qtci \
 && cd /tmp/qtci \
 && git init \
 && git remote add origin https://github.com/benlau/qtci.git \
 && git fetch --depth=1 origin ${QT_CI_COMMIT} \
 && git reset --hard FETCH_HEAD \
 && /tmp/qtci/bin/extract-qt-installer --disable-progress-report \
        /tmp/qt-opensource-linux-x64-${QT_VERSION}.run /opt/qt-all \
 && find /opt/qt-all/${QT_VERSION}/gcc_64/bin -type f -regextype posix-egrep -regex \
        '.*\/(qdoc|qgltf|linguist|designer|assistant|qhelpconverter|qmlprofiler)$' -delete \
 && rm -fr ${XDG_RUNTIME_DIR}

FROM statusteam/status-build-base:1.1.0

ARG QT_VERSION
ARG CMAKE_VERSION

COPY --from=qt_build /opt/qt-all/${QT_VERSION}/gcc_64 /opt/qt
RUN ln -s /opt/qt/mkspecs /usr/local/mkspecs \
 && ln -s /opt/qt/plugins /usr/local/plugins

# We have to do this because Jenkins doesn't let us
# https://issues.jenkins-ci.org/browse/JENKINS-49076
ENV PATH /opt/qt/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

RUN apt-get update \
 && add-apt-repository -y ppa:git-core/ppa \
 && add-apt-repository -y ppa:longsleep/golang-backports \
 && apt-get update \
 && DEBIAN_FRONTEND=noninteractive \
      apt-get -q -y --no-install-recommends install \
      golang-go python python3-pip python3-setuptools python3-wheel \
      apt-transport-https openjdk-8-jdk-headless \
      extra-cmake-modules build-essential gcc g++ fuse \
      libx11-xcb1 libxss1 libasound2 libgl-dev libsm6 libxrandr2 python-dev \
      libjasper-dev libegl1-mesa libxcomposite-dev libxcursor-dev \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* /usr/share/man

RUN cd /tmp \
 && wget -q https://cmake.org/files/v${CMAKE_VERSION%.*}/cmake-${CMAKE_VERSION}-Linux-x86_64.tar.gz \
 && tar xf cmake-${CMAKE_VERSION}-Linux-x86_64.tar.gz --strip 1 -C /usr/local \
 && rm -fr /usr/local/doc/cmake \
 && rm cmake-${CMAKE_VERSION}-Linux-x86_64.tar.gz

RUN mkdir -p /tmp/react-native-desktop \
 && cd /tmp/react-native-desktop \
 && git init \
 && git remote add origin https://github.com/status-im/react-native-desktop.git \
 && git fetch --quiet --depth=1 origin ${RNATIVE_VERSION} \
 && git reset --hard FETCH_HEAD \
 && cp -r /tmp/react-native-desktop/react-native-cli /opt \
 && cd /opt/react-native-cli \
 && npm update \
 && npm install -g --prefix /usr/local \
 && cd \
 && rm -r /tmp/react-native-desktop

LABEL source="https://github.com/status-im/status-react/tree/develop/desktop/docker" \
      description="Image for building Linux Desktop version of Status app." \
      maintainer="jakub@status.im"
