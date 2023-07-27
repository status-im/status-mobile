(ns quo2.components.avatars.account-avatar.style
  (:require [quo2.foundations.colors :as colors]))

(def default-size 80)

(defn get-border-radius
  [size]
  (case size
    80 16
    48 12
    32 10
    28 8
    24 8
    20 6
    16 4))

(defn get-emoji-size
  [size]
  (case size
    80 36
    48 24
    32 15
    28 12
    24 11
    20 11
    16 11))

(defn get-border-width
  [watch-only? size]
  (when watch-only?
    (case size
      16 0.8 ;; 0.8 px is for only size 16
      ;; Rest of the size will have 1 px
      1)))

(defn root-container
  [{:keys [type size theme customization-color]
    :or   {size                default-size
           customization-color :blue}}]
  (let [watch-only? (= type :watch-only)]
    {:width            size
     :height           size
     :background-color (if watch-only?
                         (colors/custom-color-by-theme customization-color 50 50 10 10 theme)
                         (colors/custom-color-by-theme customization-color 50 60 nil nil theme))
     :border-radius    (get-border-radius size)
     :border-width     (get-border-width watch-only? size)
     :border-color     (if (= theme :light) colors/neutral-80-opa-5 colors/white-opa-5)
     :justify-content  :center
     :align-items      :center}))
