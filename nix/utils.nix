{ xcodeWrapper }:

let
  RED = "\\033[0;31m";
  GREEN="\\033[0;32m";
  NC = "\\033[0m";
  _xcodeToolsTest = ''
    xcode=0
    iPhoneSDK=0
    export PATH=${xcodeWrapper}/bin:$PATH
    [[ "$(xcrun xcodebuild -version)" == "Xcode ${_xcodeVersion}"* ]] && xcode=1
    [ $xcode -eq 1 ] && xcrun --sdk iphoneos --show-sdk-version > /dev/null && iPhoneSDK=1
  '';
  _xcodeToolReportScript = tool-name: ''[ $SELECTED -eq 0 ] && echo -e "${NC}- ${RED}[ ] ${tool-name}" || echo -e "${NC}- ${GREEN}[√] ${tool-name}${RED}"'';
  _xcodeToolsReportScript = ''
    echo -e "${RED}There are some required tools missing in the system:"
    export SELECTED=$xcode; ${_xcodeToolReportScript "Xcode ${_xcodeVersion}"}
    export SELECTED=$iPhoneSDK; ${_xcodeToolReportScript "iPhone SDK"}
  '';
  _xcodeVersion = builtins.replaceStrings ["xcode-wrapper-"] [""] xcodeWrapper.name;
  enforceXCodeAvailable = ''
    ${_xcodeToolsTest}
    if [ $xcode -eq 0 ]; then
      ${_xcodeToolsReportScript}
      echo -e "Please install Xcode ${_xcodeVersion} from the App Store.${NC}"
      exit 1
    fi
  '';
  enforceiPhoneSDKAvailable = ''
    ${_xcodeToolsTest}
    if [ $iPhoneSDK -eq 0 ]; then
      ${_xcodeToolsReportScript}
      if [ $xcode -eq 1 ]; then
        echo -e "Please install the iPhone SDK in Xcode.${NC}""
      else
        echo -e "Please install Xcode ${_xcodeVersion} from the App Store, and then the iPhone SDK.${NC}""
      fi
      exit 1
    fi
  '';

in {
  inherit enforceXCodeAvailable
          enforceiPhoneSDKAvailable;
}