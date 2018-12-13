'use strict';

const NativeModules = require('react-native').NativeModules;

class DesktopConfig {

  static getLoggingEnabled(callbackFn) {
    NativeModules.DesktopConfigManager.getLoggingEnabled(
      (enabled) => {
        callbackFn(enabled);
      });
  }

  static setLoggingEnabled(enabled) {
    NativeModules.DesktopConfigManager.setLoggingEnabled(enabled);

  }
}

module.exports = DesktopConfig;
