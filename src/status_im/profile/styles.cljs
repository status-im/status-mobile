(ns status-im.profile.styles
  (:require [status-im.components.styles :refer [color-light-blue-transparent
                                                 color-white
                                                 color-gray
                                                 color-black
                                                 color-blue
                                                 color-blue-transparent
                                                 selected-message-color
                                                 online-color
                                                 separator-color
                                                 text1-color
                                                 text1-disabled-color
                                                 text2-color]]))

(def profile
  {:flex             1
   :background-color color-white
   :flex-direction   :column})

(def back-btn-touchable
  {:position :absolute})

(def back-btn-container
  {:width  56
   :height 56})

(def back-btn-icon
  {:margin-top  21
   :margin-left 23
   :width       8
   :height      14})

(def actions-btn-touchable
  {:position :absolute
   :right    0})

(def actions-btn-container
  {:width           56
   :height          56
   :align-items     :center
   :justify-content :center})

(def edit-btn-icon
  {:width  4
   :height 16})

(defn ok-btn-icon [enabled?]
  {:font-size 22
   :color     (if enabled? color-black color-gray)})

(def user-photo-container
  {:margin-top 22})

(def username
  {:margin-top 12
   :font-size  18
   :color      text1-color})

(def username-input
  {:align-self    "stretch"
   :margin-top    -8
   :margin-bottom -22
   :font-size     18
   :text-align    :center
   :color         text1-color})

(def status-block
  {:flex-direction "column"
   :align-items    "center"
   :justifyContent "center"
   :margin-left 100
   :margin-right 100})

(def status-input
  {:align-self   "stretch"
   :margin-left  16
   :margin-right 16
   :height       40
   :margin-top   -4
   :font-size    14
   :line-height  20
   :text-align   :center
   :color        text2-color})

(def btns-container
  {:margin-top     18
   :flex-direction :row})

(def message-btn
  {:height           40
   :justify-content  :center
   :background-color color-blue
   :padding-left     25
   :padding-right    25
   :border-radius    20})

(def message-btn-text
  {:margin-top  -2.5
   :font-size   14
   :color       color-white})

(def more-btn
  {:margin-left      10
   :width            40
   :height           40
   :align-items      :center
   :justify-content  :center
   :background-color color-blue-transparent
   :padding          8
   :border-radius    20})

(def more-btn-image
  {:width  4
   :height 16})

(def profile-properties-container
  {:margin-top     20
   :margin-left    16
   :align-items    :stretch
   :flex-firection :column})

(def profile-input-wrapper
  {:margin-bottom 16})

(def profile-input-text
  {:color text1-color})

(def profile-input-text-non-editable
  {:color text1-disabled-color})

(def report-user-container
  {:margin-top    32
   :margin-bottom 43
   :align-items   :center})

(def report-user-text
  {:font-size      14
   :line-height    21
   :color          text2-color
   ;; IOS:
   :letter-spacing 0.5})

(def qr-code-container
  {:flex       1
   :alignItems :center
   :margin     32})
