# Description

This document describes how to update Status APK builds for the [F-Droid](https://f-droid.org/) Android application catalogue.

# Intro

In simplest terms F-Droid requires a YAML file that defines the steps necessary to create a universal unsigned APK build. This is achieved by submitting a new app version into the `metadata/im.status.ethereum.yml` file in the [fdroiddata](https://gitlab.com/fdroid/fdroiddata) repository.

The app builds defined this way run on servers that generate the unsigned APKs using the [fdroidserver](https://gitlab.com/fdroid/fdroidserver) software. The [server setup](https://f-droid.org/en/docs/Build_Server_Setup/) is quite involved but is not necessary unless you want to run your own instance. Normally the applications defined in `fdroiddata` are built by servers maintained by [the F-Droid volunteers](https://f-droid.org/en/contribute/).

First release of Status app was merged in [fdroid/fdroiddata#7179](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/7179).

:warning: __WARNING__: Once changes are committed into `fdroiddata` repo they __cannot be changed__.

# Adding New Versions

There are two ways - automated and manual - described below.

## Automated

The script will analyze a provided APK, update the metadata file based on that information, and commit the change to [`fdroiddata`](https://gitlab.com/fdroid/fdroiddata) repo. The creation of merge request is manual.

#### Requirements:

- GitLab account
- Forked copy of the [`fdroiddata`](https://gitlab.com/fdroid/fdroiddata) repo
- Link to the release published

#### Steps

1. Use the F-Droid PR update script via `make`:
    ```sh
    make fdroid-pr APK=StatusIm-Mobile-v1.16.0-ef34af.apk
    ```
    The script also accepts a URL.
2. Add a fork repo:
    ```sh
    git remote add john https://gitlab.com/john/fdroiddata.git
    ```
3. Push:
    ```sh
    git push john status-im/v1.16.0
    ```
4. [Create a PR via the GitLab interface.](https://docs.gitlab.com/ee/user/project/merge_requests/creating_merge_requests.html)


## Manual

You can find our configuration file at [`metadata/im.status.ethereum.yml`](https://gitlab.com/fdroid/fdroiddata/-/blob/master/metadata/im.status.ethereum.yml)

The file defines all the necessary metadata like `SourceCode`, `Website`, or `License`, but the most important key is `Builds`, which looks like this:
```yml
Builds:
  - versionName: 1.12.0
    versionCode: 2021022512
    commit: cfb825a11b61d312af8cb5d36686af540c31f481
    sudo:
      - cd build/im.status.ethereum
      - make fdroid-build-env
    init: nix/scripts/setup.sh
    output: result/app-release-unsigned.apk
    scanignore:
      - android/build.gradle
    scandelete:
      - ios
    build: make release-fdroid BUILD_NUMBER=$$VERCODE$$
```
It contains a list of objects defining each release of the application. In order to add a new release simply copy a previous release object and adjust the following values:

* `versionName` - String version like `2.34.4`.
* `versionCode` - Android `versionCode`. Normally it is generated from timestamp at build time.
* `commit` - Specific commit SHA1 from which the given release was built.

The `versionCode` should be the same as the one in build that was uploaded to Play Store.
It can be found in the build logs or by using:
```
 > make shell TARGET=android
 > apkanalyzer manifest version-code StatusIm-Mobile-v2.34.4.apk
2021022512
```

At the bottom of the file you should also update the following keys:

* `CurrentVersion` - Same as the new `versionName` added
* `CurrentVersionCode` - Same as the `versionCode` added

Then submit a merge request with `Draft: ` prefix to the [fdroid/fdroiddata](https://gitlab.com/fdroid/fdroiddata) repository.
Prefix is necessary to avoid F-Droid people merging the PR before it's ready.

:warning: __WARNING__: Currently GitLab PR builds will fail due to running as `root` instead of `vagrant` and failing to install Nix.

# Checking Builds

## Prepare docker image
The simplest way to test if the app builds via F-Droid is to use the Docker image:
https://gitlab.com/fdroid/docker-executable-fdroidserver

Which is available under `registry.gitlab.com/fdroid/docker-executable-fdroidserver:latest`.

Because we use Nix to build the mobile app we need to slightly modify the image with this updates to the original `Dockerfile`:
```Dockerfile
FROM registry.gitlab.com/fdroid/fdroidserver:buildserver

# ADD: ensure proper user and id to build with (next 5 lines):
ARG BUILDER_UID=1000
ENV BUILDER_USER=vagrant
RUN usermod -u $BUILDER_UID $BUILDER_USER
RUN mkdir -p -m 0755 /nix /home/$BUILDER_USER \
    && chown -R $BUILDER_USER /nix /home/$BUILDER_USER

RUN . /etc/profile.d/bsenv.sh \
    && git clone --depth 1 https://gitlab.com/fdroid/fdroidserver.git "${fdroidserver}"

# Install additional utilities required by actual builds
# build-tools 32.0.0 is needed for a good apksigner
RUN . /etc/profile.d/bsenv.sh \
    && apt-get update && apt-get install --yes \
        patch \
        autoconf libtool pkg-config \
        ant \
        make xz-utils \ # ADD: additional packages
    && echo y | sdkmanager "build-tools;32.0.0" \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

RUN git config --system safe.directory '*'

ENV PATH="${PATH}:${fdroidserver}"

VOLUME ["/repo"]
WORKDIR /repo

ENTRYPOINT ["sh", "-c", ". /etc/profile.d/bsenv.sh && GRADLE_USER_HOME=${home_vagrant}/.gradle ${fdroidserver}/fdroid \"$@\"", "-s"]
CMD ["--help"]
```
Build it using:
```
docker build --build-arg=BUILDER_UID=$UID -t statusteam/docker-executable-fdroidserver:latest .
```

## Run the build

**Docker Memory Settings**

F-Droid builds are memory-intensive. Ensure Docker has at least 20GB of memory allocated:

1. Open Docker Desktop
2. Go to **Settings → Resources**
3. Set **Memory** to at least 16GB (it fails on 8GB)
4. Click **Apply & Restart**

Verify the setting:
```bash
docker info | grep -E "Memory|CPUs"
```


Then clone the [fdroiddata](https://gitlab.com/fdroid/fdroiddata) and [fdroidserver](https://gitlab.com/fdroid/fdroidserver) repos and use it to test the build of a specific Status Android app version: `2.34.4`.
Make sure you've updated local file at `metadata/im.status.ethereum.yml`to contain the version you need to build
```
git clone https://gitlab.com/fdroid/fdroidserver
git clone https://gitlab.com/fdroid/fdroiddata
# modify metadata/im.status.ethereum.yml at this point

docker run --rm \
  -u $UID:$(id -g) \
  -e USER=vagrant \
  -v $PWD/fdroiddata:/repo \
  -v $PWD/fdroidserver:/fdroidserver \
  statusteam/docker-executable-fdroidserver:latest \
  build im.status.ethereum:2025102415     <-- here you specify the versionCode of your version
```
We have to create a user and specify the UID because Nix cannot run as `root` and that is the default user for the F-Droid Docker image. By adding our own user and setting the UID we also make it possible to mount folders like `fdroiddata` and `fdroidserver`.

**Build command explanation:**
- `-u $UID:$(id -g)` - Run as current user for proper file permissions
- `-e USER=vagrant` - Set USER environment variable
- `-v $PWD/fdroiddata:/repo` - Mount fdroiddata as /repo in container
- `-v $PWD/fdroidserver:/fdroidserver` - Mount fdroidserver tools
- `build im.status.ethereum:2025102415` - Build command with app ID and version code

You can specify a `--verbose` flag for `build` command for additional information.
You can provide `--storage-opt size=30G` flag if Docker complains about running out of space.

You should also run `lint` and `rewritemeta` for the App ID to verify and fix the YAML metadata formatting.

** Monitor Build Progress **

In another terminal, you can monitor the build logs:

```bash
tail -f fdroiddata/logs/im.status.ethereum.log
```

## Check build success

Once the build completes successfully, the unsigned APK will be located at:

```bash
fdroiddata/unsigned/im.status.ethereum_2025102415.apk
```

# Scanning Builds Locally

Based on the documentation provided here: https://gitlab.com/fdroid/wiki/-/wikis/Tips-for-fdroiddata-contributors/Setting-up-app-submission-development-environment-locally

We can locally scan our source code and pre-built APKS by using the FDroid CLI tool. We can prepare a FDroid environment by
following [the steps mentioned in these instructions](https://gitlab.com/fdroid/wiki/-/wikis/Tips-for-fdroiddata-contributors/Setting-up-app-submission-development-environment-locally). 
  * Note, at the time of writing (March 2025), the easiest way to follow these instructions involved:
    * Creating a new empty directory outside of the status-mobile repository.
      * For this example, let's refer to this directory as `status-fdroid-server`
    * Then git cloning the two referenced git repos inside the `status-fdroid-server` directory:
      ```shell
        git clone https://gitlab.com/fdroid/fdroiddata.git --depth=1
        git clone https://gitlab.com/fdroid/fdroidserver.git --depth=1
      ```
    * Then create a subdirectory inside `status-fdroid-server` named `apks`
    * Then create a subdirectory inside `status-fdroid-server` named `scripts`
      * Create a file inside the `scripts` folder named `env.sh`
        ```shell
          source /etc/profile.d/bsenv.sh
          export GRADLE_USER_HOME=$home_vagrant/.gradle
          export fdroid="sudo --preserve-env --user vagrant
                 env PATH=$fdroidserver:$PATH
                 env PYTHONPATH=$fdroidserver:$fdroidserver/examples
                 env PYTHONUNBUFFERED=true
                 env TERM=$TERM
                 env HOME=$home_vagrant
                 fdroid"

          export build=im.status.ethereum
        ```
      * Create a file inside the `scripts` folder named `prep.sh`
        ```shell
          apt-get update && apt-get install -y make xz-utils
        ```
      * Create a file inside the `scripts` folder named `run.sh`
        ```shell
          docker run --rm -i -t --privileged --pid=host --entrypoint "/bin/bash" \
            -v ${ANDROID_SDK_ROOT}:/opt/android-sdk:Z \
            -v $(pwd)/fdroidserver:/home/vagrant/fdroidserver:Z \
            -v $(pwd)/scripts:/scripts:z \
            -v $(pwd)/fdroiddata:/build:z \
            -v $(pwd)/apks:/apks:z \
            registry.gitlab.com/fdroid/fdroidserver:buildserver
        ```
      * Create a file inside the `scripts` folder named `latest-version-code.sh`
        ```shell
          prefix="versionCode: "
          resultLine=$(grep $prefix ./metadata/$build.yml | tail -n 1)
          echo $resultLine | sed -e "s/^$prefix//" 
        ```
    * Then inside a terminal shell run the following command:
      * `env ANDROID_SDK_ROOT=<insert sdk directory> sh scripts/run.sh`
        * Ensure you replace `<insert sdk directory>` with the path to Android SDK folder.
          * On macOS, the value could be: `$HOME/Library/Android/sdk`
    * Once inside the Docker shell environment run the following commands:
      * `sh /scripts/prep.sh`
        * This command should install `xz` utils packages for installing the Nix package manager.
      * `source /scripts/env.sh`
        * This command should populate the Docker shell environment with the `$fdroid` command.
    * Next we can run `cd /build` to enter the `fdroiddata` directory that has been mounted inside the docker environment.
    * And now we can run `$fdroid scanner $build:$(sh /scripts/latest-version-code.sh)` to scan the latest version of the status app that has been registered in the `fdroiddata/metadata/im.status.ethereum.yml` file.
      * Alternatively, we can also copy an APK into the `apks` folder and run `$fdroid scanner /apks/<insert-apk-file-name>`.
        * Ensure that you replace the `<insert-apk-file-name>` with the correct name of the pre-built APK.
          * For example, after running `make release-fdroid`, you can copy the `app-arm64-v8a-release-unsigned.apk` file into the `apks` folder and run `$fdroid scanner /apks/app-arm64-v8arelease-unsigned.apk` inside the docker shell environment.

# Details

The original research was done in [#8512](https://github.com/status-im/status-mobile/issues/8512).

Normally F-Droid server wants to run Gradle itself, but we do not specify the `gradle` key in order to run `make release-fdroid` ourselves in `build` step. We also add `android/build.gradle` to `scanignore` to avoid F-Droid trying to use Gradle directly.

The Android metadata like description or screenshots is [loaded from our repository](https://f-droid.org/en/docs/All_About_Descriptions_Graphics_and_Screenshots/#fastlane-structure) based on the Fastlane [`supply`](https://docs.fastlane.tools/actions/supply/) tool folder structure for updating Google Play store metadata.

Once the PR is merged it may take a few days for the F-Droid server farm to build and deploy the new version to their site and app. You can look up the current state of builds [here](https://f-droid.org/wiki/index.php?title=Special:RecentChanges&days=7&from=&hidebots=0&hideanons=1&hideliu=1&limit=500) and look for your App ID and a `deploy` change after it.

# F-Droid Build

F-Droid requires submitted applications to be free of non Open Source software. For this reason we need to exclude certain Google libraries at build time.

This is achieved by setting `GOOGLE_FREE=1` in the `.env.release` file. This affects two things:

1. [Clojure code conditional](../src/status_im/acquisition/core.cljs) which stops use of `get-referrer`.
2. [Gradle build configuration](../android/app/build.gradle) by excluding Google libraries.

Both are affected by the `.env` file and both are required.
