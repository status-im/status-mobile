(ns status-im2.contexts.chat.images-horizontal.style
  (:require [quo2.foundations.colors :as colors]))

(defn container-view
  [padding-top]
  {:background-color "#000"
   :height           "100%"
   :padding-top      padding-top})

(defn top-view-container
  [top-inset]
  {:position :absolute
   :left     20
   :top      (+ 12 top-inset)
   :z-index  1})

(def close-container
  {:width            32
   :height           32
   :border-radius    12
   :justify-content  :center
   :align-items      :center
   :background-color colors/neutral-80-opa-40})
