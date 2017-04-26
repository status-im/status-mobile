(ns status-im.components.contact.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.components.styles :as common]
            [status-im.utils.platform]))

(defstyle contact-inner-container
  {:flex             1
   :flex-direction   :row
   :align-items      :center
   :padding-left     16
   :background-color common/color-white
   :android          {:height 56}
   :ios              {:height 63}})

(def info-container
  {:flex            1
   :flexDirection   :column
   :margin-left     16
   :margin-right    5
   :justify-content :center})

(defstyle name-text
  {:color   common/text1-color
   :android {:font-size      16}
   :ios     {:font-size      17
             :letter-spacing -0.2}})

(def info-text
  {:marginTop 1
   :fontSize  12
   :color     common/text2-color})

(def contact-container
  {:flex-direction   :row
   :align-items      :center
   :background-color common/color-white})

(def more-btn-container
  {:alignItems     :center
   :justifyContent :center})

(def more-btn
  {:padding 16})

(def selected-contact
  {:background-color common/selected-contact-color})

(def toggle-container
  {:width          56
   :height         56
   :alignItems     :center
   :justifyContent :center})

(defnstyle icon-check-container [checked]
  {:background-color (if checked common/color-light-blue common/color-gray5)
   :alignItems     :center
   :justifyContent :center
   :android        {:border-radius 2
                    :width         17
                    :height        17}
   :ios            {:border-radius 50
                    :width         24
                    :height        24}})

(def check-icon
  {:width  12
   :height 12})
