(ns status-im.navigation2.stack-with-switcher
  (:require [quo.react-native :as rn]
            [status-im.utils.platform :as platform]
            [status-im.switcher.switcher :as switcher]))

(defn overlap-stack [comp view-id]
  [rn/view {:style {:flex          1
                    :margin-bottom (if platform/ios? 30 0)}}
   [comp]
   [switcher/switcher view-id]])
