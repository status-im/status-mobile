#!/bin/bash

set -e

VERBOSE_LEVEL=${VERBOSE_LEVEL:-1}

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
OS=$(uname -s)
if [ -z $TARGET_SYSTEM_NAME ]; then
  TARGET_SYSTEM_NAME=$OS
fi
WINDOWS_CROSSTOOLCHAIN_PKG_NAME='mxetoolchain-x86_64-w64-mingw32'

external_modules_dir=( \
  'node_modules/react-native-i18n/desktop' \
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
  'modules/react-native-desktop-notification/desktop' \
)

external_fonts=( \
  '../../../../../resources/fonts/Inter-UI-Bold.otf' \
  '../../../../../resources/fonts/Inter-UI-Medium.otf' \
  '../../../../../resources/fonts/Inter-UI-Regular.otf' \
)

function is_macos() {
  [[ "$OS" =~ Darwin ]]
}

function is_linux() {
  [[ "$OS" =~ Linux ]]
}

function is_windows_target() {
  [[ "$TARGET_SYSTEM_NAME" =~ Windows ]]
}

function program_exists() {
  local program=$1
  command -v "$program" >/dev/null 2>&1
}

function joinPath() {
  if program_exists 'realpath'; then
    realpath -m "$1/$2"
  else
    echo "$1/$2" | tr -s /
  fi
}

function joinExistingPath() {
  if program_exists 'realpath'; then
    realpath "$1/$2"
  else
    echo "$1/$2" | tr -s /
  fi
}

STATUSREACTPATH="$(cd "$SCRIPTPATH" && cd '..' && pwd)"
WORKFOLDER="$(joinExistingPath "$STATUSREACTPATH" 'StatusImPackage')"
DEPLOYQT="$(joinPath . 'linuxdeployqt-continuous-x86_64.AppImage')"
APPIMAGETOOL="$(joinPath . 'appimagetool-x86_64.AppImage')"
STATUSIM_APPIMAGE_ARCHIVE="StatusImAppImage_20181208.zip"

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

  if is_macos; then
    if [ -z $MACDEPLOYQT ]; then
      set +e
      MACDEPLOYQT=$(which macdeployqt)
      if [ -z $MACDEPLOYQT ]; then
        echo "${RED}MACDEPLOYQT environment variable is not defined and macdeployqt executable not found in path!${NC}"
        exit 1
      fi
      set -e
    fi

    DEPLOYQT="$MACDEPLOYQT"
  elif is_linux; then
    rm -rf ./desktop/toolchain/
    # TODO: Use Conan for Linux and MacOS builds too
    if is_windows_target; then
      if ! program_exists 'python3'; then
        echo "${RED}python3 prerequisite is missing. Exiting.${NC}"
        exit 1
      fi

      export PATH=$STATUSREACTPATH:$PATH
      if ! program_exists 'conan'; then
        if ! program_exists 'pip3'; then
          echo "${RED}pip3 package manager not found. Exiting.${NC}"
          exit 1
        fi

        echo "${RED}Conan package manager not found. Installing...${NC}"
        pip3 install conan==1.9.0
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

  # Add path to javascript bundle to package.json
  jsBundleLine="\"desktopJSBundlePath\": \"$WORKFOLDER/Status.jsbundle\""
  jsPackagePath=$(joinExistingPath "$STATUSREACTPATH" 'desktop_files/package.json.orig')
  if grep -Fq "$jsBundleLine" "$jsPackagePath"; then
    echo -e "${GREEN}Found line in package.json.${NC}"
  else
    # Add line to package.json just before "dependencies" line
    if is_macos; then
      sed -i '' -e "/\"dependencies\":/i\\
 \  $jsBundleLine," "$jsPackagePath"
    else
      sed -i -- "/\"dependencies\":/i\  $jsBundleLine," "$jsPackagePath"
    fi
    echo -e "${YELLOW}Added 'desktopJSBundlePath' line to $jsPackagePath:${NC}"
    echo ""
  fi
}

