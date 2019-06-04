#!/usr/bin/env bash

set -e

VERBOSE_LEVEL=${VERBOSE_LEVEL:-1}

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
OS=$(uname -s)
if [ -z "$TARGET_OS" ]; then
  TARGET_OS=$(uname -s | tr '[:upper:]' '[:lower:]')
fi
WINDOWS_CROSSTOOLCHAIN_PKG_NAME='mxetoolchain-x86_64-w64-mingw32'

external_modules_dir=( \
  'node_modules/react-native-languages/desktop' \
  'node_modules/react-native-config/desktop' \
  'node_modules/react-native-fs/desktop' \
  'node_modules/react-native-http-bridge/desktop' \
  'node_modules/react-native-webview-bridge/desktop' \
  'node_modules/react-native-keychain/desktop' \
  'node_modules/react-native-securerandom/desktop' \
  'modules/react-native-status/desktop' \
  'node_modules/google-breakpad' \
  'modules/react-native-desktop-linking/desktop' \
  'modules/react-native-desktop-menu/desktop' \
  'modules/react-native-desktop-config/desktop' \
  'modules/react-native-desktop-shortcuts/desktop' \
  'modules/react-native-desktop-notification/desktop' \
)

external_fonts=( \
  '../../../../../resources/fonts/Inter-Bold.otf' \
  '../../../../../resources/fonts/Inter-Medium.otf' \
  '../../../../../resources/fonts/Inter-Regular.otf' \
)

source "$SCRIPTPATH/lib/setup/path-support.sh"

source_lib "packages.sh"

function is_macos() {
  [[ "$OS" =~ Darwin ]]
}

function is_linux() {
  [[ "$OS" =~ Linux ]]
}

function is_windows_target() {
  [[ "$TARGET_OS" =~ windows ]]
}

function joinPath() {
  if program_exists 'realpath'; then
    realpath -m "$1/$2" 2> /dev/null
  else
    echo "$1/$2" | tr -s /
  fi
}

function joinExistingPath() {
  if program_exists 'realpath'; then
    realpath "$1/$2" 2> /dev/null
  else
    echo "$1/$2" | tr -s /
  fi
}

function join { local IFS="$1"; shift; echo "$*"; }

CMAKE_EXTRA_FLAGS="-DCMAKE_CXX_FLAGS:='-DBUILD_FOR_BUNDLE=1'"
[ -n $STATUS_NO_LOGGING ] && CMAKE_EXTRA_FLAGS="$CMAKE_EXTRA_FLAGS -DSTATUS_NO_LOGGING=1"
if is_windows_target; then
  CMAKE_EXTRA_FLAGS="$CMAKE_EXTRA_FLAGS -DCMAKE_TOOLCHAIN_FILE='Toolchain-Ubuntu-mingw64.cmake'"
  CMAKE_EXTRA_FLAGS="$CMAKE_EXTRA_FLAGS -DCMAKE_C_COMPILER='x86_64-w64-mingw32.shared-gcc'"
  CMAKE_EXTRA_FLAGS="$CMAKE_EXTRA_FLAGS -DCMAKE_CXX_COMPILER='x86_64-w64-mingw32.shared-g++'"
  CMAKE_EXTRA_FLAGS="$CMAKE_EXTRA_FLAGS -DCMAKE_RC_COMPILER='x86_64-w64-mingw32.shared-windres'"
fi

STATUSREACTPATH="$(cd "$SCRIPTPATH" && cd '..' && pwd)"
WORKFOLDER="$(joinExistingPath "$STATUSREACTPATH" 'StatusImPackage')"

function init() {
  if [ -z $STATUSREACTPATH ]; then
    echo "${RED}STATUSREACTPATH environment variable is not defined!${NC}"
    exit 1
  fi

  if ! is_windows_target; then
    if [ -z $QT_PATH ]; then
      echo "${RED}QT_PATH environment variable is not defined!${NC}"
      exit 1
    fi
  fi

  if is_linux; then
    rm -rf ./desktop/toolchain/
    # TODO: Use Conan for Linux and MacOS builds too
    if is_windows_target; then
      export PATH=$STATUSREACTPATH:$PATH
      if ! program_exists 'conan'; then
        echo "${RED}Conan package manager not found. Exiting...${NC}"
        exit 1
      fi

      conan remote add --insert 0 -f status-im https://conan.status.im

      echo "Generating cross-toolchain profile..."
      conan install -if ./desktop/toolchain/ -g json $WINDOWS_CROSSTOOLCHAIN_PKG_NAME/5.5.0-1@status-im/stable \
        -pr ./node_modules/status-conan/profiles/status-mingw32-x86_64
      python3 ./node_modules/status-conan/profiles/generate-profiles.py ./node_modules/status-conan/profiles ./desktop/toolchain/conanbuildinfo.json

      echo "Installing cross-toolchain..."
      conan install -if ./desktop/toolchain/ -g json -g cmake $WINDOWS_CROSSTOOLCHAIN_PKG_NAME/5.5.0-1@status-im/stable \
        -pr ./node_modules/status-conan/profiles/status-mxe-mingw32-x86_64-gcc55-libstdcxx
    fi
  fi
}

