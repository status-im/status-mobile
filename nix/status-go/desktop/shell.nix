#
# This is currently unused but is a reminder of how we used to build desktop app
#

{ mkShell, status-go-desktop }:

mkShell {
  buildInputs = [ status-go-desktop ];
  # These variables are used by the Status Desktop CMake build script in:
  # - modules/react-native-status/desktop/CMakeLists.txt
  shellHook = ''
    export STATUS_GO_DESKTOP_INCLUDEDIR=${status-go-desktop}/include
    export STATUS_GO_DESKTOP_LIBDIR=${status-go-desktop}/lib
  '';
}
