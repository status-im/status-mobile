(ns quo.previews.text
  (:require [quo.core :as quo]
            [quo.react-native :as rn]))

(defn preview-text []
  [rn/scroll-view {:flex               1
                   :padding-horizontal 16}
   (for [size   [:tiny :small :base :large :x-large :xx-large]
         weight [:regular :medium :semi-bold :bold :monospace]]
     ^{:key (str)}
     [rn/view {:padding-vertical 16}
      [quo/text {:weight weight
                 :size   size}
       (str "Text size " size ", font weight " weight)]])])
