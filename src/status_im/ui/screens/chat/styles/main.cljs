(ns status-im.ui.screens.chat.styles.main
  (:require [status-im.ui.components.colors :as colors]))

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

(def last-activity-text
  {:font-size  14
   :margin-top 4
   :color      colors/text-gray})

;; this map looks a bit strange
;; but this way of setting elevation seems to be the only way to set z-index (in RN 0.30)
(defn add-contact []
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

(defn intro-header-container
  [loading-messages? no-messages?]
  (if (or loading-messages? no-messages?)
    {:flex               1
     :flex-direction     :column
     :justify-content    :center
     :align-items        :center
     :height             324}
    {:flex               1
     :flex-direction     :column
     :justify-content    :center
     :align-items        :center}))

(defn intro-header-icon [diameter color]
  {:width            diameter
   :height           diameter
   :align-items      :center
   :justify-content  :center
   :border-radius    (/ diameter 2)
   :background-color color})

(def intro-header-icon-text
  {:color       colors/white
   :font-size   52
   :font-weight "700"
   :opacity     0.8
   :line-height 72})

(defn intro-header-chat-name []
  {:font-size         22
   :font-weight       "700"
   :line-height       28
   :text-align        :center
   :margin-bottom     8
   :margin-horizontal 32
   :color             colors/black})

(def intro-header-description-container
  {:flex-wrap         :wrap
   :align-items       :flex-start
   :flex-direction    :row
   :margin-horizontal 32})

(def loading-text
  {:color          colors/gray
   :font-size      15
   :line-height    22
   :letter-spacing -0.2
   :margin-right   4
   :text-align     :center})

(def intro-header-description
  {:color             colors/gray
   :line-height       22
   :text-align        :center
   :margin-horizontal 32})

(def group-chat-join-footer
  {:flex            1
   :justify-content :center})

(def group-chat-join-container
  {:flex            1
   :align-items     :center
   :justify-content :center})

(def decline-chat
  {:color colors/blue
   :margin-bottom 40})

(def are-you-friends-bubble
  {:border-radius      8
   :border-width       1
   :margin-top         4
   :border-color       colors/gray-lighter
   :align-self         :flex-start
   :padding-vertical   12
   :margin-horizontal  8
   :padding-horizontal 16
   :margin-bottom      50})

(def are-you-friends-text
  {:line-height 22
   :text-align  :center
   :font-size   15
   :color       colors/gray})

(def share-my-profile
  {:color       colors/blue
   :text-align  :center
   :margin-top  11
   :line-height 22
   :font-size   15})

(def tribute-received-note
  {:font-size 13
   :line-height 18
   :text-align :center})
