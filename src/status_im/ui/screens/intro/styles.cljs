(ns status-im.ui.screens.intro.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def intro-view
  {:flex               1
   :justify-content    :flex-end
   :padding-horizontal 30
   :margin-bottom      12})

(def intro-logo-container
  {:align-items     :center
   :justify-content :center})

(defn dot-selector [n]
  (let [diameter 6
        interval 10]
    {:flex-direction  :row
     :justify-content :space-between
     :align-items     :center
     :height          diameter
     :width           (+ diameter (* (+ diameter interval)
                                     (dec n)))}))

(defn dot [color selected?]
  {:background-color (if selected?
                       color
                       (colors/alpha color 0.2))
   :width            6
   :height           6
   :border-radius    3})

(def welcome-image-container
  {:align-items :center
   :margin-top  42})

(def wizard-title
  {:typography    :header
   :text-align    :center
   :margin-bottom 16})

(def wizard-text
  {:color       colors/gray
   :text-align  :center})

(def welcome-text
  {:typography  :header
   :margin-top  32
   :text-align  :center})

(def welcome-text-bottom-note
  {:typography  :caption
   :color       colors/gray
   :text-align  :center})

(defn list-item [selected?]
  {:flex-direction   :row
   :align-items      :center
   :padding-left     16
   :padding-right    10
   :background-color (if selected? colors/blue-light colors/white)
   :padding-vertical 10})

(def multiaccount-image
  {:width         40
   :height        40
   :border-radius 20
   :border-width  1
   :border-color  colors/black-transparent})

(def welcome-text-description
  {:margin-top        8
   :text-align        :center
   :margin-horizontal 32
   :color             colors/gray})

(def intro-logo
  {:size 111})

(defn password-text-input [width]
  {:typography :header
   :width      width})

(def buttons-container
  {:align-items :center
   :margin-top  32})

(def bottom-button
  {:padding-horizontal 24
   :justify-content    :center
   :align-items        :center
   :flex-direction     :row})

(def bottom-button-container
  {:margin-bottom 24
   :margin-top    16})

(def bottom-arrow
  {:flex-direction   :row
   :justify-content  :flex-end
   :align-self       :stretch
   :padding-top      16
   :border-top-width 1
   :border-top-color colors/gray-lighter
   :margin-right     20})
