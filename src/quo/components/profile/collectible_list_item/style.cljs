(ns quo.components.profile.collectible-list-item.style
  (:require [quo.foundations.colors :as colors]
            [quo.foundations.shadows :as shadows]))

(def container-border-radius 12)
(def card-image-padding-vertical 3)
(def card-image-padding-horizontal 3)
(def default-opacity-for-loader 1)
(def default-opacity-for-image 0)

(defn fallback
  [{:keys [theme opacity]}]
  [{:opacity opacity}
   {:opacity          1
    :background-color (colors/theme-colors colors/neutral-2_5 colors/neutral-90 theme)
    :border-style     :dashed
    :border-color     (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
    :border-width     1
    :border-radius    container-border-radius
    :width            "100%"
    :aspect-ratio     1
    :align-items      :center
    :justify-content  :center}])

(def collectible-counter
  {:position :absolute
   :top      12
   :right    12})

(def avatar
  {:position :absolute
   :bottom   12
   :left     12})

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
   (shadows/get 2 theme)))

(def image
  {:width         "100%"
   :flex          1
   :border-radius container-border-radius})

(def card-details-container
  {:flex-direction :row
   :height         27
   :margin-top     3
   :margin-bottom  2
   :align-items    :center
   :padding-left   8
   :padding-right  0
   :padding-top    4})

(defn card-detail-text
  [empty-name? theme]
  {:flex         1
   :margin-right 8
   :color        (if empty-name?
                   (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
                   (colors/theme-colors colors/neutral-100 colors/white theme))})

(defn card-loader
  [opacity]
  [{:opacity opacity}
   {:position       :absolute
    :opacity        default-opacity-for-loader
    :left           8
    :align-items    :center
    :flex-direction :row}])

(def image-view-container
  {:aspect-ratio  1
   :border-radius container-border-radius})

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
   :opacity          default-opacity-for-loader
   :top              0
   :bottom           0
   :left             0
   :right            0
   :border-radius    container-border-radius
   :background-color (colors/theme-colors colors/white-70-blur colors/neutral-95-opa-70-blur theme)
   :z-index          2})

(defn loading-image-with-opacity
  [theme opacity]
  [{:opacity opacity}
   (loading-image theme)])

(defn avatar-container
  [opacity]
  [{:opacity opacity}
   {:flex           1
    :flex-direction :row
    :column-gap     8
    :opacity        default-opacity-for-image}])

(defn supported-file
  [opacity]
  [{:opacity opacity}
   {:aspect-ratio 1
    :opacity      default-opacity-for-image}])
