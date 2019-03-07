#!/usr/bin/env bash

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
  '../../../../../resources/fonts/Inter-UI-Bold.otf' \
  '../../../../../resources/fonts/Inter-UI-Medium.otf' \
  '../../../../../resources/fonts/Inter-UI-Regular.otf' \
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
  [[ "$TARGET_SYSTEM_NAME" =~ Windows ]]
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

CMAKE_EXTRA_FLAGS=$'-DCMAKE_CXX_FLAGS:=\'-DBUILD_FOR_BUNDLE=1\''
[ -n $STATUS_NO_LOGGING ] && CMAKE_EXTRA_FLAGS="$CMAKE_EXTRA_FLAGS -DSTATUS_NO_LOGGING=1"
if is_windows_target; then
  CMAKE_EXTRA_FLAGS="$CMAKE_EXTRA_FLAGS -DCMAKE_TOOLCHAIN_FILE='Toolchain-Ubuntu-mingw64.cmake'"
  CMAKE_EXTRA_FLAGS="$CMAKE_EXTRA_FLAGS -DCMAKE_C_COMPILER='x86_64-w64-mingw32.shared-gcc'"
  CMAKE_EXTRA_FLAGS="$CMAKE_EXTRA_FLAGS -DCMAKE_CXX_COMPILER='x86_64-w64-mingw32.shared-g++'"
  CMAKE_EXTRA_FLAGS="$CMAKE_EXTRA_FLAGS -DCMAKE_RC_COMPILER='x86_64-w64-mingw32.shared-windres'"
fi

STATUSREACTPATH="$(cd "$SCRIPTPATH" && cd '..' && pwd)"
WORKFOLDER="$(joinExistingPath "$STATUSREACTPATH" 'StatusImPackage')"
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

  # Add path to javascript bundle to package.json
  local jsBundleLine="\"desktopJSBundlePath\": \"$WORKFOLDER/Status.jsbundle\""
  local jsPackagePath=$(joinExistingPath "$STATUSREACTPATH" 'desktop_files/package.json.orig')
  local tmp=$(mktemp)
  jq ".=(. + {$jsBundleLine})" "$jsPackagePath" > "$tmp" && mv "$tmp" "$jsPackagePath"
  echo -e "${YELLOW}Added 'desktopJSBundlePath' line to $jsPackagePath:${NC}"
  echo ""
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
    fi
    cmake -Wno-dev \
          -DCMAKE_BUILD_TYPE=Release \
          -DEXTERNAL_MODULES_DIR="$EXTERNAL_MODULES_DIR" \
          -DDESKTOP_FONTS="$DESKTOP_FONTS" \
          -DJS_BUNDLE_PATH="$JS_BUNDLE_PATH" \
          $CMAKE_EXTRA_FLAGS || exit 1
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

