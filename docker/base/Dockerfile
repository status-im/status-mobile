FROM ubuntu:16.04

ARG NODE_VERSION
ARG YARN_VERSION
ARG LEIN_VERSION

# We have to do this because Jenkins doesn't let us
# https://issues.jenkins-ci.org/browse/JENKINS-49076
ENV PATH=/home/jenkins/.nvm/versions/node/v${NODE_VERSION}/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin \
    LANG=en_US.UTF-8 \
    LC_ALL=en_US.UTF-8 \
    LANGUAGE=en_US.UTF-8 \
    LEIN_HOME=/var/tmp/lein \
    LEIN_URL=https://raw.githubusercontent.com/technomancy/leiningen/${LEIN_VERSION}/bin/lein \
    YARN_CACHE_FOLDER=/var/tmp/yarn \
    NVM_DIR=/home/jenkins/.nvm

RUN mkdir /var/tmp/npm /var/tmp/lein \
 && chmod 777 /var/tmp/npm /var/tmp/lein \
 && apt-get update && apt-get -q -y --no-install-recommends install curl software-properties-common \
 && add-apt-repository -y ppa:webupd8team/java \
 && echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections \
 && echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections \
 && curl -sL https://deb.nodesource.com/setup_10.x | bash - \
 && apt-get update \
 && DEBIAN_FRONTEND=noninteractive apt-get -q -y --no-install-recommends install \
      file autoconf automake libtool build-essential \
      jq git wget zip unzip s3cmd nodejs locales \
      ca-certificates oracle-java8-installer oracle-java8-set-default \
 && locale-gen ${LANG} \
 && npm install -g yarn@${YARN_VERSION} \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* /usr/share/man

# These are the UID and GID values used by Jenkins
RUN addgroup --gid 1001 jenkins \
 && adduser --shell /bin/bash \
      --disabled-password --gecos "" \
      --uid 1001 --gid 1001 jenkins \
 && su jenkins -c "git config --global user.email 'jenkins'" \
 && su jenkins -c "git config --global user.name 'jenkins@status.im'"

# Leiningen setup
RUN wget ${LEIN_URL} -O /usr/local/bin/lein \
 && chmod 755 /usr/local/bin/lein \
 && lein version

# Install NVM for Jenkins
ADD nvm_install.sh /tmp/nvm_install.sh
RUN su -l jenkins -c \
   "bash /tmp/nvm_install.sh && \
    . ${NVM_DIR}/nvm.sh && \
    nvm install ${NODE_VERSION} && \
    nvm alias default ${NODE_VERSION}"

WORKDIR /home/jenkins

LABEL source="https://github.com/status-im/status-react/tree/develop/docker/base" \
      description="Base Image used for building Status app." \
      maintainer="jakub@status.im"
