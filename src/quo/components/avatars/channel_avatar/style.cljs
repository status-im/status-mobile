(ns quo.components.avatars.channel-avatar.style
  (:require
    [quo.foundations.colors :as colors]))

(def lock-icon-size 12)

(defn outer-container
  [{:keys [size customization-color theme]}]
  (let [container-size (case size
                         :size-80 80
                         :size-64 64
                         :size-32 32
                         24)]
    {:width            container-size
     :height           container-size
     :border-radius    container-size
     :justify-content  :center
     :align-items      :center
     :background-color (colors/resolve-color customization-color theme 10)}))

(defn emoji-size
  [size]
  {:text-align :center
   :font-size  (case size
                 :size-80 36
                 :size-64 24
                 :size-32 15
                 11)})

(defn lock-container
  [size theme]
  (let [distance (if (= size :size-32) 20 12)]
    {:position         :absolute
     :left             distance
     :top              distance
     :background-color (colors/theme-colors colors/white colors/neutral-95 theme)
     :border-radius    (* 2 lock-icon-size)
     :padding          2}))

(def lock-icon
  {:width  lock-icon-size
   :height lock-icon-size})
