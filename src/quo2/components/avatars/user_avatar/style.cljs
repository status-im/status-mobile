(ns quo2.components.avatars.user-avatar.style
  (:require [quo2.foundations.colors :as colors]))

(def sizes
  {:big    {:outer                   80
            :inner                   72
            :status-indicator        20
            :status-indicator-border 4
            :font-size               :heading-1}
   :medium {:outer                   48
            :inner                   44
            :status-indicator        12
            :status-indicator-border 2
            :font-size               :heading-2}
   :small  {:outer                   32
            :inner                   28
            :status-indicator        12
            :status-indicator-border 2
            :font-size               :paragraph-2}
   :xs     {:outer                   24
            :inner                   24
            :status-indicator        0
            :status-indicator-border 0
            :font-size               :paragraph-2}
   :xxs    {:outer                   20
            :inner                   20
            :status-indicator        0
            :status-indicator-border 0
            :font-size               :label}
   :xxxs   {:outer                   16
            :inner                   16
            :status-indicator        0
            :status-indicator-border 0
            :font-size               :label}})

(defn outer
  [size]
  (let [dimensions (get-in sizes [size :outer])]
    {:width         dimensions
     :height        dimensions
     :border-radius dimensions}))

(defn initials-avatar
  [size draw-ring? customization-color]
  (let [outer-dimensions (get-in sizes [size :outer])
        inner-dimensions (get-in sizes [size (if draw-ring? :inner :outer)])]
    {:position         :absolute
     :top              (/ (- outer-dimensions inner-dimensions) 2)
     :left             (/ (- outer-dimensions inner-dimensions) 2)
     :width            inner-dimensions
     :height           inner-dimensions
     :border-radius    inner-dimensions
     :justify-content  :center
     :align-items      :center
     :background-color (colors/custom-color-by-theme customization-color 50 60)}))

(def initials-avatar-text
  {:color colors/white-opa-70})

(defn inner-dot
  [size online?]
  (let [background (if online? colors/success-50 colors/neutral-40)
        dimensions (get-in sizes [size :status-indicator])]
    {:width            (- dimensions 4)
     :height           (- dimensions 4)
     :border-radius    (- dimensions 4)
     :background-color background}))

(defn dot
  [size ring?]
  (let [dimensions   (get-in sizes [size :status-indicator])
        border-width (get-in sizes [size :status-indicator-border])
        right        (case size
                       :big    2
                       :medium 0
                       :small  -2
                       0)
        bottom       (case size
                       :big    (if ring? -1 2)
                       :medium (if ring? 0 -2)
                       :small  -2
                       0)]
    {:position         :absolute
     :justify-content  :center
     :align-items      :center
     :bottom           bottom
     :right            right
     :width            dimensions
     :height           dimensions
     :border-width     border-width
     :border-radius    dimensions
     :background-color (colors/theme-colors colors/white colors/neutral-100)
     :border-color     (colors/theme-colors colors/white colors/neutral-100)}))
