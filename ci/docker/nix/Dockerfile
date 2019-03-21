# This image is only for extracting and cleaning up Android SDK
FROM ubuntu:18.04 AS android_sdk

ARG ANDROID_SDK_VERSION
ARG SDK_PLATFORM_VERSION
ARG SDK_BUILD_TOOLS_VERSION

# names of packages to install with sdkmanager
ENV SDK_PACKAGES emulator patcher;v4 \
                 platforms;android-26 \
                 platforms;${SDK_PLATFORM_VERSION} \
                 build-tools;${SDK_BUILD_TOOLS_VERSION} \
                 extras;android;m2repository extras;google;m2repository

ENV LANG=en_US.UTF-8 \
    LC_ALL=en_US.UTF-8 \
    LANGUAGE=en_US.UTF-8

RUN apt-get update && apt-get -q -y --no-install-recommends install software-properties-common \
 && add-apt-repository -y ppa:webupd8team/java \
 && echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections \
 && echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections \
 && apt-get update \
 && DEBIAN_FRONTEND=noninteractive apt-get -q -y --no-install-recommends install \
      wget unzip locales ca-certificates oracle-java8-installer oracle-java8-set-default \
 && locale-gen ${LANG} \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* /usr/share/man

# install Android SDK
COPY sdk-tools-linux-${ANDROID_SDK_VERSION}.zip /tmp/sdk-tools-linux.zip
RUN unzip -q /tmp/sdk-tools-linux.zip -d /usr/lib/android-sdk \
 && mkdir ~/.android && touch ~/.android/repositories.cfg
RUN /usr/lib/android-sdk/tools/bin/sdkmanager --licenses
RUN for PKG in ${SDK_PACKAGES}; do \
      yes | /usr/lib/android-sdk/tools/bin/sdkmanager --install "${PKG}" > /dev/null; done \
 && chmod 777 -R /usr/lib/android-sdk
# cleanup SDK
RUN cd /usr/lib/android-sdk \
 && rm -fr extras emulator ndk-bundle/toolchains/{aarch64-linux-android-4.9,x86-4.9,x86_64-4.9,llvm}

################################################################################
FROM ubuntu:18.04
ENV LANG=en_US.UTF-8 \
    LC_ALL=en_US.UTF-8 \
    LANGUAGE=en_US.UTF-8

ARG NIX_VERSION
# DEPS_HASH is not used expect for a new image to be created in case some indirect dependency changes (e.g. a download brought by nix-shell)
ARG DEPS_HASH
ARG ANDROID_SDK_VERSION
ARG JENKINS_UID
ARG JENKINS_GID

RUN apt-get update \
 && DEBIAN_FRONTEND=noninteractive \
    apt-get -q -y --no-install-recommends \
      install file binutils make locales curl git strace jq bzip2 ca-certificates s3cmd \
 && apt-get clean \
 && locale-gen ${LANG} \
 && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* /usr/share/man

ADD nix-install-${NIX_VERSION}.sh /tmp/nix-install.sh

RUN groupadd --gid ${JENKINS_GID} jenkins \
 && useradd --shell /bin/bash --create-home --password "" \
      --comment "jenkins" --groups root \
      --uid ${JENKINS_UID} --gid ${JENKINS_GID} jenkins

ADD nix.conf /home/jenkins/.config/nix/nix.conf
ADD default.nix /home/jenkins/default.nix
ADD default.nix /bin/default.nix

RUN mkdir /nix \
 && chown ${JENKINS_UID}:${JENKINS_GID} -R /nix /home/jenkins

WORKDIR /home/jenkins
USER jenkins

# Install Android SDK
RUN mkdir -p /home/jenkins/.status/Android/Sdk
COPY --from=android_sdk /usr/lib/android-sdk /home/jenkins/.status/Android/Sdk
RUN chmod o+w /home/jenkins/.status/Android/Sdk

RUN export USER=jenkins \
 && /tmp/nix-install.sh \
 && . ~/.nix-profile/etc/profile.d/nix.sh \
 && nix-shell https://github.com/status-im/status-react/tarball/develop --run 'echo'

# hack to avoid calling login
ENV USER=jenkins
# make nix tools available by default
ENV PATH=/home/jenkins/.nix-profile/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
ENV NIX_PATH=nixpkgs=/home/jenkins/.nix-defexpr/channels/nixpkgs
ENV NIX_SSL_CERT_FILE=/etc/ssl/certs/ca-certificates.crt

MAINTAINER Jakub Sokolowski "jakub@status.im"
LABEL description="Adjusted Nix Docker image for use with Jenkins"
