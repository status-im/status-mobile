(ns quo2.components.list-items.index
  (:require [quo.react-native :as rn]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]))

(defn index [{:keys [title]}]
  [rn/view
   [rn/view {:style {:border-top-width           1
                     :border-color               (colors/theme-colors colors/neutral-10 colors/neutral-80)
                     :padding-vertical           8
                     :padding-horizontal         16}}
    [text/text {:weight          :medium
                :size            :paragraph-2
                :secondary-color (colors/theme-colors colors/neutral-50 colors/neutral-40)}
     title]]])
