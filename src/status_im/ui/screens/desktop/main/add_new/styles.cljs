(ns status-im.ui.screens.desktop.main.add-new.styles
  (:require [status-im.ui.components.colors :as colors]))

(def new-view
  {:flex             1
   :background-color colors/white
   :margin-left      16
   :margin-right     24})

(def new-contact-title
  {:height          64
   :align-items     :flex-start
   :justify-content :center})

(def new-contact-title-text
  {:font-size   20
   :font-weight :bold
   :color       :black})

(def new-contact-subtitle
  {:font-size 14})

(def add-contact-edit-view
  {:height           45
   :margin-bottom    32
   :background-color colors/white
   :border-radius    12
   :flex-direction   :row
   :margin-top       16})

(defn add-contact-input [error?]
  (cond-> {:flex 1
           :font-size        14
           :background-color colors/gray-lighter
           :margin-right     12
           :border-radius    8}
    error?
    (assoc :border-color colors/red
           :border-width 1)))

(defn add-pub-chat-input [error?]
  (assoc (add-contact-input error?) :padding-left 10))

(defn add-contact-button [error?]
  {:width            140
   :height           45
   :border-radius    8
   :background-color (if error? colors/gray-lighter colors/blue)
   :align-items      :center
   :justify-content  :center})

(defn add-contact-button-text [error?]
  {:font-size 16
   :color     (if error? colors/gray colors/white)})

(def suggested-contact-view
  {:flex-direction "row"
   :align-items    :center
   :margin-bottom  16})

(def suggested-contacts
  {:margin-top 12})

(def suggested-contact-image
  {:width         46
   :height        46
   :border-radius 46
   :margin-right  16})

(def suggested-topic-image
  (merge suggested-contact-image
         {:background-color colors/blue
          :align-items      :center
          :justify-content  :center}))

(def suggested-topic-text
  {:font-size 25.6
   :color     colors/white})

(def topic-placeholder
  {:flex          0
   :top           -63
   :left          5
   :font-size     14
   :width         5
   :height        16
   :margin-bottom -16})

(def tooltip-container
  {:position    :absolute
   :align-items :center
   :align-self  :center
   :bottom         34})

(def tooltip-icon-text
  {:justify-content  :center
   :align-items      :center
   :flex 1
   :border-radius    8
   :padding-left     10
   :padding-right    10
   :background-color colors/red-light})

(def tooltip-triangle
  {:width              0
   :height             0
   :border-top-width   9.1
   :border-left-width  9.1
   :border-right-width 9.1
   :border-left-color  :transparent
   :border-right-color :transparent
   :border-top-color   colors/red-light})
