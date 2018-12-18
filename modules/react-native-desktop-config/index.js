'use strict';

const NativeModules = require('react-native').NativeModules;

class DesktopConfig {

  static getValue(name, callbackFn) {
    NativeModules.DesktopConfigManager.getValue(name, callbackFn);
  }

  static setValue(name, value) {
    NativeModules.DesktopConfigManager.setValue(name, value);

  }
}

module.exports = DesktopConfig;
