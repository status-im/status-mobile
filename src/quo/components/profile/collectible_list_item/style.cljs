(ns quo.components.profile.collectible-list-item.style
  (:require [quo.foundations.colors :as colors]
            [quo.foundations.shadows :as shadows]))

(def type-card-image-height 150)
(def type-card-image-height-and-border (+ type-card-image-height 2))
(def type-image-height 160)
(def container-border-radius 12)
(def borders-width 2)
(def card-image-padding-vertical 3)
(def card-image-padding-horizontal 3)

(defn fallback
  [{:keys [theme width type]}]
  {:background-color (colors/theme-colors colors/neutral-2_5 colors/neutral-90 theme)
   :border-style     :dashed
   :border-color     (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
   :border-width     1
   :border-radius    container-border-radius
   :height           (if (= type :image)
                       width
                       (- width borders-width (* 2 card-image-padding-horizontal)))
   :width            (if (= type :image)
                       width
                       (- width borders-width (* 2 card-image-padding-horizontal)))
   :align-items      :center
   :justify-content  :center})

(def collectible-counter
  {:position :absolute
   :top      12
   :right    12})

(def avatar
  {:position :absolute
   :bottom   12
   :left     12})

(def container
  {:flex 1})

(defn card-view-container
  [theme]
  (merge
   {:background-color (colors/theme-colors colors/white colors/neutral-95 theme)
    :padding-top      card-image-padding-vertical
    :padding-left     card-image-padding-horizontal
    :padding-right    card-image-padding-horizontal
    :padding-bottom   card-image-padding-vertical
    :border-radius    14
    :border-width     1
    :border-color     (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)}
   (shadows/get 2)))

(defn card-image
  [width]
  {:aspect-ratio  1
   :flex          1
   :height        (- width borders-width (* 2 card-image-padding-horizontal))
   :width         (- width borders-width (* 2 card-image-padding-horizontal))
   :border-radius container-border-radius})

(defn card-details-container
  [status]
  {:flex-direction :row
   :margin-top     (if (= :loading status) 4 3)
   :margin-bottom  (if (= :loading status) 4 2)
   :align-items    :center
   :padding-left   8
   :padding-right  0
   :padding-top    4})

(def card-detail-text
  {:flex 1})

(def image-view-container {:border-radius container-border-radius})

(defn loading-square
  [theme]
  {:height           20
   :width            20
   :border-radius    6
   :align-self       :center
   :background-color (colors/theme-colors colors/neutral-5 colors/neutral-90 theme)})

(defn loading-message
  [theme]
  {:height           14
   :width            72
   :margin-left      8
   :border-radius    6
   :align-self       :center
   :background-color (colors/theme-colors colors/neutral-5 colors/neutral-90 theme)})

(defn loading-image
  [theme]
  {:position         :absolute
   :top              0
   :bottom           0
   :right            0
   :left             0
   :z-index          2
   :border-radius    16
   :background-color (colors/theme-colors colors/white-70-blur colors/neutral-95-opa-70-blur theme)})
