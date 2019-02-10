'use strict';

const NativeModules = require('react-native').NativeModules;
const NativeEventEmitter = require('react-native').NativeEventEmitter;

type Shortcuts = Array<{
  shortcut?: string,
  onPress?: ?Function,
}>;

class DesktopShortcuts {
  constructor() {
    this.shortcuts = new Map();
    this.eventEmitter = new NativeEventEmitter(NativeModules.DesktopShortcutsManager);
    this.eventEmitter.addListener('shortcutInvoked', this.handleShortcut.bind(this));
  }

  handleShortcut(shortcut) {
    var fn;// = this.shortcuts.get(shortcut);
    for (var [key, value] of this.shortcuts) {
      if (shortcut == key) {
        fn = value;
        break;
      }
    }
    if (fn) {
      fn();
    };
  }

  register(shortcuts: Shortcuts): void {
    //console.log('### register(shortcuts)' + JSON.stringify(shortcuts));
    this.shortcuts = new Map();

    var shortcutKeys = shortcuts.map(s => s.shortcut);
    for (let i = 0; i < shortcuts.length; ++i) {
      this.shortcuts.set(shortcuts[i].shortcut, shortcuts[i].onPress);
    }

    NativeModules.DesktopShortcutsManager.registerShortcuts(shortcutKeys); 
  }
}

module.exports = new DesktopShortcuts();
