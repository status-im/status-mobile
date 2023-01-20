(ns status-im2.contexts.chat.messages.content.album.style
  (:require [quo2.foundations.colors :as colors]))

(def max-album-height 292)

(defn album-container
  [portrait?]
  {:flex-direction (if portrait? :column :row)
   :flex-wrap      :wrap
   :max-height     max-album-height})

(defn image
  [dimensions index]
  {:width         (:width dimensions)
   :height        (:height dimensions)
   :margin-left   (when (or (and (not= index 0) (not= index 2) (not= count 3))
                            (= count 3)
                            (= index 2))
                    1)
   :margin-bottom (when (< index 2) 1)
   :align-self    :flex-start})

(def overlay
  {:position         :absolute
   :width            73
   :height           73
   :background-color colors/neutral-80-opa-60
   :justify-content  :center
   :align-items      :center})