function joinStrings() {
  local arr=("$@")
  printf -v var "%s;" "${arr[@]}"
  var=${var%?}
  echo ${var[@]}
}

function buildClojureScript() {
  # create directory for all work related to bundling
  rm -rf $WORKFOLDER
  mkdir -p $WORKFOLDER
  echo -e "${GREEN}Work folder created: $WORKFOLDER${NC}"
  echo ""

  # from index.desktop.js create javascript bundle and resources folder
  echo "Generating Status.jsbundle and assets folder..."
  react-native bundle --entry-file index.desktop.js --bundle-output "$WORKFOLDER/Status.jsbundle" \
                      --dev false --platform desktop --assets-dest "$WORKFOLDER/assets"
  echo -e "${GREEN}Generating done.${NC}"
  echo ""
}

function compile() {
  # Temporarily add path to javascript bundle to package.json
  local JS_BUNDLE_PATH="$WORKFOLDER/Status.jsbundle"
  local jsBundleLine="\"desktopJSBundlePath\": \"$JS_BUNDLE_PATH\""
  local jsPackagePath=$(joinExistingPath "$STATUSREACTPATH" 'desktop_files/package.json.orig')
  local tmp=$(mktemp)
  jq ".=(. + {$jsBundleLine})" "$jsPackagePath" > "$tmp" && mv "$tmp" "$jsPackagePath"
  echo -e "${YELLOW}Added 'desktopJSBundlePath' line to $jsPackagePath:${NC}"
  echo ""

  local EXTERNAL_MODULES_DIR="$(joinStrings ${external_modules_dir[@]})"
  local DESKTOP_FONTS="$(joinStrings ${external_fonts[@]})"
  pushd desktop
    rm -rf CMakeFiles CMakeCache.txt cmake_install.cmake Makefile modules reportApp/CMakeFiles desktop/node_modules/google-breakpad/CMakeFiles desktop/node_modules/react-native-keychain/desktop/qtkeychain-prefix/src/qtkeychain-build/CMakeFiles desktop/node_modules/react-native-keychain/desktop/qtkeychain
    if is_windows_target; then
      export PATH=$STATUSREACTPATH:$PATH

      # Get the toolchain bin folder from toolchain/conanbuildinfo.json
      local bin_dirs=$(jq -r '.dependencies[0].bin_paths | .[]' toolchain/conanbuildinfo.json)
      while read -r bin_dir; do
        if [ ! -d $bin ]; then
          echo -e "${RED}Could not find $bin_dir directory from 'toolchain/conanbuildinfo.json', aborting${NC}"
          exit 1
        fi
        export PATH=$bin_dir:$PATH
      done <<< "$bin_dirs"
    fi
    cmake -Wno-dev \
          $CMAKE_EXTRA_FLAGS \
          -DCMAKE_BUILD_TYPE=Release \
          -DEXTERNAL_MODULES_DIR="$EXTERNAL_MODULES_DIR" \
          -DDESKTOP_FONTS="$DESKTOP_FONTS" \
          -DJS_BUNDLE_PATH="$JS_BUNDLE_PATH" || exit 1
    make -S -j5 || exit 1
  popd

  git checkout $jsPackagePath # remove the bundle from the package.json file
}

