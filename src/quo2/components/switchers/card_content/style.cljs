(ns quo2.components.switchers.card-content.style
  (:require
    [quo2.foundations.colors :as colors]))

(defn content-container
  [status]
  {:flex-direction  :row
   :justify-content :space-between
   :align-items     (if (= status :mention) :center :flex-end)})

(def notification-container
  {:width           20
   :height          20
   :justify-content :center
   :align-items     :center
   :margin-left     8})

(def message-text
  {:color colors/white})

(def sticker
  {:width  24
   :height 24})

(def gif
  {:width         24
   :height        24
   :border-radius 8})
