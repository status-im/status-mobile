'use strict';

type MenuItems = Array<{
  text?: string,
  onPress?: ?Function,
}>;

const NativeModules = require('react-native').NativeModules;

class DesktopMenu {

  static show(
    menuItems?: MenuItems
  ): void {
    var itemNames = menuItems.map(i => i.text);
    var itemMap = new Map();
    for (let i = 0; i < menuItems.length; ++i) {
      itemMap.set(menuItems[i].text, menuItems[i].onPress);
    }
    NativeModules.DesktopMenuManager.show(
        itemNames,
        (name) => {
          (itemMap.get(name))();
        }
    );
  }
}

module.exports = DesktopMenu;
