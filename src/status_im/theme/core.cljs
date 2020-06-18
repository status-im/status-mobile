(ns status-im.theme.core
  (:require [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.colors :as colors]
            [quo.theme :as quo-theme]
            ["react-native-navigation-bar-color" :default changeNavigationBarColor]))

(defn change-theme [theme]
  (quo-theme/set-theme theme)
  (colors/set-theme theme)
  (status-bar/set-status-bar nil)
  (changeNavigationBarColor colors/white (= theme :light)))
