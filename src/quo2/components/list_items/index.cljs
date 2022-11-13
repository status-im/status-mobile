(ns quo2.components.list-items.index
  (:require [quo.react-native :as rn]
            [quo.components.text :as text]
            [quo.design-system.colors :as colors]))

(defn index [{:keys [title]}]
  [rn/view
   [rn/view {:style {:border-top-width           1
                     :border-color               (colors/get-color :border-01)
                     :padding-vertical           3
                     :padding-horizontal         16}}
    [text/text title]]])
