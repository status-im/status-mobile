#
# This prepares status-go build file called Statusgo.framework for XCode.
# It copies the status-go build result to 'modules/react-native-status/ios/RCTStatus'.
#

{ lib, mkShell, status-go-shared, status-go-nim-status }:

mkShell {
  shellHook = ''
    export RCTSTATUS_DIR="$STATUS_REACT_HOME/modules/react-native-status/ios/RCTStatus"
    cp ${status-go-shared}/* $RCTSTATUS_DIR
    cp ${status-go-nim-status}/* $RCTSTATUS_DIR

  '';
}
