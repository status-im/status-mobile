(ns status-im.ui.screens.chat.styles.message.message
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.chat.styles.photos :as photos]
            [status-im.utils.platform :as platform]
            [status-im.constants :as constants]))

(defn style-message-text
  [outgoing]
  {:color (if outgoing colors/white colors/text)})

(defn message-timestamp-placeholder
  [outgoing]
  {:font-size  10
   :align-self :flex-end
   :color (if outgoing
            colors/blue
            colors/blue-light)})

(def message-expand-button
  {:color         colors/gray
   :font-size     12
   :opacity       0.7
   :margin-bottom 20})

(def selected-message
  {:margin-top  18
   :margin-left 40
   :font-size   12
   :color       colors/text-gray})

(defn group-message-view
  [outgoing message-type]
  (let [align (if outgoing :flex-end :flex-start)]))

(defn delivery-status [outgoing]
  (if outgoing
    {:align-self    :flex-end
     :padding-right (if platform/desktop? 24 8)}
    {:align-self    :flex-start
     :padding-left  (if platform/desktop? 24 8)}))

(def message-author
  {:width      photos/default-size
   :align-self :flex-end})

(def delivery-view
  {:flex-direction :row
   :margin-top     2})

(def delivery-text
  {:color       colors/gray
   :font-size   12})

(def not-sent-view
  (assoc delivery-view
         :margin-bottom 2
         :padding-top 2))

(def not-sent-text
  (assoc delivery-text
         :color colors/red
         :text-align :right
         :padding-top 4))

(def not-sent-icon
  {:padding-top  3
   :padding-left 3})

(def message-activity-indicator
  {:padding-top 4})

(defn text-message
  [collapsed? outgoing]
  (assoc (style-message-text outgoing)
         :line-height 22
         :margin-bottom (if collapsed? 2 0)))

(defstyle emoji-message
  {:font-size 40
   :desktop   {:line-height 46}})

(def play-image
  {:width  33
   :height 33})

(def status-container
  {:padding-horizontal 5})

(defn message-container [window-width]
  {:position :absolute
   :width    window-width})

(defn message-author-name [chosen?]
  {:font-size           (if chosen? 13 12)
   :font-weight         (if chosen? "500" "400")
   :padding-top         6
   :padding-left        12
   :padding-right       16
   :margin-right        12
   :text-align-vertical :center
   :color               colors/gray})

(defn quoted-message-container [outgoing]
  {:margin-bottom              6
   :padding-bottom             6
   :border-bottom-color        (if outgoing
                                 colors/white-light-transparent
                                 (colors/alpha colors/black 0.1))
   :border-bottom-width        2
   :border-bottom-left-radius  2
   :border-bottom-right-radius 2})

(def quoted-message-author-container
  {:flex-direction  :row
   :align-items     :center
   :justify-content :flex-start})

(defn quoted-message-author [outgoing chosen?]
  (assoc (message-author-name chosen?)
         :padding-bottom  5
         :padding-top     4
         :padding-left    6
         :color           (if outgoing
                            (colors/alpha colors/white 0.7)
                            colors/gray)))

(defn quoted-message-text [outgoing]
  {:font-size 14
   :color (if outgoing
            (colors/alpha colors/white 0.7)
            colors/gray)})

(def extension-container
  {:align-items :center
   :margin      10})

(defn extension-text [outgoing]
  {:font-size  12
   :margin-top 10
   :color      (if outgoing colors/white-transparent colors/gray)})

(defn extension-install [outgoing]
  {:font-size 12
   :color     (if outgoing colors/white colors/blue)})
