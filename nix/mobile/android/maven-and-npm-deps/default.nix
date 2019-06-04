{ stdenv, stdenvNoCC, lib, callPackage,
  gradle, bash, file, status-go, zlib,
  projectNodePackage, androidEnvShellHook, localMavenRepoBuilder, mkFilter }:

# This script prepares a finalized version of node_modules
# (required because for Android we need to run `gradle react-native-android:installArchives`, which builds some additional native libraries
# under node_modules/react-native), as well as

let
  mavenLocalRepo = callPackage ./maven { inherit localMavenRepoBuilder; stdenv = if stdenv.isLinux then stdenv else stdenvNoCC; };

  jsc-filename = "jsc-android-236355.1.1";
  react-native-deps = callPackage ./maven/reactnative-android-native-deps.nix { inherit stdenvNoCC jsc-filename; };

  # fake build to pre-download deps into fixed-output derivation
  deps = 
    let
      projectDir = "$NIX_BUILD_TOP/project";
      mavenRepoDir = "$NIX_BUILD_TOP/.m2/repository";
      reactNativeMavenPackageDir = "${mavenRepoDir}/com/facebook/react/react-native"; # This is directory where the react-native Maven package will be generated in
      reactNativeDepsDir = "$NIX_BUILD_TOP/deps"; # Use local writable deps, otherwise (probably due to some interaction between Nix sandboxing and Java) gradle will fail copying directly from the nix store
    in 
      stdenv.mkDerivation {
        name = "gradle-install-android-archives-and-patched-npm-modules";
        src =
          let path = ./../../../..; # Import the root /android and /mobile_files folders clean of any build artifacts
          in builtins.path { # We use builtins.path so that we can name the resulting derivation, otherwise the name would be taken from the checkout directory, which is outside of our control
            inherit path;
            name = "status-react-gradle-install-source";
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
        nativeBuildInputs = [ projectNodePackage ];
        buildInputs = [ gradle bash file zlib mavenLocalRepo ];
        propagatedBuildInputs = [ react-native-deps ] ++ status-go.buildInputs;
        unpackPhase = ''
          runHook preUnpack

          # Copy project directory
          mkdir -p ${projectDir}
          cp -a $src/. ${projectDir}
          chmod u+w ${projectDir}
          cd ${projectDir}

          # Copy fresh RN maven dependencies and make them writable, otherwise Gradle copy fails (since the top-level directory is read-only, Java isn't smart enough to copy the child files/folders into that target directory)
          mkdir -p ${mavenRepoDir}
          cp -a ${mavenLocalRepo}/. ${mavenRepoDir}
          mkdir -p ${reactNativeMavenPackageDir}
          chmod u+w ${reactNativeMavenPackageDir}/
          cp -a ${react-native-deps}/deps ${reactNativeDepsDir}
          for d in `find ${reactNativeDepsDir} -mindepth 1 -maxdepth 1 -type d`; do 
            chmod -R u+w $d
          done

          # Copy fresh node_modules
          rm -rf ${projectDir}/node_modules
          mkdir -p ${projectDir}/node_modules
          cp -aL --copy-contents ${projectNodePackage}/libexec/${projectNodePackage.passthru.pname}/node_modules/. ${projectDir}/node_modules/
          chmod -R u+w ${projectDir}/node_modules
          cp -aL --copy-contents ${projectNodePackage}/libexec/${projectNodePackage.passthru.pname}/deps/${projectNodePackage.passthru.pname}/node_modules/. ${projectDir}/node_modules/
          chmod u+w ${projectDir}/node_modules

          # Adjust permissions
          chmod -R u+w ${projectDir}

          cp -R ${projectDir}/status-modules/ ${projectDir}/node_modules/status-modules/
          cp -R ${projectDir}/translations/ ${projectDir}/node_modules/status-modules/translations/

          # Set up symlinks to mobile enviroment in project root 
          ln -sf ${projectDir}/mobile_files/package.json.orig ${projectDir}/package.json
          ln -sf ${projectDir}/mobile_files/metro.config.js ${projectDir}/metro.config.js
          ln -sf ${projectDir}/mobile_files/yarn.lock ${projectDir}/yarn.lock

          # Create a dummy VERSION, since we don't want this expression to be invalidated just because the version changed
          echo '0.0.1' > ${projectDir}/VERSION

          runHook postUnpack
        '';
        patchPhase = ''
          runHook prePatch

          prevSet=$-
          set -e

          patchShebangs ${projectDir}

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
          for f in `find node_modules/ -name build.gradle`; do
            patchMavenSources $f '${mavenLocalRepo}'
          done

          # Patch prepareJSC so that it doesn't try to download from registry
          substituteInPlace node_modules/react-native/ReactAndroid/build.gradle \
            --replace 'prepareJSC(dependsOn: downloadJSC)' 'prepareJSC(dependsOn: createNativeDepsDirectories)' \
            --replace 'def jscTar = tarTree(downloadJSC.dest)' "def jscTar = tarTree(new File(\"${react-native-deps}/deps/${jsc-filename}.tar.gz\"))"

          # We don't want to include the scripts directory, as this would invalidate the Nix cache build every time an unrelated script changed. In any case, the version shouldn't matter for this build.
          substituteInPlace android/app/build.gradle \
            --replace 'versionCode getVersionCode()' 'versionCode 9999'

          # Do not add a BuildId to the generated libraries, for reproducibility
          substituteInPlace ${projectDir}/node_modules/react-native/ReactAndroid/src/main/jni/Application.mk \
            --replace '-Wl,--build-id' '-Wl,--build-id=none'

          # Disable Gradle daemon and caching, since that causes rebuilds (and subsequently errors) anyway due to cache being considered stale
          substituteInPlace android/gradle.properties \
            --replace 'org.gradle.jvmargs=-Xmx8704M' 'org.gradle.jvmargs=-Xmx8704M
          org.gradle.daemon=false
          org.gradle.caching=false'

          set $prevSet

          runHook postPatch
        '';
        buildPhase =
          androidEnvShellHook +
          status-go.shellHook + ''
          export HOME=$NIX_BUILD_TOP
          export REACT_NATIVE_DEPENDENCIES="${reactNativeDepsDir}"
          export STATUS_REACT_HOME="${projectDir}"

          pushd ${projectDir}/android
          # NOTE: This generates the react-native-android binaries under node_modules/react-native/android
          LD_LIBRARY_PATH=$LD_LIBRARY_PATH:${lib.makeLibraryPath [ zlib ]} \
            gradle -Dmaven.repo.local=${mavenRepoDir} --offline -S react-native-android:installArchives || exit
          popd > /dev/null
        '';
        doCheck = true;
        checkPhase = ''
          runHook preCheck
          test -d ${projectDir}/node_modules/react-native/ReactAndroid/build/intermediates/javac/release/compileReleaseJavaWithJavac/classes/com/facebook || \
            exit 1
          test -d ${projectDir}/node_modules/react-native/ReactAndroid/build/react-ndk/exported || \
            exit 2
          runHook postCheck
        '';
        installPhase = ''
          rm -rf $out
          mkdir -p $out/project $out/.m2/repository

          # TODO: maybe node_modules/react-native/ReactAndroid/build/{tmp,generated} can be discarded?
          cp -R ${mavenRepoDir} $out/.m2/
          cp -R ${projectDir}/android/ ${projectDir}/node_modules/ $out/project
        '';
        fixupPhase = ''
          # Patch prepareJSC so that it doesn't subsequently try to build NDK libs
          substituteInPlace $out/project/node_modules/react-native/ReactAndroid/build.gradle \
            --replace 'packageReactNdkLibs(dependsOn: buildReactNdkLib, ' 'packageReactNdkLibs('
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
  buildInputs = deps;

  shellHook = ''
    # This avoids RN trying to download dependencies. Maybe we need to wrap this in a special RN environment derivation
    ln -sf $STATUS_REACT_HOME/mobile_files/package.json.orig $STATUS_REACT_HOME/package.json
    ln -sf $STATUS_REACT_HOME/mobile_files/metro.config.js $STATUS_REACT_HOME/metro.config.js
    ln -sf $STATUS_REACT_HOME/mobile_files/yarn.lock $STATUS_REACT_HOME/yarn.lock

    export PATH="$STATUS_REACT_HOME/node_modules/.bin:$PATH"
    export STATUSREACT_NIX_MAVEN_REPO="${deps}/.m2/repository"
  '';
}
