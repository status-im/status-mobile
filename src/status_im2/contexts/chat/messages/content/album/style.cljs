(ns status-im2.contexts.chat.messages.content.album.style
  (:require [quo2.foundations.colors :as colors]))

(def album-container
  {:flex-direction :row
   :flex-wrap      :wrap
   :overflow       :hidden})

(defn image
  [size index]
  {:width         size
   :height        size
   :margin-left   (when (and (not= index 0) (not= index 2)) 1)
   :margin-bottom (when (< index 2) 1)})

(def overlay
  {:position         :absolute
   :width            73
   :height           73
   :background-color colors/neutral-80-opa-60
   :justify-content  :center
   :align-items      :center})
