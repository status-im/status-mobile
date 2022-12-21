(ns quo.components.list.index
  (:require [quo.react-native :as rn]
            [quo.components.text :as text]
            [quo.design-system.colors :as colors]))

(defn index [{:keys [title]}]
  [rn/view {:style {:padding-right 16}}
   [rn/view {:style {:border-top-width           1
                     :border-bottom-width        1
                     :border-right-width         1
                     :border-color               (colors/get-color :border-01)
                     :padding-vertical           3
                     :padding-horizontal         16
                     :border-top-right-radius    16
                     :border-bottom-right-radius 16}}
    [text/text title]]])
