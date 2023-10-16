(ns quo2.components.avatars.account-avatar.style
  (:require
    [quo2.foundations.colors :as colors]))

(def default-size :size-80)
(def default-border-radius 16)
(def default-padding 16)
(def default-emoji-size 36)

(defn get-container-size
  [size]
  (case size
    :size-80 80
    :size-64 64
    :size-48 48
    :size-32 32
    :size-28 28
    :size-24 24
    :size-20 20
    :size-16 16))

(defn get-border-radius
  [size]
  (case size
    :size-80 16
    :size-64 16
    :size-48 12
    :size-32 10
    :size-28 8
    :size-24 8
    :size-20 6
    :size-16 4
    default-border-radius))

(defn get-padding
  [size]
  (case size
    :size-80 16
    :size-48 8
    :size-32 6
    :size-28 6
    :size-24 6
    :size-20 4
    :size-16 2
    default-padding))

(defn get-emoji-size
  [size]
  (case size
    :size-80 36
    :size-64 30
    :size-48 24
    :size-32 15
    :size-28 12
    :size-24 11
    :size-20 11
    :size-16 11
    default-emoji-size))

(defn get-border-width
  [size]
  (if (= size :size-16)
    0.8 ;; 0.8 px is for only size 16
    1)) ;; Rest of the size will have 1 px

(defn root-container
  [{:keys [type size theme customization-color]
    :or   {size                default-size
           customization-color :blue}}]
  (let [watch-only?      (= type :watch-only)
        missing-keypair? (= type :missing-keypair)
        dimension-size   (get-container-size size)]
    (cond-> {:width            dimension-size
             :height           dimension-size
             :background-color (colors/resolve-color customization-color theme)
             :border-radius    (get-border-radius size)
             :border-color     (colors/theme-colors colors/neutral-80-opa-5 colors/white-opa-5 theme)
             :padding          (get-padding size)
             :align-items      :center
             :justify-content  :center}

      watch-only?
      (assoc :border-width
             (get-border-width size)
             :background-color
             (colors/resolve-color customization-color theme 5))

      missing-keypair?
      (assoc
       :border-width     (get-border-width size)
       :background-color (colors/resolve-color customization-color theme (if (= theme :dark) 20 10))))))
