# This image is only for extracting and cleaning up NDK and SDK
ARG BASE_IMAGE_TAG

FROM statusteam/status-build-base:${BASE_IMAGE_TAG} AS sdk_and_ndk

ARG ANDROID_NDK_VERSION
ARG ANDROID_SDK_VERSION
ARG SDK_PLATFORM_VERSION
ARG SDK_BUILD_TOOLS_VERSION

# names of packages to install with sdkmanager
ENV SDK_PACKAGES emulator patcher;v4 \
                 platforms;android-26 \ 
                 platforms;${SDK_PLATFORM_VERSION} \
                 build-tools;${SDK_BUILD_TOOLS_VERSION} \
                 extras;android;m2repository extras;google;m2repository

# install SDK
COPY sdk-tools-linux-${ANDROID_SDK_VERSION}.zip /tmp/sdk-tools-linux.zip
RUN unzip -q /tmp/sdk-tools-linux.zip -d /usr/lib/android-sdk \
 && mkdir ~/.android && touch ~/.android/repositories.cfg \
 && yes | /usr/lib/android-sdk/tools/bin/sdkmanager --licenses > /dev/null \
 && for PKG in ${SDK_PACKAGES}; do \
      yes | /usr/lib/android-sdk/tools/bin/sdkmanager --install "${PKG}" > /dev/null; done \
 && chmod 777 -R /usr/lib/android-sdk
# cleanup SDK
RUN cd /usr/lib/android-sdk \
 && rm -fr extras emulator ndk-bundle/toolchains/{aarch64-linux-android-4.9,x86-4.9,x86_64-4.9,llvm}

# install NDK
COPY android-ndk-${ANDROID_NDK_VERSION}-linux-x86_64.zip /tmp/android-ndk-linux.zip
RUN unzip -q /tmp/android-ndk-linux.zip -d /usr/lib \
 && ln -sf /usr/lib/android-ndk-${ANDROID_NDK_VERSION} /usr/lib/android-ndk \
 && chmod 777 -R /usr/lib/android-ndk
# cleanup NDK
RUN cd /usr/lib/android-ndk && rm -fr docs tests samples \
 && find toolchains -mindepth 1 -maxdepth 1 | grep -vE '(arm|aarch|x86|mips).*4.(8|9)' | xargs rm -fr \
 && find prebuilt -mindepth 1 -maxdepth 1 | grep -vE '(android-arm|linux-x86)' | xargs rm -fr \
 && find platforms -mindepth 1 -maxdepth 1 | grep -v android-21 | xargs rm -fr \
 && find sources -mindepth 2 -maxdepth 2 | grep -v 'gnu-libstdc' | xargs rm -fr

################################################################################
FROM statusteam/status-build-base:${BASE_IMAGE_TAG}

ARG ANDROID_NDK_VERSION
ARG ANDROID_SDK_VERSION

# We have to do this because Jenkins doesn't let us
# https://issues.jenkins-ci.org/browse/JENKINS-49076
ENV GRADLE_USER_HOME=/var/tmp/gradle \
    BUNDLE_PATH=/var/tmp/bundle \
    ANDROID_HOME=/usr/lib/android-sdk \
    ANDROID_SDK_ROOT=/usr/lib/android-sdk \
    ANDROID_NDK=/usr/lib/android-ndk \
    ANDROID_NDK_HOME=/usr/lib/android-ndk

RUN add-apt-repository -y ppa:cwchien/gradle \
 && apt-get update \
 && DEBIAN_FRONTEND=noninteractive \
      apt-get -q -y --no-install-recommends install \
        gradle ruby ruby-dev ruby-bundler \
 && apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* /usr/share/man \
 && gem install cocoapods bundler fastlane fastlane-plugin-diawi fastlane-plugin-clean_testflight_testers CFPropertyList

# Install Android SDK & NDK
COPY --from=sdk_and_ndk /usr/lib/android-sdk /usr/lib/android-sdk 
COPY --from=sdk_and_ndk /usr/lib/android-ndk /usr/lib/android-ndk 
RUN chmod o+w /usr/lib/android-sdk /usr/lib/android-ndk

LABEL source="https://github.com/status-im/status-react/tree/develop/docker/android" \
      description="Image for building Android version of Status app." \
      maintainer="jakub@status.im"
