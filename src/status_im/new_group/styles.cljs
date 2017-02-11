(ns status-im.new-group.styles
  (:require [status-im.components.styles :refer [color-white
                                                 color-blue
                                                 text1-color
                                                 text2-color
                                                 color-light-blue
                                                 color-gray4]]
            [status-im.utils.platform :refer [platform-specific]]))

(defn toolbar-icon [enabled?]
  {:width   20
   :height  18
   :opacity (if enabled? 1 0.3)})

(def new-group-container
  {:flex             1
   :flex-direction   :column
   :background-color color-white})

(def chat-name-container
  {:margin-left 16})

(def group-chat-name-input
  {:font-size  14
   :color      text1-color})

(def group-chat-name-wrapper
  {:padding-top 0})

(def group-name-text
  {:margin-top    11
   :margin-bottom 10
   :letter-spacing -0.1
   :color         color-gray4
   :font-size     13
   :line-height   20})

(def members-text
  {:margin-top    10
   :margin-bottom 8
   :letter-spacing -0.2
   :color         color-gray4
   :font-size     16
   :line-height   19})

(def add-container
  {:flex-direction :row
   :align-items :center
   :margin-top 16
   :margin-bottom  16
   :margin-right 20})

(def add-icon
  {:align-items       :center
   :width             24
   :height            24})

(def add-text
  {:margin-left 32
   :color       color-light-blue
   :letter-spacing -0.2
   :font-size   17
   :line-height 20})

(def contacts-list
  {:background-color :white})

(def contact-container
  {:flex-direction :row
   :justify-content :center
   :align-items :center
   :height         56})

(def selected-contact
  {:background-color "#eff3fc"})

(def icon-check-container
  (merge (get-in platform-specific [:component-styles :contacts :icon-check])
         {:alignItems     :center
          :justifyContent :center
          :width 24
          :height 24}))

(def toggle-container
  {:width          56
   :height         56
   :alignItems     :center
   :justifyContent :center})

(def check-icon
  {:width  12
   :height 12})
