#
# This script prepares a finalized version of node_modules,
#   as well as a local version of the Maven repository required by Gradle scripts
#

{ stdenv, stdenvNoCC, lib, callPackage,
  gradle, bash, file, nodejs, zlib,
  projectNodePackage, localMavenRepoBuilder, mkFilter }:

let
  mavenLocalRepo = callPackage ./maven { inherit localMavenRepoBuilder; stdenv = if stdenv.isLinux then stdenv else stdenvNoCC; };

  # Import the native dependencies for React Native Android builds
  react-native-deps = callPackage ./maven/reactnative-android-native-deps.nix { inherit stdenvNoCC; };

  createMobileFilesSymlinks = root: ''
    ln -sf ${root}/mobile_files/package.json.orig ${root}/package.json
    ln -sf ${root}/mobile_files/metro.config.js ${root}/metro.config.js
    ln -sf ${root}/mobile_files/yarn.lock ${root}/yarn.lock
  '';

  # fake build to pre-download deps into fixed-output derivation
  deps = 
    let
      # Place build target directories in NIX_BUILD_TOP (normally represents /build)
      projectBuildDir = "$NIX_BUILD_TOP/project";
      mavenRepoDir = "$NIX_BUILD_TOP/.m2/repository";
      reactNativeDepsDir = "$NIX_BUILD_TOP/deps"; # Use local writable deps, otherwise (probably due to some interaction between Nix sandboxing and Java) gradle will fail copying directly from the nix store
    in 
      stdenv.mkDerivation {
        name = "patched-android-gradle-and-npm-modules";
        src =
          let path = ./../../../..; # Import the root /android and /mobile_files folders clean of any build artifacts
          in builtins.path { # We use builtins.path so that we can name the resulting derivation, otherwise the name would be taken from the checkout directory, which is outside of our control
            inherit path;
            name = "status-react-source-gradle-install";
            filter =
              # Keep this filter as restrictive as possible in order to avoid unnecessary rebuilds and limit closure size
              mkFilter {
                dirRootsToInclude = [
                  "android" "mobile_files" "packager" "resources"
                  "translations" "status-modules"
                ];
                dirsToExclude = [ ".git" ".svn" "CVS" ".hg" ".gradle" "build" "intermediates" "libs" "obj" ];
                filesToInclude = [ ".babelrc" ];
                filesToExclude = [ "android/gradlew" ];
                root = path;
              };
          };
        phases = [ "unpackPhase" "patchPhase" "installPhase" "fixupPhase" ];
        nativeBuildInputs = [ projectNodePackage ];
        buildInputs = [ gradle nodejs bash file zlib mavenLocalRepo ];
        propagatedBuildInputs = [ react-native-deps ];
        unpackPhase = ''
          runHook preUnpack

          # Copy project directory
          mkdir -p ${projectBuildDir}
          cp -a $src/. ${projectBuildDir}
          chmod u+w ${projectBuildDir}
          cd ${projectBuildDir}

          # Copy RN maven dependencies and make them writable, otherwise Gradle copy fails (since the top-level directory is read-only, Java isn't smart enough to copy the child files/folders into that target directory)
          mkdir -p ${mavenRepoDir}
          cp -a ${mavenLocalRepo}/. ${mavenRepoDir}
          cp -a ${react-native-deps}/deps ${reactNativeDepsDir}
          for d in `find ${reactNativeDepsDir} -mindepth 1 -maxdepth 1 -type d`; do 
            chmod -R u+w $d
          done

          # Copy node_modules from Nix store
          rm -rf ${projectBuildDir}/node_modules
          mkdir -p ${projectBuildDir}/node_modules
          cp -a ${projectNodePackage}/node_modules/. ${projectBuildDir}/node_modules/

          # Adjust permissions
          chmod -R u+w ${projectBuildDir}

          cp -R ${projectBuildDir}/status-modules/ ${projectBuildDir}/node_modules/status-modules/
          cp -R ${projectBuildDir}/translations/ ${projectBuildDir}/node_modules/status-modules/translations/

          # Set up symlinks to mobile enviroment in project root
          ${createMobileFilesSymlinks projectBuildDir}

          # Create a dummy VERSION, since we don't want this expression to be invalidated just because the version changed
          echo '0.0.1' > ${projectBuildDir}/VERSION

          runHook postUnpack
        '';
        patchPhase = ''
          runHook prePatch

          prevSet=$-
          set -e

          patchShebangs ${projectBuildDir}

          function patchMavenSource() {
            set +e

            local targetGradleFile="$1"
            local source="$2"
            local deriv="$3"
            grep "$source" $targetGradleFile > /dev/null && \
              substituteInPlace $targetGradleFile --replace "$source" "$deriv"
          }

          function patchMavenSources() {
            set +e

            local targetGradleFile="$1"
            local deriv="$2"
            patchMavenSource $targetGradleFile 'mavenCentral()' 'mavenLocal()'
            patchMavenSource $targetGradleFile 'google()' 'mavenLocal()'
            patchMavenSource $targetGradleFile 'jcenter()' 'mavenLocal()'
            grep 'https://maven.google.com' $targetGradleFile > /dev/null && \
              substituteInPlace $targetGradleFile --replace 'https://maven.google.com' "$deriv"
            grep 'https://jitpack.io' $targetGradleFile > /dev/null && \
              substituteInPlace $targetGradleFile --replace 'https://jitpack.io' "$deriv"
          }

          # Patch maven and google central repositories with our own local directories. This prevents the builder from downloading Maven artifacts
          patchMavenSources 'android/build.gradle' '${mavenLocalRepo}'
          for f in `find ${projectBuildDir}/node_modules/ -name build.gradle`; do
            patchMavenSources $f '${mavenLocalRepo}'
          done

          # Do not add a BuildId to the generated libraries, for reproducibility
          substituteInPlace ${projectBuildDir}/node_modules/react-native/ReactAndroid/src/main/jni/Application.mk \
            --replace \
              '-Wl,--build-id' \
              '-Wl,--build-id=none'

          # Disable Gradle daemon and caching, since that causes rebuilds (and subsequently errors) anyway due to cache being considered stale
          substituteInPlace ${projectBuildDir}/android/gradle.properties \
            --replace \
              'org.gradle.jvmargs=-Xmx8704M' \
              'org.gradle.jvmargs=-Xmx8704M
          org.gradle.daemon=false
          org.gradle.caching=false'

          # Patch the path to nodejs in project.ext.react
          substituteInPlace ${projectBuildDir}/android/app/build.gradle \
            --replace \
              'nodeExecutableAndArgs: ["node"' \
              'nodeExecutableAndArgs: ["${nodejs}/bin/node"'

          # Patch dependencies which are not yet ported to AndroidX
          npx jetify

          set $prevSet

          runHook postPatch
        '';
        installPhase = ''
          rm -rf $out
          mkdir -p $out/{project,.m2/repository}

          # TODO: maybe node_modules/react-native/ReactAndroid/build/{tmp,generated} can be discarded?
          cp -R ${mavenRepoDir} $out/.m2/
          cp -R ${projectBuildDir}/{android,node_modules}/ $out/project
        '';
        fixupPhase = ''
          # Patch prepareJSC so that it doesn't subsequently try to build NDK libs
          substituteInPlace $out/project/node_modules/react-native/ReactAndroid/build.gradle \
            --replace \
              'packageReactNdkLibs(dependsOn: buildReactNdkLib, ' \
              'packageReactNdkLibs('
        '';

        # The ELF types are incompatible with the host platform, so let's not even try
        # TODO: Use Android NDK to strip binaries manually
        dontPatchELF = true;
        dontStripHost = true;

        # Take whole sources into consideration when calculating sha
        outputHashMode = "recursive";
        outputHashAlgo = "sha256";
      };

in {
  deriv = deps;

  shellHook = ''
    ${createMobileFilesSymlinks "$STATUS_REACT_HOME"}

    export STATUSREACT_NIX_MAVEN_REPO="${deps}/.m2/repository"
  '';
}
