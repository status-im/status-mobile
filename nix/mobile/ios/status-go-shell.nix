#
# This prepares status-go build libs for XCode.
# It copies the status-go build result to 'modules/react-native-status/ios/RCTStatus'.
#

{ lib, mkShell, status-go, nim-status }:

mkShell {
  shellHook = ''
    export RCTSTATUS_DIR="$STATUS_REACT_HOME/modules/react-native-status/ios/RCTStatus"
    cp ${status-go}/* $RCTSTATUS_DIR
    cp ${nim-status}/* $RCTSTATUS_DIR

  '';
}
