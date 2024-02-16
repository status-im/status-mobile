(ns legacy.status-im.ui.screens.network.styles
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.utils.styles :as styles]))

(def wrapper
  {:flex 1})

(def badge-name-text
  {:font-size 17})

(styles/def badge-connected-text
  {:color colors/gray
   :ios   {:margin-top 5}})

(def connect-button-container
  {:margin-top        8
   :margin-bottom     16
   :margin-horizontal 16})

(def connect-button
  {:height           52
   :align-items      :center
   :justify-content  :center
   :background-color colors/blue
   :border-radius    8
   :ios              {:opacity 0.9}})

(def connect-button-label
  {:color     colors/white-persist
   :font-size 17})

(def connect-button-description
  {:color      colors/gray
   :margin-top 8
   :height     20})

(styles/defn network-config-container
  []
  {:height            160
   :margin-top        8
   :padding-top       16
   :padding-left      16
   :margin-horizontal 16
   :background-color  colors/gray-lighter
   :ios               {:border-radius 9
                       :opacity       0.9}
   :android           {:border-radius 4}})

(styles/def network-config-text
  {:font-size 17
   :ios       {:opacity 0.8}
   :android   {:opacity 0.4}})

(defn network-icon
  [connected? size]
  {:width            size
   :height           size
   :border-radius    (/ size 2)
   :background-color (if connected? colors/blue colors/gray-lighter)
   :align-items      :center
   :justify-content  :center})

(def network-badge
  {:height         88
   :padding-left   16
   :flex-direction :row
   :align-items    :center})

(styles/def network-item
  {:flex-direction     :row
   :align-items        :center
   :padding-horizontal 16
   :ios                {:height 64}
   :android            {:height 56}})

(def network-item-name-text
  {:font-size 17})

(def network-item-connected-text
  {:color      colors/gray
   :font-size  14
   :margin-top 6})

(def bottom-container
  {:flex-direction    :row
   :margin-horizontal 12
   :margin-vertical   15})

(def container {:flex 1})
