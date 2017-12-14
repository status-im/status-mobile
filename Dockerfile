FROM ubuntu:16.04

ENV TERM xterm
ENV DEBIAN_FRONTEND noninteractive
ENV USER developer
ENV HOME /home/${USER}

# Setup user
RUN export uid=1000 gid=1000 && \
    mkdir -p /home/${USER} && \
    echo "${USER}:x:${uid}:${gid}:${USER},,,:/home/${USER}:/bin/bash" >> /etc/passwd && \
    echo "${USER}:x:${uid}:" >> /etc/group && \
    mkdir -p /etc/sudoers.d && \
    echo "${USER} ALL=(ALL) NOPASSWD: ALL" > /etc/sudoers.d/${USER} && \
    chmod 0440 /etc/sudoers.d/${USER} && \
    chown ${uid}:${gid} -R /home/${USER}

# These are assumed to be installed by default by the user
RUN apt-get update && \
    apt-get install -y \
      android-tools-adb \
      git \
      software-properties-common \
      sudo \
      && \
    apt update

# Install base dependencies
ADD . /home/${USER}/status
RUN chown -R ${USER}:${USER} /home/${USER}
USER ${USER}
WORKDIR /home/${USER}/status
RUN scripts/setup
