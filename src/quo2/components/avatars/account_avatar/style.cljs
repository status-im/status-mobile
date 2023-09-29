(ns quo2.components.avatars.account-avatar.style
  (:require [quo2.foundations.colors :as colors]))

(def default-size 80)
(def default-border-radius 16)
(def default-padding 16)
(def default-emoji-size 36)

(def ^:private container-size
  {:size-64 64})

(defn get-border-radius
  [size]
  (case size
    80       16
    :size-64 16
    48       12
    32       10
    28       8
    24       8
    20       6
    16       4
    default-border-radius))

(defn get-padding
  [size]
  (case size
    80 16
    48 8
    32 6
    28 6
    24 6
    20 4
    16 2
    default-padding))

(defn get-emoji-size
  [size]
  (case size
    80       36
    :size-64 30
    48       24
    32       15
    28       12
    24       11
    20       11
    16       11
    default-emoji-size))

(defn get-border-width
  [size]
  (if (= size 16)
    0.8 ;; 0.8 px is for only size 16
    1)) ;; Rest of the size will have 1 px


(defn root-container
  [{:keys [type size theme customization-color]
    :or   {size                default-size
           customization-color :blue}}]
  (let [watch-only? (= type :watch-only)
        width       (cond-> size
                      (keyword? size) (container-size size))]
    (cond-> {:width            width
             :height           width
             :background-color (colors/custom-color-by-theme customization-color 50 60 nil nil theme)
             :border-radius    (get-border-radius size)
             :border-color     (if (= theme :light) colors/neutral-80-opa-5 colors/white-opa-5)
             :padding          (get-padding size)
             :align-items      :center
             :justify-content  :center}

      watch-only?
      (assoc :border-width     (get-border-width size)
             :background-color (colors/custom-color-by-theme customization-color 50 50 10 10 theme)))))