function compile() {
  pushd desktop
    rm -rf CMakeFiles CMakeCache.txt cmake_install.cmake Makefile modules reportApp/CMakeFiles desktop/node_modules/google-breakpad/CMakeFiles desktop/node_modules/react-native-keychain/desktop/qtkeychain-prefix/src/qtkeychain-build/CMakeFiles desktop/node_modules/react-native-keychain/desktop/qtkeychain
    EXTERNAL_MODULES_DIR="$(joinStrings ${external_modules_dir[@]})"
    DESKTOP_FONTS="$(joinStrings ${external_fonts[@]})"
    JS_BUNDLE_PATH="$WORKFOLDER/Status.jsbundle"
    if is_windows_target; then
      export PATH=$STATUSREACTPATH:$PATH

      # Get the toolchain bin folder from toolchain/conanbuildinfo.json
      bin_dirs=$(jq -r '.dependencies[0].bin_paths | .[]' toolchain/conanbuildinfo.json)
      while read -r bin_dir; do
        if [ ! -d $bin ]; then
          echo -e "${RED}Could not find $bin_dir directory from 'toolchain/conanbuildinfo.json', aborting${NC}"
          exit 1
        fi
        export PATH=$bin_dir:$PATH
      done <<< "$bin_dirs"
      cmake -Wno-dev \
            -DCMAKE_TOOLCHAIN_FILE='Toolchain-Ubuntu-mingw64.cmake' \
            -DCMAKE_C_COMPILER='x86_64-w64-mingw32.shared-gcc' \
            -DCMAKE_CXX_COMPILER='x86_64-w64-mingw32.shared-g++' \
            -DCMAKE_RC_COMPILER='x86_64-w64-mingw32.shared-windres' \
            -DCMAKE_BUILD_TYPE=Release \
            -DEXTERNAL_MODULES_DIR="$EXTERNAL_MODULES_DIR" \
            -DDESKTOP_FONTS="$DESKTOP_FONTS" \
            -DJS_BUNDLE_PATH="$JS_BUNDLE_PATH" \
            -DCMAKE_CXX_FLAGS:='-DBUILD_FOR_BUNDLE=1' || exit 1
    else
      cmake -Wno-dev \
            -DCMAKE_BUILD_TYPE=Release \
            -DEXTERNAL_MODULES_DIR="$EXTERNAL_MODULES_DIR" \
            -DDESKTOP_FONTS="$DESKTOP_FONTS" \
            -DJS_BUNDLE_PATH="$JS_BUNDLE_PATH" \
            -DCMAKE_CXX_FLAGS:='-DBUILD_FOR_BUNDLE=1' || exit 1
    fi
    make -S -j5 || exit 1
  popd
}

