(ns legacy.status-im.ui.screens.offline-messaging-settings.styles
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.utils.styles :as styles]))

(def wrapper
  {:flex 1})

(styles/defn mailserver-item
  []
  {:flex-direction     :row
   :align-items        :center
   :justify-content    :space-between
   :padding-horizontal 16
   :ios                {:height 64}
   :android            {:height 56}})

(def mailserver-item-name-text
  {:typography :title})

(defn mailserver-icon-container
  [connected?]
  {:width            40
   :height           40
   :border-radius    20
   :background-color (if connected?
                       colors/blue
                       colors/black-transparent)
   :align-items      :center
   :justify-content  :center})

(def switch-container
  {:height 52})

(def automatic-selection-container
  {:border-top-width 1
   :border-top-color colors/gray-lighter
   :margin-top       16})

(def explanation-text
  {:color colors/gray})

(def use-history-explanation-text-container
  {:margin-right  16
   :margin-left   16
   :margin-top    8
   :margin-bottom 16})

(def history-nodes-label
  {:color              colors/gray
   :padding-horizontal 16
   :margin-top         48})
