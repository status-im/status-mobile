#!/bin/bash

set -e

VERBOSE_LEVEL=${VERBOSE_LEVEL:-1}

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
OS=$(uname -s)

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
  'modules/react-native-desktop-notification/desktop' \
)

external_fonts=( \
  '../../../../../resources/fonts/SF-Pro-Text-Regular.otf' \
  '../../../../../resources/fonts/SF-Pro-Text-Medium.otf' \
  '../../../../../resources/fonts/SF-Pro-Text-Light.otf' \
)

function is_macos() {
  [[ "$OS" =~ Darwin ]]
}

function is_linux() {
  [[ "$OS" =~ Linux ]]
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

function init() {
  if [ -z $QT_PATH ]; then
    echo "${RED}QT_PATH environment variable is not defined!${NC}"
    exit 1
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
  fi

  if is_macos; then
    DEPLOYQT="$MACDEPLOYQT"
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
  jsPackagePath=$(joinExistingPath "$STATUSREACTPATH" 'desktop_files/package.json')
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
    rm -rf CMakeFiles CMakeCache.txt cmake_install.cmake Makefile
    cmake -Wno-dev \
          -DCMAKE_BUILD_TYPE=Release \
          -DEXTERNAL_MODULES_DIR="$(joinStrings ${external_modules_dir[@]})" \
          -DDESKTOP_FONTS="$(joinStrings ${external_fonts[@]})" \
          -DJS_BUNDLE_PATH="$WORKFOLDER/Status.jsbundle" \
          -DCMAKE_CXX_FLAGS:='-DBUILD_FOR_BUNDLE=1 -std=c++11'
    make -j5
  popd
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
    rm -rf StatusImAppImage
    # TODO this needs to be fixed: status-react/issues/5378
    if [ -z $STATUSIM_APPIMAGE ]; then
      [ -f ./StatusImAppImage.zip ] || wget https://desktop-app-files.ams3.digitaloceanspaces.com/StatusImAppImage.zip
      STATUSIM_APPIMAGE=./StatusImAppImage.zip
    fi
    unzip "$STATUSIM_APPIMAGE" -d .
    rm -rf AppDir
    mkdir AppDir
  popd

  qmakePath="$(joinExistingPath "${QTBIN}" 'qmake')"
  usrBinPath=$(joinPath "$WORKFOLDER" "AppDir/usr/bin")
  cp -r ./deployment/linux/usr $WORKFOLDER/AppDir
  cp ./.env $usrBinPath
  cp ./desktop/bin/Status $usrBinPath
  cp ./desktop/reportApp/reportApp $usrBinPath
  
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
    cp -rf ../desktop/modules/react-native-desktop-notification/desktop/SnoreNotify_ep-prefix/src/SnoreNotify_ep/lib/x86_64-linux-gnu/plugins AppDir/usr/lib
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
    [ -f ./Status.app.zip ] || curl -L -o Status.app.zip https://desktop-app-files.ams3.digitaloceanspaces.com/Status.app.zip
    echo -e "${GREEN}Downloading done.${NC}"
    echo ""
    unzip ./Status.app.zip
    cp -r assets/share/assets Status.app/Contents/Resources
    ln -sf ../Resources/assets ../Resources/ubuntu-server ../Resources/node_modules Status.app/Contents/MacOS
    chmod +x Status.app/Contents/Resources/ubuntu-server
    cp ../desktop/bin/Status Status.app/Contents/MacOS/Status
    cp ../desktop/reportApp/reportApp Status.app/Contents/MacOS
    cp ../.env Status.app/Contents/Resources
    ln -sf ../Resources/.env Status.app/Contents/MacOS/.env
    cp -f ../deployment/macos/qt-reportApp.conf Status.app/Contents/Resources
    ln -sf ../Resources/qt-reportApp.conf Status.app/Contents/MacOS/qt.conf
    install_name_tool -add_rpath "@executable_path/../Frameworks" \
                      -delete_rpath "$(joinExistingPath "$QT_PATH" 'clang_64/lib')" \
                      'Status.app/Contents/MacOS/reportApp'
    cp -f ../deployment/macos/Info.plist Status.app/Contents
    cp -f ../deployment/macos/status-icon.icns Status.app/Contents/Resources
    cp -rf ../desktop/modules/react-native-desktop-notification/desktop/SnoreNotify_ep-prefix/src/SnoreNotify_ep/lib Status.app/Contents
    install_name_tool -change \
                  ${STATUSREACTPATH}/desktop/modules/react-native-desktop-notification/desktop/SnoreNotify_ep-prefix/src/SnoreNotify_ep/lib/libsnore-qt5.0.7.dylib @rpath/libsnore-qt5.0.7.dylib \
                  ${WORKFOLDER}/Status.app/Contents/lib/plugins/libsnore-qt5/libsnore_backend_osxnotificationcenter.so
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
  else
    bundleLinux
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
