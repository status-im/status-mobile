FROM ubuntu:16.04 AS qt_build

RUN apt-get update && apt-get -y install \
      git libx11-xcb1 libxss1 libasound2 libfontconfig1 libdbus-1-3 && \
    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

ADD qt-opensource-linux-x64-5.9.1.run /tmp/
RUN git clone --depth=1 https://github.com/benlau/qtci.git /tmp/qtci && \
    /tmp/qtci/bin/extract-qt-installer /tmp/qt-opensource-linux-x64-5.9.1.run /opt/qt && \
    cd /opt/qt && \
    rm -fr Examples Docs Tools 5.9.1/android_armv7 \
      MaintenanceTool.dat MaintenanceTool MaintenanceTool.ini

FROM ubuntu:16.04

COPY --from=qt_build /opt/qt /opt/qt
RUN ln -s /opt/qt/5.9.1/gcc_64/mkspecs /usr/local/mkspecs && \
    ln -s /opt/qt/5.9.1/gcc_64/plugins /usr/local/plugins && \
    ln -s /opt/qt/5.9.1/gcc_64 /opt/qt59

ENV LANG en_US.UTF-8 \
    LC_ALL en_US.UTF-8 \
    LANGUAGE en_US.UTF-8 \
    LEIN_HOME /var/tmp/lein \
    NPM_CONFIG_CACHE /var/tmp/npm

# We have to do this because Jenkins doens't let us
# https://issues.jenkins-ci.org/browse/JENKINS-49076
ENV PATH /opt/qt59/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

RUN apt-get update && apt-get -y --no-install-recommends install curl software-properties-common && \
    curl -sL https://deb.nodesource.com/setup_8.x | bash - && \
    curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | apt-key add - && \
    echo "deb https://dl.yarnpkg.com/debian/ stable main" | tee /etc/apt/sources.list.d/yarn.list && \
    add-apt-repository -y ppa:longsleep/golang-backports && \
    apt-get remove -y software-properties-common && \
    apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
      wget git unzip golang-go nodejs yarn file python \
      apt-transport-https locales openjdk-8-jdk-headless \
      extra-cmake-modules build-essential gcc g++ fuse libcairo2-dev \
      libx11-xcb1 libxss1 libasound2 libgl-dev libsm6 libxrandr2 python-dev \
      libjasper-dev libegl1-mesa libxcomposite-dev libxcursor-dev && \
    locale-gen en_US.UTF-8 && \
    npm install -g npm@5.5.1 && \
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
      --uid 1001 --gid 1002 \
      --home /var/tmp/jenkins jenkins

LABEL source="https://github.com/status-im/status-react/tree/develop/desktop/docker" \
      description="Image for building Linux Desktop version of Status app." \
      maintainer="jakub@status.im"
