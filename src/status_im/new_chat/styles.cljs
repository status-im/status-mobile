(ns status-im.new-chat.styles
  (:require [status-im.components.styles :as st]
            [status-im.utils.platform :as p]))

(def list-bottom-shadow
  ["rgba(24, 52, 76, 0.165)"
   "rgba(24, 52, 76, 0.03)"
   "rgba(24, 52, 76, 0.01)"])

(def list-top-shadow
  ["rgba(24, 52, 76, 0.01)"
   "rgba(24, 52, 76, 0.03)"])

(def list-separator
  {:border-bottom-width 1
   :border-bottom-color st/color-gray5
   :margin-left         72
   :opacity             0.5})

(def list-separator-wrapper
  {:background-color st/color-white
   :height           1})

(defn options-list []
  {:padding-top      (if p/ios? 0 8)
   :padding-bottom   (if p/ios? 0 8)
   :background-color st/color-white})

(def option-container
  {:flex-direction   :row
   :background-color st/color-white})

(def option-inner-container
  (merge {:flex             1
          :flex-direction   :row
          :background-color st/color-white}
         (get-in p/platform-specific [:component-styles :new-chat :option-inner-container])))

(def option-icon-container
  (merge {:width         40
          :height        40
          :border-radius 50
          :margin-left   16}
         (get-in p/platform-specific [:component-styles :new-chat :option-icon-container])))

(def option-icon
  {:width  24
   :height 24
   :top    8
   :left   8})

(def option-info-container
  {:flex           1
   :flexDirection  :column
   :margin-left    16
   :justifyContent :center})

(def option-name-text
  (get-in p/platform-specific [:component-styles :new-chat :option-name-text]))

(def contact-list-title-container
  (merge {:padding-left 16}
         (get-in p/platform-specific [:component-styles :new-chat :contact-list-title-container])))

(def contact-list-title
  (get-in p/platform-specific [:component-styles :new-chat :contact-list-title]))

(def contact-list-title-count
  {:color        st/text4-color
   :opacity      0.5})

(def contacts-list-container
  {:flex          1
   :margin-bottom 0})

(def contacts-list
  {:backgroundColor st/color-light-gray})

(def spacing-top
  {:background-color st/color-white
   :height           8})

(def spacing-bottom
  {:background-color st/color-white
   :height           8})
