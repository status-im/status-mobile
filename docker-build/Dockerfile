FROM openjdk:8-jdk

ENV DEBIAN_FRONTEND noninteractive
ENV ANDROID_HOME /opt/android-sdk-linux
ENV ANDROID_SDK_HOME /opt/android-sdk-linux
ENV PATH ${PATH}:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/platform-tools:${PATH}:${ANDROID_HOME}/tools
ENV ANDROID_NDK /opt/android-ndk-linux
ENV ANDROID_NDK_HOME /opt/android-ndk-linux

WORKDIR /opt

RUN mkdir -p /opt/android-sdk-linux && mkdir -p ~/.android && touch ~/.android/repositories.cfg

# Install dependencies
RUN apt-get update && \
    apt-get install -yq supervisor curl unzip wget sudo maven libc6 libstdc++6 zlib1g libncurses5 make file --no-install-recommends && \
    apt-get clean

RUN cd /opt/android-sdk-linux && \
	wget -q --output-document=sdk-tools.zip https://dl.google.com/android/repository/sdk-tools-linux-3859397.zip && \
	unzip sdk-tools.zip && \
	rm -f sdk-tools.zip && \
	sdkmanager "cmake;3.6.4111459"

RUN cd /opt/android-sdk-linux && \
    wget -q --output-document=sdk-platform-tools.zip  'https://dl.google.com/android/repository/platform-tools-latest-linux.zip' && \
    unzip sdk-platform-tools.zip && \
    rm -f sdk-platform-tools.zip

RUN wget -q --output-document=android-ndk.zip http://dl.google.com/android/repository/android-ndk-r10e-linux-x86_64.zip && \
	unzip android-ndk.zip && \
	rm -f android-ndk.zip && \
	mv android-ndk-r10e android-ndk-linux

RUN yes | sdkmanager --licenses

# install Node JS (https://github.com/nodejs/docker-node/blob/90d5e3df903b830d039d3fe8f30e3a62395db37e/7.5/Dockerfile)

RUN groupadd --gid 1000 node \
  && useradd --uid 1000 --gid node --shell /bin/bash --create-home node

ENV NPM_CONFIG_LOGLEVEL info
# that's the only version that doesn't hang on installing react-native-http inside docker
ENV NODE_VERSION 10.14.0
ENV NODE_ENV development

RUN curl -SLO "https://nodejs.org/dist/v$NODE_VERSION/node-v$NODE_VERSION-linux-x64.tar.xz" \
  && tar -xJf "node-v$NODE_VERSION-linux-x64.tar.xz" -C /usr/local --strip-components=1 \
  && rm "node-v$NODE_VERSION-linux-x64.tar.xz" \
  && ln -s /usr/local/bin/node /usr/local/bin/nodejs

# END install Node JS

# Support Gradle
ENV TERM dumb

# Install Lein
# Check out latest releases from https://github.com/technomancy/leiningen/releases
ENV LEIN_VERSION=2.8.1
ENV LEIN_INSTALL=/usr/local/bin/

RUN mkdir -p $LEIN_INSTALL \
  && wget -q https://raw.githubusercontent.com/technomancy/leiningen/$LEIN_VERSION/bin/lein-pkg \
  && mv lein-pkg $LEIN_INSTALL/lein \
  && chmod 0755 $LEIN_INSTALL/lein \
  && wget -q https://github.com/technomancy/leiningen/releases/download/$LEIN_VERSION/leiningen-$LEIN_VERSION-standalone.zip \
  && mkdir -p /usr/share/java \
  && mv leiningen-$LEIN_VERSION-standalone.zip /usr/share/java/leiningen-$LEIN_VERSION-standalone.jar

# Install React-Native
RUN npm install -g react-native-cli

# Add files needed for installing dependencies
# Use directory /build for that purpose
WORKDIR /build

ADD .env.jenkins .env

ADD ./project.clj ./

ADD ./package.json package-lock.json ./

RUN npm install

ADD . ./

RUN make setup
RUN make prepare

COPY supervisord.conf /etc/supervisord.conf

RUN keytool -genkey -v -keystore status-im.keystore -storepass password -alias status -keyalg RSA -keysize 2048 -validity 10000  -dname "CN=mqttserver.ibm.com, OU=ID, O=IBM, L=Hursley, S=Hants, C=GB" -keypass password
RUN mkdir -p /root/.gradle
RUN mv status-im.keystore /root/.gradle/


RUN echo "sdk.dir=/opt/android-sdk-linux" >> android/local.properties
RUN echo "ndk.dir=/opt/android-ndk-linux" >> android/local.properties

RUN cd ./android && ./gradlew react-native-android:installArchives
RUN cd ./android && ./gradlew assembleRelease

CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]
