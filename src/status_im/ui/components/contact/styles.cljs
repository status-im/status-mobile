(ns status-im.ui.components.contact.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as common]
            [status-im.utils.platform]))

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
   :flexDirection   :column
   :margin-left     16
   :margin-right    5
   :justify-content :center})

(defstyle name-text
  {:color   common/text1-color
   :android {:font-size 16}
   :ios     {:font-size      17
             :letter-spacing -0.2}})

(def info-text
  {:marginTop 1
   :fontSize  12
   :color     common/text2-color})

(def contact-container
  {:flex-direction   :row
   :align-items      :center
   :background-color colors/white})

(def forward-btn
  {:opacity        0.4
   :padding        12
   :alignItems     :center
   :justifyContent :center})

(def more-btn-container
  {:alignItems     :center
   :justifyContent :center})

(def more-btn
  {:padding 16})
