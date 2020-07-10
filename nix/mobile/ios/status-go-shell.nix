#
# This prepares status-go build libs for XCode.
# It copies the status-go build result to 'modules/react-native-status/ios/RCTStatus'.
#

{ lib, mkShell, nim-status }:

mkShell {
  shellHook = ''
    export RCTSTATUS_DIR="$STATUS_REACT_HOME/modules/react-native-status/ios/RCTStatus"
    cp ${nim-status}/* $RCTSTATUS_DIR

  '';
}
