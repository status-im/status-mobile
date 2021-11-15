# Status defaults for config
{
  status-im = {
    build-type = "pr";       # Build type (influences which .env file gets used for feature flags)
    build-number = 9999;     # Used for versionCode and CFBundleVersion in Android and iOS respectively
    commit-hash = "unknown"; # Included in APK Manifest for easier identification.

    android = {
      gradle-opts = null;    # Gradle options passed for Android builds
      keystore-path = null;  # Path to keystore for signing the APK
      apk-signed = true;     # F-Droid builds aren't signed by us
      abi-split = false;     # If APKs should be split based on architectures
      abi-include = "armeabi-v7a;arm64-v8a;x86"; # Android architectures to build for
    };

    status-go = { src-override = null; };
  };

  # Android SDK requires an accepted license
  android_sdk.accept_license = true;
}
