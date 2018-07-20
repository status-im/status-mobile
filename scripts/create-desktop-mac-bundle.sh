#!/bin/bash


RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
STATUSREACTPATH="$SCRIPTPATH/.."
WORKFOLDER="$SCRIPTPATH/../mac_bundle"
MACDEPLOYQT=""

#if no arguments passed, inform user about possible ones (one for making script interactive, one for path to macdeployqt)

if [ $# -eq 0 ]
  then
    echo -e "${RED}You need to specify path to macdeployqt binary as an argument${NC}"
    echo "Example: scripts/create-desktop-mac-bundle.sh /usr/bin/macdeployqt"
    exit 1
  else
    MACDEPLOYQT=$1
fi

# check if gdrive installed
command -v gdrive >/dev/null 2>&1 || { echo -e "${RED}gdrive tool need to be installed. (brew install gdrive). Aborting.${NC}" >&2; exit 1; }


# inform user that define should be changed in "desktop/main.cpp"
echo ""
echo -e "${YELLOW}In desktop/main.cpp file please uncomment #define BULID_FOR_BUNDLE line.${NC}"
read -p "When ready, plese press enter to continue"
echo ""


# create directory for all work related to bundling
mkdir -p $WORKFOLDER
echo -e "${GREEN}Work folder created: $WORKFOLDER${NC}"
echo ""

# from index.desktop.js create javascript bundle and resources folder
echo "Generating StatusIm.bundle and assets folder..."
react-native bundle --entry-file index.desktop.js --bundle-output $WORKFOLDER/StatusIm.jsbundle --dev false --platform desktop --assets-dest $WORKFOLDER/assets
echo -e "${GREEN}Generating done.${NC}"
echo ""

# show path to javascript bundle and line that should be added to package.json
echo -e "${YELLOW}Please add the following line to package.json:${NC}"
echo "\"desktopJSBundlePath\": \"$WORKFOLDER/StatusIm.jsbundle\""
echo ""
read -p "When ready, plese press enter to continue"
echo ""


# build desktop app
echo "Building StatusIm desktop..."
react-native build-desktop
echo -e "${GREEN}Building done.${NC}"
echo ""


# download prepared package with mac bundle files (it contains qt libraries, icon)
echo "Downloading skeleton of mac bundle..."
echo -e "${YELLOW}First time gdrive can ask you for permissions to google drive${NC}"
gdrive download --path $WORKFOLDER 1fJbW9FzGGPvYkuJcSH5mCAcGdnyUeDSY
echo -e "${GREEN}Downloading done.${NC}"
echo ""


# Unpacking downloaded archive
echo "Unpacking bundle skeleton"
unzip $WORKFOLDER/StatusIm.app.zip -d $WORKFOLDER
chmod +x $WORKFOLDER/StatusIm.app/Contents/MacOs/ubuntu-server
echo -e "${GREEN}Unzipping done.${NC}"
echo ""


# copy binary and resources to mac bundle
echo "Copying resources and binary..."
cp -r $WORKFOLDER/assets/share/assets $WORKFOLDER/StatusIm.app/Contents/MacOs
cp $STATUSREACTPATH/desktop/bin/StatusIm $WORKFOLDER/StatusIm.app/Contents/MacOs
echo -e "${GREEN}Copying done.${NC}"
echo ""


# invoke macdeployqt to create StatusIm.dmg
echo "Creating bundle dmg..."
$MACDEPLOYQT $WORKFOLDER/StatusIm.app -verbose=3 -qmldir="$STATUSREACTPATH/node_modules/react-native/ReactQt/application/src/" -qmldir="$STATUSREACTPATH/node_modules/react-native/ReactQt/runtime/src/qml/" -dmg
echo -e "${GREEN}Bundle ready!${NC}"
echo ""