function bundleWindows() {
  local buildType="$1"

  local version_file="${STATUSREACTPATH}/desktop_files/VERSION"
  VERSION=$(cat $version_file)
  if [ -z "$VERSION" ]; then
    echo "${RED}Could not read version from ${version_file}!${NC}"
    exit 1
  fi

  pushd $WORKFOLDER
    rm -rf Windows
    mkdir Windows

    if [ -z $STATUSIM_WINDOWS_BASEIMAGE_ZIP ]; then
      STATUSIM_WINDOWS_BASEIMAGE_ZIP=./StatusIm-Windows-base-image.zip
      [ -f $STATUSIM_WINDOWS_BASEIMAGE_ZIP ] || wget https://desktop-app-files.ams3.digitaloceanspaces.com/StatusIm-Windows-base-image_20181113.zip -O StatusIm-Windows-base-image.zip
    fi
    unzip "$STATUSIM_WINDOWS_BASEIMAGE_ZIP" -d Windows/

    pushd $STATUSREACTPATH/desktop/bin
      rm -rf cmake_install.cmake Makefile CMakeFiles Status_autogen
    popd
  popd

  local compressionAlgo="lzma"
  local compressionType="/SOLID"
  if [ -z $buildType ]; then
    compressionAlgo="bzip2"
    compressionType=""
  elif [ "$buildType" = "pr" ]; then
    compressionAlgo="zlib"
  fi

  local top_srcdir=$(joinExistingPath "$STATUSREACTPATH" '.')
  VERSION_MAJOR="$(cut -d'.' -f1 <<<"$VERSION")"
  VERSION_MINOR="$(cut -d'.' -f2 <<<"$VERSION")"
  VERSION_BUILD="$(cut -d'.' -f3 <<<"$VERSION")"
  makensis -Dtop_srcdir=${top_srcdir} \
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

  # invoke linuxdeployqt to create Status.AppImage
  echo "Creating AppImage..."
  pushd $WORKFOLDER
    rm -rf StatusImAppImage*
    # TODO this needs to be fixed: status-react/issues/5378
    if [ -z $STATUSIM_APPIMAGE_DIR ]; then
      STATUSIM_APPIMAGE="./${STATUSIM_APPIMAGE_ARCHIVE}"
    else
      STATUSIM_APPIMAGE="${STATUSIM_APPIMAGE_DIR}/${STATUSIM_APPIMAGE_ARCHIVE}"
    fi
    [ -f $STATUSIM_APPIMAGE ] || wget "https://desktop-app-files.ams3.digitaloceanspaces.com/${STATUSIM_APPIMAGE_ARCHIVE}" -O $STATUSIM_APPIMAGE
    unzip "$STATUSIM_APPIMAGE" -d .
    rm -rf AppDir
    mkdir AppDir
  popd

  qmakePath="$(joinExistingPath "${QTBIN}" 'qmake')"
  usrBinPath=$(joinPath "$WORKFOLDER" "AppDir/usr/bin")
  cp -r ./deployment/linux/usr $WORKFOLDER/AppDir
  cp ./.env $usrBinPath
  cp ./desktop/bin/Status $usrBinPath
  cp ./desktop/bin/reportApp $usrBinPath
  
  if [ ! -f $DEPLOYQT ]; then
    wget --output-document="$DEPLOYQT" --show-progress -q https://github.com/probonopd/linuxdeployqt/releases/download/continuous/linuxdeployqt-continuous-x86_64.AppImage
    chmod a+x $DEPLOYQT
  fi

  if [ ! -f $APPIMAGETOOL ]; then
    wget --output-document="$APPIMAGETOOL" --show-progress -q https://github.com/AppImage/AppImageKit/releases/download/10/appimagetool-x86_64.AppImage
    chmod a+x $APPIMAGETOOL
  fi

  rm -f Application-x86_64.AppImage
  rm -f Status-x86_64.AppImage

  [ $VERBOSE_LEVEL -ge 1 ] && ldd $(joinExistingPath "$usrBinPath" 'Status') 
  $DEPLOYQT \
    $(joinExistingPath "$usrBinPath" 'reportApp') \
    -verbose=$VERBOSE_LEVEL -always-overwrite -no-strip -no-translations -qmake="$(joinExistingPath "${QTBIN}" 'qmake')" \
    -qmldir="$STATUSREACTPATH/desktop/reportApp"

  desktopFilePath="$(joinExistingPath "$WORKFOLDER" 'AppDir/usr/share/applications/Status.desktop')"
  $DEPLOYQT \
    $desktopFilePath \
    -verbose=$VERBOSE_LEVEL -always-overwrite -no-strip \
    -no-translations -bundle-non-qt-libs \
    -qmake="$qmakePath" \
    -extra-plugins=imageformats/libqsvg.so \
    -qmldir="$(joinExistingPath "$STATUSREACTPATH" 'node_modules/react-native')"

  pushd $WORKFOLDER
    [ $VERBOSE_LEVEL -ge 1 ] && ldd AppDir/usr/bin/Status
    cp -r assets/share/assets AppDir/usr/bin
    cp -rf StatusImAppImage/* AppDir/usr/bin
    rm -f AppDir/usr/bin/Status.AppImage
  popd

  $DEPLOYQT \
    $desktopFilePath \
    -verbose=$VERBOSE_LEVEL -appimage -qmake="$qmakePath"
  pushd $WORKFOLDER
    [ $VERBOSE_LEVEL -ge 1 ] && ldd AppDir/usr/bin/Status
    cp -r assets/share/assets AppDir/usr/bin
    cp -rf StatusImAppImage/* AppDir/usr/bin
    rm -f AppDir/usr/bin/Status.AppImage
  popd
  $APPIMAGETOOL \
    "$WORKFOLDER/AppDir"
  pushd $WORKFOLDER
    [ $VERBOSE_LEVEL -ge 1 ] && ldd AppDir/usr/bin/Status
    rm -rf Status.AppImage
  popd

  echo -e "${GREEN}Package ready in ./Status-x86_64.AppImage!${NC}"
  echo ""
}

function bundleMacOS() {
  # download prepared package with mac bundle files (it contains qt libraries, icon)
  echo "Downloading skeleton of mac bundle..."

  pushd $WORKFOLDER
    rm -rf Status.app
    # TODO this needs to be fixed: status-react/issues/5378
    [ -f ./Status.app.zip ] || curl -L -o Status.app.zip https://desktop-app-files.ams3.digitaloceanspaces.com/Status_20181113.app.zip
    echo -e "${GREEN}Downloading done.${NC}"
    echo ""
    unzip ./Status.app.zip
    cp -r assets/share/assets Status.app/Contents/Resources
    ln -sf ../Resources/assets ../Resources/ubuntu-server ../Resources/node_modules Status.app/Contents/MacOS
    chmod +x Status.app/Contents/Resources/ubuntu-server
    cp ../desktop/bin/Status Status.app/Contents/MacOS/Status
    cp ../desktop/bin/reportApp Status.app/Contents/MacOS
    cp ../.env Status.app/Contents/Resources
    ln -sf ../Resources/.env Status.app/Contents/MacOS/.env
    cp -f ../deployment/macos/qt-reportApp.conf Status.app/Contents/Resources
    ln -sf ../Resources/qt-reportApp.conf Status.app/Contents/MacOS/qt.conf
    install_name_tool -add_rpath "@executable_path/../Frameworks" \
                      -delete_rpath "${QT_PATH}/lib" \
                      'Status.app/Contents/MacOS/reportApp'
    install_name_tool -add_rpath "@executable_path/../Frameworks" \
                      -delete_rpath "${QT_PATH}/lib" \
                      'Status.app/Contents/MacOS/Status'
    cp -f ../deployment/macos/Info.plist Status.app/Contents
    cp -f ../deployment/macos/status-icon.icns Status.app/Contents/Resources
    $DEPLOYQT Status.app -verbose=$VERBOSE_LEVEL \
      -qmldir="$STATUSREACTPATH/node_modules/react-native/ReactQt/runtime/src/qml/"
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
