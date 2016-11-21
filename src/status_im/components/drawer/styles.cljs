(ns status-im.components.drawer.styles
  (:require [status-im.components.styles :refer [color-light-blue-transparent
                                                 color-white
                                                 color-black
                                                 color-blue
                                                 color-blue-transparent
                                                 selected-message-color
                                                 online-color
                                                 separator-color
                                                 text1-color
                                                 text2-color
                                                 text3-color
                                                 color-red]]
            [status-im.utils.platform :as p]))

(def drawer-menu
  {:flex             1
   :background-color color-white
   :flex-direction   :column})

(def user-photo-container
  {:margin-top      40
   :align-items     :center
   :justify-content :center})

(def user-photo
  {:border-radius 32
   :width         64
   :height        64})

(def name-container
  {:margin-top    (if p/ios? -13 -19)
   :margin-bottom -16
   :margin-left   16
   :margin-right  16})

(def name-input-wrapper
  {})

(defn name-input-text [valid?]
  {:color      (if valid? text1-color
                          color-red)
   :text-align :center})

(def status-container
  {:margin-left      16
   :margin-right     16
   :flex-direction   "row"
   :margin-top       (if p/ios? 5 5)
   :justify-content  :center})

(def status-view
  {:height              56
   :width               200
   :font-size           14
   :text-align          :center
   :text-align-vertical :top
   :color               text2-color})

(def status-input
  (merge status-view
         {:padding-left 4
          :padding-top  (if p/ios? 0 5)}))

(def status-text
  (merge status-view
         {:padding-left 0
          :padding-top  5}))

(def menu-items-container
  {:flex           1
   :margin-top     20
   :align-items    :stretch
   :flex-direction :column})

(def menu-item-touchable
  {:height      48
   :paddingLeft 16
   :paddingTop  14})

(def menu-item-text
  {:font-size   14
   :line-height 21
   :color       text1-color})

(def name-text
  {:color     text1-color
   :font-size 16})

(def switch-users-container
  {:padding-vertical 36
   :align-items      :center})

(def switch-users-text
  {:font-size   14
   :line-height 21
   :color       text3-color})

(def feedback {:text-align     :center})
