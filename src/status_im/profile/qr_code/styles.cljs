(ns status-im.profile.qr-code.styles
  (:require [status-im.components.styles :refer [color-white]]
            [status-im.components.react :as r]))

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

(def qr-photo-container
  (merge photo-container
         {:margin-left  8
          :margin-right 4}))

(defn qr-code-container [dimensions]
  {:background-color "white"
   :width            (:width dimensions)
   :align-items      :center
   :justify-content  :center
   :padding          40})

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

(def online-image-container
  {:width           40
   :height          40
   :margin-right    4
   :align-items     :center
   :justify-content :center})

(def wallet-qr-code
  {:flex-grow      1
   :flex-direction :column})

(def account-toolbar
  {:background-color "#2f3031"})

(def wallet-account-container
  {:flex-grow      1
   :flexDirection  :row
   :height         69
   :alignItems     :center
   :justifyContent :center})

(def qr-code
  {:background-color "#2f3031"
   :flex-grow        1
   :align-items      :center
   :justify-content  :center})

(def footer
  {:background-color "#2f3031"})

(def wallet-info
  {:align-items    :center
   :padding-bottom 20
   :padding-top    20})

(def wallet-name-text
  {:color          color-white
   :padding-bottom 5})

(def wallet-address-text
  {:color "#999999"})

(def done-button
  {:flex-grow        1
   :flex-direction   :column
   :align-items      :center
   :justify-content  :center
   :height           51
   :background-color "#7597e4"})

(def done-button-text
  {:color color-white})
