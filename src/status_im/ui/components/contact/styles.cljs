(ns status-im.ui.components.contact.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.styles :as common]
            [status-im.utils.platform]
            [status-im.ui.components.colors :as colors]))

(defstyle contact-inner-container
  {:flex             1
   :flex-direction   :row
   :align-items      :center
   :padding-left     16
   :background-color colors/white
   :android          {:height 56}
   :ios              {:height 63}})

(def info-container
  {:flex            1
   :flex-direction  :column
   :margin-left     16
   :margin-right    5
   :justify-content :center})

(defstyle name-text
  {:color   common/text1-color
   :android {:font-size 16}
   :ios     {:font-size      17
             :letter-spacing -0.2}})

(def info-text
  {:margin-top 1
   :font-size  12
   :color      common/text2-color})

(def contact-container
  {:flex-direction   :row
   :align-items      :center
   :background-color colors/white})

(def forward-btn
  {:opacity         0.4
   :padding         12
   :align-items     :center
   :justify-content :center})

(def more-btn-container
  {:align-items     :center
   :justify-content :center})

(def more-btn
  {:padding 16})

(def toggle-container
  {:width          56
   :height         56
   :alignItems     :center
   :justifyContent :center})

(def check-icon
  {:width  16
   :height 16})
