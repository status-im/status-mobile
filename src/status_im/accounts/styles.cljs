(ns status-im.accounts.styles
  (:require [status-im.components.styles :refer [font
                                                 title-font
                                                 text1-color
                                                 color-white
                                                 toolbar-background2
                                                 online-color]]))


(def screen-container
  {:flex  1
   :color :white})

(def gradient-background
  {:position :absolute
   :top      0
   :right    0
   :bottom   0
   :left     0})

(def account-list-container1
  {:position :absolute
   :top 56
   :right 0
   :bottom 0
   :left 0
   :justifyContent :center})

(def accounts-container
  {:flex           1
   :flex-direction :column
   :justifyContent :center
   :padding-bottom 56})

(def account-list-view-container
  {:flexDirection :column
   :justifyContent :center})

(def account-list
  {})

(def row-separator
  {:borderBottomWidth 1
   :borderBottomColor "#bababa"})

(def account-container
  {:flex 1
   :flexDirection :row
   :height 69
   :backgroundColor "rgba(255, 255, 255, 0.1)"
   :alignItems     :center
   :justifyContent :center})

(def photo-container
  {:flex 0.2
   :flexDirection :column
   :alignItems     :center
   :justifyContent :center})

(def account-photo-container
  {:flex 1
   :backgroundColor "rgba(255, 255, 255, 0.2)"
   :borderRadius 50
   :width 36
   :height 36
   :alignItems     :center
   :justifyContent :center})

(def photo-image
  {:borderRadius 50
   :width        36
   :height       36})

(def name-container
  {:flex 1
   :flexDirection :column})

(def name-text
  {:color color-white
   :fontSize 16})

(def address-text
  {:color color-white
   :fontSize 12})

(def online-container
  {:flex 0.2
   :flexDirection :column
   :alignItems     :center
   :justifyContent :center})

(def add-account-button-container
  {:position :absolute
   :bottom 16
   :height 50
   :left 100
   :right 100
    :justifyContent :center
   :alignItems :center})

(def add-account-button
  {:flexDirection :row})

(def icon-plus
  {:flexDirection :column
   :paddingTop 2
   :width 20
   :height 20})

(def add-account-text
  {:flexDirection :column
   :color :white
   :fontSize 16
   :marginLeft 8})