function bundleWindows() {
  local buildType="$1"

  local version_file="${STATUSREACTPATH}/VERSION"
  VERSION=$(cat $version_file)
  if [ -z "$VERSION" ]; then
    echo "${RED}Could not read version from ${version_file}!${NC}"
    exit 1
  fi

  pushd $STATUSREACTPATH/desktop/bin
    rm -rf cmake_install.cmake Makefile CMakeFiles Status_autogen
  popd

  local compressionAlgo="lzma"
  local compressionType="/SOLID"
  if [ -z $buildType ]; then
    compressionAlgo="bzip2"
    compressionType=""
  elif [ "$buildType" = "pr" ]; then
    compressionAlgo="zlib"
  fi

  # TODO this needs to be fixed: status-react/issues/5378
  local top_srcdir=$(joinExistingPath "$STATUSREACTPATH" '.')
  VERSION_MAJOR="$(cut -d'.' -f1 <<<"$VERSION")"
  VERSION_MINOR="$(cut -d'.' -f2 <<<"$VERSION")"
  VERSION_BUILD="$(cut -d'.' -f3 <<<"$VERSION")"
  makensis -Dtop_srcdir=${top_srcdir} \
           -Dbase_image_dir=${STATUSREACT_WINDOWS_BASEIMAGE_PATH} \
           -DCOMPRESSION_ALGO=${compressionAlgo} \
           -DCOMPRESSION_TYPE=${compressionType} \
           -DVERSION_MAJOR=$VERSION_MAJOR \
           -DVERSION_MINOR=$VERSION_MINOR \
           -DVERSION_BUILD=$VERSION_BUILD \
           -DPUBLISHER=Status.im \
           -DWEBSITE_URL="https://status.im/" \
           ./deployment/windows/nsis/setup.nsi
}

