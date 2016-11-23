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
                                                 text2-color
                                                 color-red]]
            [status-im.utils.platform :as p]))

(def profile
  {:flex             1
   :background-color color-white
   :flex-direction   :column})

(def back-btn-touchable
  {:position :absolute})

(def back-btn-container
  {:width  46
   :height 56
   :align-items     :center
   :justify-content :center})

(def back-btn-icon
  {:width       8
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

(def username-wrapper
  {:width         300
   :margin-top    (if p/ios? -18 -22)
   :margin-bottom -16})

(defn username-input [edit? valid?]
  {:font-size  18
   :text-align :center
   :color      (if edit?
                 (if valid? text1-color color-red)
                 text1-disabled-color)})

(def status-block
  {:flex-direction "column"
   :align-items    "center"
   :justifyContent "center"
   :margin-left    55
   :margin-right   55})

(defn status-view [height]
  {:align-self "stretch"
   :font-size  14
   :height     height
   :min-height 30
   :text-align "center"
   :color      text2-color})

(defn status-input [height]
  (merge (status-view height)
         {:margin-left  (if p/ios? 21 16)
          :margin-right 16
          :margin-top   (if p/ios? 6 1)}))

(defn status-text [height]
  (merge (status-view (- height (if p/ios? 5 10)))
         {:margin-left   18
          :margin-right  18
          :margin-top    11
          :margin-bottom 0}))

(def btns-container
  {:margin-top     0
   :flex-direction :row})

(def message-btn
  {:height           40
   :justify-content  :center
   :background-color color-blue
   :padding-left     25
   :padding-right    25
   :border-radius    20})

(def message-btn-text
  {:margin-top -2.5
   :font-size  14
   :color      color-white})

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
  {:align-items    :stretch
   :flex-firection :column
   :margin-top     16})

(def my-profile-properties-container
  {:align-items    :stretch
   :flex-firection :column
   :margin-top     32})

(def profile-property
  {:margin-left 16})

(def profile-property-with-top-spacing
  {:margin-top  32
   :margin-left 16})

(def profile-property-row
  {:flex           1
   :flex-direction :row})

(def profile-property-field
  {:margin-right 96
   :flex         1})

(def profile-input-wrapper
  {:margin-bottom 16})

(def profile-input-text
  {:color text1-color})

(def profile-input-text-non-editable
  {:color text1-disabled-color})

(def report-user-text
  {:font-size      14
   :line-height    21
   :color          text2-color
   ;; IOS:
   :letter-spacing 0.5})

(def qr-code-container
  {:flex             1
   :alignItems       :center
   :justify-content  :center
   :background-color "#000000aa"})

(def qr-code
  {:width            250
   :height           250
   :background-color "white"
   :border-radius    4
   :align-items      :center
   :padding-top      15
   :elevation        4})

(def hashtag
  {:color "#7099e6"})

(def underline-container
  {:background-color "#0000001f"
   :margin-bottom    18
   :height           1
   :align-items      :center})
