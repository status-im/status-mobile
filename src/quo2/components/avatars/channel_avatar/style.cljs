(ns quo2.components.avatars.channel-avatar.style
  (:require [quo2.foundations.colors :as colors]))

(def lock-icon-size 12)

(defn outer-container
  [{:keys [size color]}]
  (let [container-size (case size
                         :size/l-64 64
                         :size/l    32
                         24)]
    {:width            container-size
     :height           container-size
     :border-radius    container-size
     :justify-content  :center
     :align-items      :center
     :background-color (colors/theme-alpha color 0.1 0.1)}))

(defn emoji-size
  [size]
  {:font-size (case size
                :size/l-64 24
                :size/l    15
                11)})

(defn lock-container
  [size]
  (let [distance (if (= size :size/l) 20 12)]
    {:position         :absolute
     :left             distance
     :top              distance
     :background-color (colors/theme-colors colors/white colors/neutral-95)
     :border-radius    (* 2 lock-icon-size)
     :padding          2}))

(def lock-icon
  {:width  lock-icon-size
   :height lock-icon-size})
