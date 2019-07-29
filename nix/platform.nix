{ target-os, stdenv }:

# We allow for "none" value because `make shell` can be called with `TARGET_OS` not set.
# We don't want to assume `all`, because that will rebuild status-go for all platforms.
assert stdenv.lib.assertOneOf "target-os" target-os [
  "linux"
  "android"
  "windows"
  "macos"
  "darwin"
  "ios"
  "all"
  "none"
];

let
  inherit (stdenv) isDarwin isLinux;

  # based on the value passed in through target-os, check if we're targetting a desktop platform
  targetDesktop = {
    "linux" = true;
    "windows" = true;
    "macos" = true;
    "darwin" = true;
    "all" = true;
  }.${target-os} or false;
  # based on the value passed in through target-os, check if we're targetting a mobile platform
  targetMobile = {
    "android" = true;
    "ios" = true;
    "all" = true;
  }.${target-os} or false;
  targetAndroid = {
    "android" = true;
    "all" = true;
  }.${target-os} or false;
  targetIOS = {
    "ios" = true;
    "all" = isDarwin;
  }.${target-os} or false;
  targetLinux = {
    "linux" = true;
    "all" = isLinux;
  }.${target-os} or false;
  targetDarwin = {
    "macos" = true;
    "darwin" = true;
    "all" = isDarwin;
  }.${target-os} or false;
  targetWindows = {
    "windows" = true;
    "all" = isLinux;
  }.${target-os} or false;

in {
  inherit targetDesktop targetMobile;
  inherit targetAndroid targetIOS;
  inherit targetLinux targetDarwin targetWindows;
}
