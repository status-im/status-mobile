(ns status-im.accounts.styles
  (:require [status-im.components.styles :refer [text1-color
                                                 color-white
                                                 toolbar-background2
                                                 online-color]]
            [status-im.components.react :as r]))


(def screen-container
  {:flex  1
   :color :white})

(def gradient-background
  {:position       :absolute
   :top            0
   :bottom         0
   :left           0
   :right          0
   :padding-bottom 84})

(defn account-list-content [cnt]
  (merge {:justifyContent :center}
         ;; todo this will not work with landscape and looks bad
         (when (< (* 69 (+ 2 cnt)) (:height (r/get-dimensions "window")))
           {:flex 1})))

(def account-list
  {:margin-top 20
   :height     100})

(def row-separator
  {:borderBottomWidth 1
   :borderBottomColor "#bababa"})

(def account-container
  {:flex            1
   :flexDirection   :row
   :height          69
   :backgroundColor "rgba(255, 255, 255, 0.1)"
   :alignItems      :center
   :justifyContent  :center})

(def photo-container
  {:flex           0.2
   :flexDirection  :column
   :alignItems     :center
   :justifyContent :center})

(def account-photo-container
  {:flex           1
   :width          36
   :height         36
   :alignItems     :center
   :justifyContent :center})

(def photo-image
  {:width         36
   :height        36
   :border-radius 18})

(def name-container
  {:flex          1
   :flexDirection :column})

(def name-text
  {:color    color-white
   :fontSize 16})

(def address-text
  {:color    color-white
   :fontSize 12})

(def online-container
  {:flex           0.2
   :flexDirection  :column
   :alignItems     :center
   :justifyContent :center})

(def bottom-actions-container
  {:position :absolute
   :left     0
   :right    0
   :bottom   0})

(def add-account-button-container
  {:flex           1
   :paddingVertical   16
   :paddingHorizontal 28
   :justifyContent :center
   :alignItems     :center})

(def add-account-button
  {:flexDirection :row})

(def icon-plus
  {:flexDirection :column
   :paddingTop    2
   :width         20
   :height        20})

(def add-account-text
  {:flexDirection :column
   :color         :white
   :fontSize      16
   :marginLeft    8})

(def recover-button-container
  {:flex     1})

(def recover-button
  {:flex 1
   :alignItems :center
   :paddingVertical   16
   :paddingHorizontal 28})

(def recover-button-text
  {:flex  1
   :color color-white
   :fontSize 16})
