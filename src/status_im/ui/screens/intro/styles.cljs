(ns status-im.ui.screens.intro.styles
  (:require [status-im.ui.components.colors :as colors]))

(def intro-view
  {:flex               1
   :justify-content    :flex-end
   :margin-bottom      12})

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

(def wizard-title
  {:typography    :header
   :text-align    :center
   :margin-bottom 16})

(def wizard-text
  {:color      colors/gray
   :text-align :center})

(defn wizard-text-with-height [height]
  (merge wizard-text
         (when-not (zero? height)
           {:height height})))

(def welcome-text-bottom-note
  {:typography  :caption
   :color       colors/gray
   :text-align  :center})

(defn list-item [selected?]
  {:flex-direction   :row
   :align-items      :center
   :justify-content  :space-between
   :padding-left     16
   :padding-right    10
   :background-color (if selected? colors/blue-light colors/white)
   :padding-vertical 12})

(def list-item-body
  {:flex-direction :row
   :flex           1
   :align-items    :flex-start})

(def multiaccount-image
  {:width         40
   :height        40
   :border-radius 20
   :border-width  1
   :border-color  colors/black-transparent})

(defn password-text-input [width]
  {:typography :header
   :width      width})

(def buttons-container
  {:align-items        :center
   :padding-horizontal 32})

(def bottom-button
  {:padding-horizontal 24
   :justify-content    :center
   :align-items        :center
   :flex-direction     :row})

(def disabled-bottom-button
  (assoc bottom-button :background-color colors/gray-transparent-10))

(def disabled-bottom-button-text
  {:color colors/gray-transparent-40})

(defn bottom-arrow []
  {:flex-direction   :row
   :justify-content  :flex-end
   :align-self       :stretch
   :padding-top      16
   :border-top-width 1
   :border-top-color colors/gray-lighter})
