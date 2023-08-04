(ns quo2.components.tags.context-tag.style
  (:require [quo2.foundations.colors :as colors]))

(defn base-tag
  [override-theme blur?]
  {:border-radius    100
   :padding-vertical 3
   :flex-direction   :row
   :padding-right    8
   :padding-left     8
   :background-color (if blur?
                       (colors/theme-colors colors/neutral-80-opa-5 colors/white-opa-5 override-theme)
                       (colors/theme-colors colors/neutral-10 colors/neutral-90 override-theme))})

(defn context-tag-icon-color
  [blur? theme]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-40 theme)
    (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)))

(def context-tag-text-container
  {:align-items    :center
   :margin-left    4
   :flex-direction :row
   :font-size      13})

(def audio-tag-container
  {:padding-left     2
   :padding-vertical 2})

(def audio-tag-icon-container
  {:width            20
   :height           20
   :border-radius    10
   :align-items      :center
   :justify-content  :center
   :background-color colors/primary-50})

(def audio-tag-icon-color colors/white)

(defn audio-tag-text-color
  [override-theme]
  (colors/theme-colors colors/neutral-100 colors/white override-theme))

(def community-tag
  {:padding-vertical 2
   :padding-left     2})

(def community-tag-text-container
  {:margin-left    4
   :flex           1
   :flex-direction :row
   :align-items    :center})

(defn community-tag-text
  [override-theme]
  {:margin-left 2
   :color       (colors/theme-colors colors/neutral-100 colors/white override-theme)})
