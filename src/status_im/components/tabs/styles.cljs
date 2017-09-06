(ns status-im.components.tabs.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.components.styles :as styles]
            [status-im.utils.platform :as p]))


(def tabs-height (if p/ios? 52 56))
(def tab-height (dec tabs-height))

(defn tabs-container [hidden?]
  {:position         :absolute
   :bottom           (if hidden? (- tabs-height) 0)
   :left             0
   :right            0
   :height           tabs-height
   :background-color styles/color-white
   :transform        [{:translateY 1}]})

(def tabs-container-line
  {:border-top-width 1
   :border-top-color styles/color-light-gray3})

(def tabs-inner-container
  {:flexDirection   :row
   :height          tab-height
   :opacity         1
   :justifyContent  :center
   :alignItems      :center})

(def tab
  {:flex           1
   :height         tab-height
   :justifyContent :center
   :alignItems     :center})

(defnstyle tab-title [active?]
  {:ios        {:font-size 11}
   :android    {:font-size 12}
   :margin-top 3
   :min-width  60
   :text-align :center
   :color      (if active? styles/color-blue4 styles/color-gray8)})

(defn tab-icon [active?]
  {:color (if active? styles/color-blue4 styles/color-gray4)})

(def tab-container
  {:flex           1
   :height         tab-height
   :justifyContent :center
   :alignItems     :center})

(def swiper
  {:shows-pagination false})

(defn main-swiper [tabs-hidden?]
  (merge
    swiper
    {:position :absolute
     :top      0
     :left     0
     :right    0
     :bottom   (if tabs-hidden? 0 tabs-height)}))
