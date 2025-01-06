(ns quo.components.wallet.account-overview.style
  (:require
    [quo.foundations.colors :as colors]))

(def account-overview-wrapper
  {:height          110
   :align-items     :center
   :justify-content :center})

(defn account-name
  [color]
  {:margin-bottom 4
   :color         color})

(def reveal-icon
  {:margin-left 4
   :margin-top  2})

(def current-value
  {:margin-bottom 2})

(def row-centered
  {:flex-direction  :row
   :justify-content :center})

(defn bottom-time-text
  [margin-right?]
  {:margin-right (when margin-right? 8)
   :color        colors/neutral-80-opa-40})

(def right-arrow
  {:margin-top   1
   :margin-right 1})

(def bottom-time-to-text
  {:margin-right 8
   :color        colors/neutral-80-opa-40})

(defn percentage-change
  [customization-color theme]
  {:color        (colors/resolve-color customization-color theme)
   :margin-right 4})

(defn dot-separator
  [customization-color theme]
  {:background-color (colors/theme-colors
                      (colors/override-color customization-color 40 60)
                      (colors/override-color customization-color 40 50)
                      theme)
   :margin-right     4
   :margin-top       8
   :width            2
   :height           2})

(defn currency-change
  [customization-color theme]
  {:color        (colors/resolve-color customization-color theme)
   :margin-right 4})

(defn loading-bar-margin-bottom
  [{:keys [width height color margin-bottom]}]
  {:style {:width            width
           :height           height
           :border-radius    6
           :background-color color
           :margin-bottom    margin-bottom}})

(defn loading-bar-margin-right
  [color]
  {:width            32
   :height           10
   :border-radius    6
   :background-color color
   :margin-right     2})

(defn loading-bar-margin-right-extra
  [{:keys [color width height margin-right]}]
  {:width            width
   :height           height
   :border-radius    6
   :background-color color
   :margin-right     margin-right})

(defn loading-bar-tiny
  [color]
  {:width            10
   :height           10
   :border-radius    6
   :background-color color
   :margin-right     8})

(defn icon-props
  [customization-color theme]
  {:color (colors/resolve-color customization-color theme)
   :size  16})
