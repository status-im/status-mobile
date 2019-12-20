(ns status-im.ui.screens.notifications-settings.views
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]))

(defn notifications-settings []
  [react/view {:flex 1 :background-color colors/white}
   [topbar/topbar {:title :t/notifications}]])

