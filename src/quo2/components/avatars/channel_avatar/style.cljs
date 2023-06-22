(ns quo2.components.avatars.channel-avatar.style
  (:require [quo2.foundations.colors :as colors]))

(def lock-icon-size 12)

(defn outer-container
  [{:keys [big? background-color]}]
  {:width            (if big? 32 24)
   :height           (if big? 32 24)
   :border-radius    (if big? 32 24)
   :justify-content  :center
   :align-items      :center
   :background-color background-color})

(defn lock-container
  [{:keys [big?]}]
  {:position         :absolute
   :left             (if big? 20 12)
   :top              (if big? 20 12)
   :background-color (colors/theme-colors colors/white colors/neutral-90)
   :border-radius    (* 2 lock-icon-size)
   :padding          2})

(def lock-icon
  {:width  lock-icon-size
   :height lock-icon-size})
