(ns status-im.ui.screens.chat.styles.main
  (:require [quo.design-system.colors :as colors]))

(def toolbar-container
  {:flex           1
   :align-items    :center
   :flex-direction :row})

(def chat-name-view
  {:flex            1
   :justify-content :center})

(def chat-name-text
  {:typography  :main-medium
   :font-size   15
   :line-height 22})

(def toolbar-subtitle
  {:typography  :caption
   :line-height 16
   :color       colors/text-gray})

;; this map looks a bit strange
;; but this way of setting elevation seems to be the only way to set z-index (in RN 0.30)
(defn add-contact
  []
  {:flex-direction      :row
   :align-items         :center
   :justify-content     :center
   :padding-vertical    6
   :border-bottom-width 1
   :border-color        colors/gray-lighter})

(def add-contact-text
  {:margin-left 4
   :color       colors/blue})

(def empty-chat-container
  {:flex             1
   :justify-content  :center
   :align-items      :center
   :padding-vertical 50
   :margin-right     6})

(def loading-text
  {:color          colors/gray
   :font-size      15
   :line-height    22
   :letter-spacing -0.2
   :margin-right   4
   :text-align     :center})

(def contact-request
  {:width            "100%"
   :justify-content  :center
   :align-items      :center
   :border-top-width 1
   :border-color     colors/gray-transparent-10})
