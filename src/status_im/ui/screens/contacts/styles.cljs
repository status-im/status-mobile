(ns status-im.ui.screens.contacts.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.ui.components.styles :as common]))

(def toolbar-actions
  {:flex-direction :row})

(def contact-groups
  {:flex             1
   :background-color common/color-light-gray})

(def contacts-list-container
  {:flex 1})

(def contacts-list
  {:background-color common/color-white})

(def contacts-list-modal
  {:background-color common/color-light-gray})

(def empty-contact-groups
  (merge contact-groups
         {:align-items     :center
          :justify-content :center}))

(def empty-contacts-icon
  {:height 62
   :width  62})

(def empty-contacts-text
  {:margin-top 12
   :font-size  16
   :color      common/color-gray2})

(def contact-group-count
  {:margin-left 8
   :opacity     0.6})

(defstyle show-all
  {:flexDirection   :row
   :alignItems      :center
   :backgroundColor common/color-white
   :padding-left    72
   :android         {:height       56}
   :ios             {:height       64}})

(defstyle show-all-text
  {:android {:font-size      14
             :color          common/color-blue
             :letter-spacing 0.5}
   :ios     {:font-size      16
             :color          common/color-gray4
             :letter-spacing -0.2}})

(def option-inner-image
  {:width  24
   :height 18
   :top    16
   :left   13})

(def group-icon
  (assoc option-inner-image
    :tint-color common/color-gray))

; New contact

(def contact-form-container
  {:flex            1
   :color           :white
   :backgroundColor :white})

(def gradient-background
  {:position :absolute
   :top      0
   :right    0
   :bottom   0
   :left     0})

(def form-container
  {:margin-left 16
   :margin-top  8
   :height      72})

(def address-explication-container
  {:flex         1
   :margin-top   30
   :paddingLeft  16
   :paddingRight 16})

(def address-explication
  {:textAlign :center
   :color     common/color-gray})

(def qr-input
  {:margin-right 42})
