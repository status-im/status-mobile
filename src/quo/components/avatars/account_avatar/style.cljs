(ns quo.components.avatars.account-avatar.style
  (:require
    [quo.foundations.colors :as colors]))

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
  [{:keys [type size customization-color]
    :or   {size                default-size
           customization-color :blue}}
   theme]
  (let [watch-only? (= type :watch-only)
        width       (cond-> size
                      (keyword? size) (container-size size))]
    (cond-> {:width            width
             :height           width
             :background-color (colors/resolve-color customization-color theme)
             :border-radius    (get-border-radius size)
             :border-color     (colors/theme-colors colors/neutral-80-opa-5 colors/white-opa-5 theme)
             :padding          (get-padding size)
             :align-items      :center
             :justify-content  :center}

      watch-only?
      (assoc :border-width     (get-border-width size)
             :background-color (colors/resolve-color customization-color theme 10)))))

