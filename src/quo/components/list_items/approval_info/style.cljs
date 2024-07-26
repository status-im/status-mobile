(ns quo.components.list-items.approval-info.style
  (:require [quo.foundations.colors :as colors]))

(defn icon-description-color
  [blur? theme]
  (if blur?
    colors/white-opa-40
    (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)))

(defn container
  [description? blur? theme]
  {:flex-direction     :row
   :padding-horizontal 12
   :padding-vertical   (if description? 8 12)
   :border-radius      16
   :gap                8
   :align-items        :center
   :border-width       1
   :border-color       (if blur?
                         colors/white-opa-5
                         (colors/theme-colors colors/neutral-10 colors/neutral-80 theme))})

(def labels
  {:flex            1
   :justify-content :center
   :padding-right   4})

(defn label
  [blur? theme]
  {:color (if blur?
            colors/white
            (colors/theme-colors colors/neutral-100 colors/white theme))})

(defn description
  [blur? theme]
  {:color (icon-description-color blur? theme)})