function bundleLinux() {
  local QTBIN=$(joinExistingPath "$QT_PATH" 'gcc_64/bin')
  if [ ! -d "$QTBIN" ]; then
    # CI environment doesn't contain gcc_64 path component
    QTBIN=$(joinExistingPath "$QT_PATH" 'bin')
  fi

  echo "Creating AppImage..."
  pushd $WORKFOLDER
    rm -rf StatusImAppImage AppDir

    # TODO this needs to be fixed: status-react/issues/5378
    cp -r ${STATUSREACT_LINUX_BASEIMAGE_PATH}/StatusImAppImage .
    chmod -R +w StatusImAppImage/

    mkdir AppDir
  popd

  # invoke linuxdeployqt to create Status.AppImage
  local qmakePath="$(joinExistingPath "${QTBIN}" 'qmake')"
  local usrBinPath="$(joinPath "$WORKFOLDER" "AppDir/usr/bin")"
  cp -r ./deployment/linux/usr $WORKFOLDER/AppDir
  cp ./.env $usrBinPath
  cp ./desktop/bin/Status ./desktop/bin/reportApp $usrBinPath

  rm -f Application-x86_64.AppImage Status-x86_64.AppImage

  [ $VERBOSE_LEVEL -ge 1 ] && ldd $(joinExistingPath "$usrBinPath" 'Status')
  pushd $WORKFOLDER
    cp -r assets/share/assets $usrBinPath
    cp -rf StatusImAppImage/* $usrBinPath
    rm -f $usrBinPath/Status.AppImage
  popd

  local desktopFilePath="$(joinExistingPath "$WORKFOLDER" 'AppDir/usr/share/applications/Status.desktop')"
  linuxdeployqt \
    $desktopFilePath \
    -verbose=$VERBOSE_LEVEL -always-overwrite -no-strip \
    -no-translations -bundle-non-qt-libs \
    -qmake="$qmakePath" \
    -executable="$(joinExistingPath "$usrBinPath" 'reportApp')" \
    -qmldir="$(joinExistingPath "$STATUSREACTPATH" 'node_modules/react-native')" \
    -qmldir="$(joinExistingPath "$STATUSREACTPATH" 'desktop/reportApp')" \
    -extra-plugins=imageformats/libqsvg.so

  pushd $WORKFOLDER
    rm -f $usrBinPath/Status.AppImage

    # Patch libraries and executables to remove references to /nix/store
    set +e
    for f in `find ./AppDir/usr/lib/*`; do
      patchelf --set-interpreter /lib64/ld-linux-x86-64.so.2 $f 2> /dev/null
      patchelf --set-rpath "\$ORIGIN" $f
    done
    set -e
    for f in $usrBinPath/Status $usrBinPath/reportApp; do
      patchelf --set-interpreter /lib64/ld-linux-x86-64.so.2 --set-rpath "\$ORIGIN:\$ORIGIN/../lib" $f
    done
    # To make the output more reproducible, always set the timestamps to the same value
    for f in `find ./AppDir`; do
      touch --no-create -h -t 197001010000.00 $f
    done
    [ $VERBOSE_LEVEL -ge 1 ] && ldd $usrBinPath/Status

    appimagetool ./AppDir
    # Ensure the AppImage itself isn't using the interpreter in Nix's store
    patchelf --set-interpreter /lib64/ld-linux-x86-64.so.2 --set-rpath "\$ORIGIN" ./Status-x86_64.AppImage
    chmod +x ./Status-x86_64.AppImage
    rm -rf Status.AppImage
    mv -f ./Status-x86_64.AppImage ..
  popd

  echo -e "${GREEN}Package ready in ./Status-x86_64.AppImage!${NC}"
  echo ""
}

if is_macos; then
  function copyDylibNixDependenciesToPackage() {
    local dylib="$1"
    local contentsDir="$2"
    local frameworksDir="$contentsDir/Frameworks"
    local exeDir="$contentsDir/MacOS"

    # Walk through the dependencies of $dylib
    local dependencies=$(otool -L "$dylib" | grep -E "\s+/nix/" | awk -F "(" '{print $1}' | xargs)
    local moduleDirPath=$(basename $dylib)
    for depDylib in $dependencies; do
      local targetDepDylib=$(joinPath "$frameworksDir" "$(basename $depDylib)")
      # Copy any dependencies that: are not in the Frameworks directory, do not already exist in /usr/lib and are not a Qt5 module (will be handled by macdeployqt anyway)
      if [ ! -f "$targetDepDylib" ] && [[ "$(basename $targetDepDylib)" != "libQt5"* ]] && [ ! -f "/usr/lib/$(basename $depDylib)" ]; then
        [ $VERBOSE_LEVEL -ge 1 ] && echo "  Copying $depDylib to $frameworksDir..."
        cp -a -L "$depDylib" "$frameworksDir"
        chmod 0755 "$targetDepDylib"

        copyDylibNixDependenciesToPackage "$depDylib" "$contentsDir"
      fi
    done
  }

  function copyQtPlugInToPackage() {
    local qtPath="$1"
    local pluginName="$2"
    local contentsPath="$3"
    local filter=""
    local targetPath="$contentsPath/PlugIns"
    local pluginTargetPath="$targetPath/$pluginName"

    [ "$pluginName" == 'platforms' ] && filter='libqcocoa.dylib'

    mkdir -p $pluginTargetPath
    local qtLibPath=$(find $qtPath/lib -maxdepth 1 -name qt-*)
    local srcPath=$(readlink -f "$qtLibPath/plugins/$pluginName")
    echo "Copying $srcPath to $targetPath"
    if [ -z "$filter" ]; then
      cp -a -f -L "$srcPath" "$targetPath"
    else
      cp -f $(readlink -f "$srcPath/$filter") "$pluginTargetPath"
    fi
    chmod 755 $pluginTargetPath
    chmod 755 $pluginTargetPath/*

    for dylib in `find $pluginTargetPath -name *.dylib`; do
      copyDylibNixDependenciesToPackage "$dylib" "$contentsPath"
    done
  }

  function fixupRPathsInDylib() {
    local dylib="$1"
    local contentsDir="$2"
    local frameworksDir="$contentsDir/Frameworks"
    local exeDir="$contentsDir/MacOS"

    [ $VERBOSE_LEVEL -ge 1 ] && echo "Checking rpaths in ${dylib}"
  
    # Walk through the dependencies of $dylib
    local dependencies=$(otool -L "$dylib" | grep -E "\s+/nix/" | sed "s|@executable_path|$exeDir|" | awk -F "(" '{print $1}' | xargs)
    local moduleDirPath=$(dirname $dylib)
    for depDylib in $dependencies; do
      # Fix rpath and copy library to target
      local replacementTargetPath=""
      local framework=$(echo $depDylib | sed -E "s|^\/nix\/.+\/Library\/Frameworks\/(.+)\.framework\/\1$|\1|" 2> /dev/null)
      if [ -n "$framework" ] && [ "$framework" != "$depDylib" ]; then
        # Handle macOS framework
        local targetDepDylib=$(joinExistingPath "/System/Library/Frameworks" "${framework}.framework/${framework}")

        if [ ! -f "$targetDepDylib" ]; then
          echo -e "${RED}FATAL: system framework not found: ${targetDepDylib}${NC}"
          exit 1
        fi

        # Change dependency rpath in $dylib to point to $targetDepDylib
        replacementTargetPath=$targetDepDylib
      else
        # Handle other libraries
        local targetDepDylib=$(joinPath "$frameworksDir" "$(basename $depDylib)")

        if [ ! -f "$targetDepDylib" ]; then
          echo -e "${RED}FATAL: macdeployqt should have copied the dependency to ${targetDepDylib}${NC}"
          exit 1
        fi

        # Change dependency rpath in $dylib to point to $replacementTargetPath
        local replacementPath=""
        local targetDepModuleDirPath=$(dirname $targetDepDylib)
        if [[ $targetDepModuleDirPath -ef $moduleDirPath ]]; then
          replacementPath="@loader_path"
        else
          replacementPath="@executable_path/$(realpath --relative-to="$exeDir" "$targetDepModuleDirPath")"
        fi
        local modulePathRegExp="($(pwd)/)?$moduleDirPath"
        replacementTargetPath=$(echo $targetDepDylib | sed -E "s|$modulePathRegExp|$replacementPath|")
      fi

      if [ -n "$replacementTargetPath" ]; then
        [ $VERBOSE_LEVEL -ge 1 ] && echo "Updating $dylib to point to $replacementTargetPath"
        install_name_tool -change "$depDylib" "$replacementTargetPath" "$dylib"
      fi
    done
  }

  function fixupRemainingRPaths() {
    local searchRootPath="$1"
    local contentsDir="$2"

    for dylib in `find $searchRootPath -name *.dylib`; do
      fixupRPathsInDylib "$dylib" "$contentsDir"

      # Sanity check for absolute paths
      local dependencies=$(otool -L "$dylib" | grep -E "\s+${STATUSREACTPATH}")
      if [ -n "$dependencies" ]; then
        echo "Absolute path detected in dependencies of $dylib. Aborting..."
        echo "${dependencies[@]}"
        exit 1
      fi
    done
  }
fi

function bundleMacOS() {
  pushd $WORKFOLDER
    # download prepared package with mac bundle files (it contains qt libraries, icon)
    rm -rf Status.app
    # TODO this needs to be fixed: status-react/issues/5378
    cp -r ${STATUSREACT_MACOS_BASEIMAGE_PATH}/Status.app .
    chmod -R +w Status.app/

    local contentsPath='Status.app/Contents'
    local usrBinPath=$(joinExistingPath "$WORKFOLDER" "$contentsPath/MacOS")

    cp -r assets/share/assets $contentsPath/Resources
    ln -sf ../Resources/assets ../Resources/ubuntu-server ../Resources/node_modules $usrBinPath
    chmod +x $contentsPath/Resources/ubuntu-server
    cp ../desktop/bin/Status $usrBinPath/Status
    cp ../desktop/bin/reportApp $usrBinPath
    cp ../.env $contentsPath/Resources
    ln -sf ../Resources/.env $usrBinPath/.env
    cp -f ../deployment/macos/qt-reportApp.conf $contentsPath/Resources
    ln -sf ../Resources/qt-reportApp.conf $usrBinPath/qt.conf
    cp -f ../deployment/macos/Info.plist $contentsPath
    cp -f ../deployment/macos/status-icon.icns $contentsPath/Resources

    local qtbaseplugins=(bearer platforms printsupport styles)
    local qtfullplugins=(iconengines imageformats webview)
    if [ -n "$IN_NIX_SHELL" ]; then
      # Since in the Nix qt.full package the different Qt modules are spread across several directories,
      # macdeployqt cannot find some qtbase plugins, so we copy them in its place
      mkdir -p "$contentsPath/PlugIns"
      for plugin in ${qtbaseplugins[@]}; do copyQtPlugInToPackage "$QT_BASEBIN_PATH" "$plugin" "$contentsPath"; done
      for plugin in ${qtfullplugins[@]}; do copyQtPlugInToPackage "$QT_PATH" "$plugin" "$contentsPath"; done
    fi

    macdeployqt Status.app \
      -verbose=$VERBOSE_LEVEL \
      -executable="$(joinExistingPath "$usrBinPath" 'reportApp')" \
      -qmldir="$(joinExistingPath "$STATUSREACTPATH" 'node_modules/react-native')" \
      -qmldir="$(joinExistingPath "$STATUSREACTPATH" 'desktop/reportApp')"

    # macdeployqt doesn't fix rpaths for all the libraries (although it copies them all), so we'll just walk through them and update rpaths to not point to /nix
    echo "Fixing remaining rpaths in modules..."
    fixupRemainingRPaths "$contentsPath/Frameworks" "$contentsPath"
    fixupRemainingRPaths "$contentsPath/PlugIns" "$contentsPath"
    echo "Done fixing rpaths in modules"
    rm -f Status.app.zip
  popd

  echo -e "${GREEN}Package ready in $WORKFOLDER/Status.app!${NC}"
  echo ""
}

function bundle() {
  if is_macos; then
    bundleMacOS
  elif is_linux; then
    if is_windows_target; then
      bundleWindows
    else
      bundleLinux
    fi
  fi
}

init

if [ -z "$@" ]; then
  buildClojureScript
  compile
  bundle
else
  "$@"
fi
