(ns status-im.ui.screens.language-settings.views
  (:require [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]))

(defn language-settings []
  [react/view {:flex 1 :background-color colors/white}
   [status-bar/status-bar]
   [toolbar/simple-toolbar
    (i18n/label :t/language)]])