if is_linux; then
  declare -A treated_libs=()
  function handleLinuxDependency() {
    local module="$1"
    local targetPath="$2"
    local indent="$3"

    if [ ${treated_libs[$module]} ]; then
      return
    fi

    treated_libs["$module"]=1

    local targetModule="$targetPath/$(basename $module)"

    if [ -L "$module" ]; then
      handleLinuxDependency "$(dirname $module)/$(readlink -sq $module)" "$targetPath" "$indent"
    fi
  
    # Copy library to target
    [ $VERBOSE_LEVEL -ge 2 ] && echo "${indent}Copying $module to $targetModule"
    cp -a -f $module $targetModule
    chmod 777 $targetModule

    if [ -f "$targetModule" ]; then
      module="$targetModule"
    else
      echo -e "${RED}FATAL: $DEPLOYQT should have copied the dependency to ${targetPath}${NC}"
      exit 1
    fi

    treated_libs["$module"]=1
    if [ ! -L "$module" ]; then
      fixupRPathsInModule "$module" "$targetPath" "  ${indent}"
    fi
  }

  function fixupRPathsInModule() {
    local module="$1"
    local targetPath="$2"
    local indent="$3"

    if program_exists 'realpath'; then
      module=$(realpath -m --no-symlinks "$module" 2> /dev/null)
    fi

    local type=$(realpath $module | xargs file | awk -F':' "/^.*/{print \$2}" | awk -F' ' "/^.*/{print \$1}")
    if [ "$type" != 'ELF' ]; then
      return
    fi

    treated_libs["$module"]=1
    [ $VERBOSE_LEVEL -ge 2 ] && echo "${indent}Examining ${module}"
  
    if [ -L "$module" ]; then
      handleLinuxDependency "$(dirname $module)/$(readlink -sq $module)" "$targetPath" "$indent"
      return
    fi

    # Walk through the dependencies of $module
    local package_dep_libs=$(ldd $module | grep '=>')
    [ $? -eq 0 ] || return
    package_dep_libs=$(echo "$package_dep_libs" | awk -F'=>' -F ' ' "/^.*/{print \$3}")
    if [ $(echo "$package_dep_libs" | grep "not found") ]; then
      echo "Some dependencies for $module were not found:"
      ldd $module
      exit 1
    fi

    # Change dependency rpath in $module to point to $libPath
    local relPath="/$(realpath --relative-to="$(dirname $module)" $libPath)"
    [ "$relPath" = '/.' ] && relPath=''
    local rpath="\$ORIGIN${relPath}"
    echo "${indent}Updating $module to point to $rpath"
    patchelf --set-rpath "$rpath" "$module"
    set +e
    patchelf --set-interpreter /lib64/ld-linux-x86-64.so.2 "$module" 2> /dev/null
    set -e

    local nix_package_dep_libs=$(echo "$package_dep_libs" | grep /nix)
    if [ ${#nix_package_dep_libs[@]} -eq 0 ]; then
      return
    fi

    for depModule in ${nix_package_dep_libs[@]}; do
      local type=$(realpath $depModule | xargs file | awk -F':' "/^.*/{print \$2}" | awk -F' ' "/^.*/{print \$1}")
      if [ $type == 'ELF' ]; then
        local fileName=$(basename $depModule)
        local baseNameGlob="$(dirname $depModule)/${fileName%%.*}.so*"
        for file in `ls $baseNameGlob 2> /dev/null`; do
          if [ -L "$file" ]; then
            handleLinuxDependency "$file" "$targetPath" "$indent"
          fi
        done
        handleLinuxDependency "$depModule" "$targetPath" "$indent"
      else
        echo "${indent}$depModule is not an ELF file"
      fi
    done
  }

  function patchQtPlugins() {
    for f in `find $1 -name *.so*`; do
      local relPath=$(realpath --relative-to="$(dirname $f)" ${WORKFOLDER}/AppDir/usr/lib)
      patchelf --set-rpath "\$ORIGIN/$relPath" $f
      touch --no-create -h -t 197001010000.00 $f
    done
  }
fi

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
  usrBinPath="$(joinPath "$WORKFOLDER" "AppDir/usr/bin")"
  cp -r ./deployment/linux/usr $WORKFOLDER/AppDir
  cp ./.env $usrBinPath
  cp ./desktop/bin/Status ./desktop/bin/reportApp $usrBinPath

  local libPath=$(joinPath "$WORKFOLDER" "AppDir/usr/lib")
  fixupRPathsInModule "$usrBinPath/Status" "$libPath"
  fixupRPathsInModule "$usrBinPath/reportApp" "$libPath"

  rm -f Application-x86_64.AppImage Status-x86_64.AppImage

  [ $VERBOSE_LEVEL -ge 1 ] && ldd $(joinExistingPath "$usrBinPath" 'Status')
  desktopFilePath="$(joinExistingPath "$WORKFOLDER" 'AppDir/usr/share/applications/Status.desktop')"
  pushd $WORKFOLDER
    cp -r assets/share/assets $usrBinPath
    cp -rf StatusImAppImage/* $usrBinPath
    rm -f $usrBinPath/Status.AppImage
  popd

  # TODO: process plugins
  linuxdeployqt \
    $desktopFilePath \
    -verbose=$VERBOSE_LEVEL -no-strip \
    -no-translations -bundle-non-qt-libs \
    -qmake="$qmakePath" \
    -executable="$(joinExistingPath "$usrBinPath" 'reportApp')" \
    -qmldir="$(joinExistingPath "$STATUSREACTPATH" 'node_modules/react-native')" \
    -qmldir="$(joinExistingPath "$STATUSREACTPATH" 'desktop/reportApp')" \
    -extra-plugins=imageformats/libqsvg.so \
    -appimage

  patchQtPlugins "${WORKFOLDER}/AppDir/usr/plugins"
  patchQtPlugins "${WORKFOLDER}/AppDir/usr/qml"

  pushd $WORKFOLDER
    rm -f $usrBinPath/Status.AppImage
    for f in `find ./AppDir`; do
      touch --no-create -h -t 197001010000.00 $f
    done
    [ $VERBOSE_LEVEL -ge 1 ] && ldd $usrBinPath/Status

    appimagetool ./AppDir
    # Ensure the AppImage isn't using the interpreter in Nix's store
    patchelf --set-interpreter /lib64/ld-linux-x86-64.so.2 ./Status-x86_64.AppImage
    chmod +x ./Status-x86_64.AppImage
    rm -rf Status.AppImage
  popd

  echo -e "${GREEN}Package ready in ./Status-x86_64.AppImage!${NC}"
  echo ""
}

if is_macos; then
  function getQtBaseBinPathFromNixStore() {
    local qtFullDerivationPath=$(nix show-derivation -f $STATUSREACTPATH/default.nix | jq -r '.[] | .inputDrvs | 'keys' | .[]' | grep qt-full)
    local qtBaseDerivationPath=$(nix show-derivation $qtFullDerivationPath | jq -r '.[] | .inputDrvs | 'keys' | .[]' | grep qtbase)

    echo $(nix show-derivation $qtBaseDerivationPath | jq -r '.[] | .outputs.bin.path')
  }

  function copyVersionedQtLibToPackage() {
    local qtbaseBinPath="$1"
    local fileName="$2"
    local targetPath="$3"

    mkdir -p $targetPath
    local srcPath=$(find $qtbaseBinPath/lib -name $fileName)
    echo "Copying $srcPath to $targetPath"
    cp -a -f "$srcPath" "$targetPath/$fileName"
    chmod +w "$targetPath/$fileName"
  }

  function fixupRPathsInDylib() {
    local dylib="$1"
    local targetPath="$2"
    local replacementPath="$3"

    [ $VERBOSE_LEVEL -ge 2 ] && echo "${dylib}"
  
    # Walk through the dependencies of $dylib
    local dependencies=$(otool -L "$dylib" | grep ".dylib (" | sed "s|@executable_path|$targetPath|" | awk -F "(" '{print $1}' | xargs)
    for depDylib in $dependencies; do
      local targetDepDylib=$(joinPath "$targetPath" "$(basename $depDylib)")

      # Fix rpath and copy library to target
      if [[ $depDylib == /nix/* ]]; then
        if [ ! -f "$targetDepDylib" ]; then
          echo -e "${RED}FATAL: $DEPLOYQT should have copied the dependency to ${targetPath}${NC}"
          exit 1
        fi

        # Change dependency rpath in $dylib to point to $targetReplacementPath
        local targetReplacementPath=$(echo $targetDepDylib | sed -e "s|$targetPath|$replacementPath|")
        echo "Updating $dylib to point to $targetReplacementPath"
        install_name_tool -change "$depDylib" "$targetReplacementPath" "$dylib"
      fi
    done
  }

  function fixupRemainingRPaths() {
    local binPath="$1"
    local replacementPath="$2"

    for dylib in $binPath/*.dylib; do
      fixupRPathsInDylib "$dylib" "$binPath" "$replacementPath"
    done
  }
fi

function bundleMacOS() {
  pushd $WORKFOLDER
    # download prepared package with mac bundle files (it contains qt libraries, icon)
    echo "Downloading skeleton of mac bundle..."

    rm -rf Status.app
    # TODO this needs to be fixed: status-react/issues/5378
    [ -f ./Status.app.zip ] || curl -L -o Status.app.zip https://desktop-app-files.ams3.digitaloceanspaces.com/Status_20181113.app.zip
    echo -e "${GREEN}Downloading done.${NC}"
    echo ""
    unzip ./Status.app.zip

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

    if program_exists nix && [ -n "$IN_NIX_SHELL" ]; then
      # Since in the Nix qt.full package the different Qt modules are spread across several directories,
      # macdeployqt cannot find some qtbase plugins, so we copy them in its place
      local qtbaseBinPath=$(getQtBaseBinPathFromNixStore)
      copyVersionedQtLibToPackage $qtbaseBinPath libqcocoa.dylib "$contentsPath/PlugIns/platforms/"
      copyVersionedQtLibToPackage $qtbaseBinPath libcocoaprintersupport.dylib "$contentsPath/PlugIns/printsupport/"
    fi

    macdeployqt Status.app \
      -verbose=$VERBOSE_LEVEL \
      -executable="$(joinExistingPath "$usrBinPath" 'reportApp')" \
      -qmldir="$(joinExistingPath "$STATUSREACTPATH" 'node_modules/react-native')" \
      -qmldir="$(joinExistingPath "$STATUSREACTPATH" 'desktop/reportApp')"

    # macdeployqt doesn't fix rpaths for all the libraries (although it copies them all), so we'll just walk through them and update rpaths to not point to /nix
    echo "Fixing remaining rpaths in modules..."
    local frameworksPath=$(joinExistingPath "$WORKFOLDER" "$contentsPath/Frameworks")
    fixupRemainingRPaths "$frameworksPath" "@executable_path/../Frameworks"
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
