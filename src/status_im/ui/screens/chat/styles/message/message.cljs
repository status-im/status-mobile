(ns status-im.ui.screens.chat.styles.message.message
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.chat.styles.photos :as photos]
            [status-im.utils.platform :as platform]
            [status-im.constants :as constants]))

(defnstyle style-message-text [outgoing]
  {:font-size      15
   :color          (if outgoing colors/white colors/text)
   :letter-spacing -0.2
   :android        {:line-height 22}
   :ios            {:line-height 22}})

(defn message-padding-top
  [{:keys [first-in-group? display-username?]}]
  (if (and display-username?
           first-in-group?)
    6
    2))

(defn last-message-padding
  [{:keys [last? typing]}]
  (when (and last? (not typing))
    {:padding-bottom 16}))

(defn message-body
  [{:keys [outgoing display-photo?] :as message}]
  (let [align (if outgoing :flex-end :flex-start)
        direction (if outgoing :row-reverse :row)]
    (merge {:flex-direction direction
            :width          230
            :padding-top    (message-padding-top message)
            :align-self     align
            :align-items    align}
           (when display-photo?
             {:padding-right 8
              :padding-left  8}))))

(def message-timestamp
  {:font-size      10
   :letter-spacing 0.1
   :align-self     :flex-end})

(defn message-timestamp-text [justify-timestamp? outgoing rtl?]
  (merge message-timestamp
         {:color (if outgoing colors/wild-blue-yonder colors/gray)}
         (when justify-timestamp? {:position              :absolute
                                   :bottom                8
                                   (if rtl? :left :right) 12})))

(defn message-timestamp-placeholder-text [outgoing]
  (assoc message-timestamp
         :color
         (if outgoing colors/blue colors/white)))

(def message-expand-button
  {:color         colors/gray
   :font-size     12
   :opacity       0.7
   :margin-bottom 1})

(def selected-message
  {:margin-top  18
   :margin-left 40
   :font-size   12
   :color       colors/text-gray})

(defn group-message-wrapper [message]
  (merge {:flex-direction :column}
         (last-message-padding message)))

(defn timestamp-content-wrapper [{:keys [outgoing]}]
  {:flex-direction (if outgoing :row-reverse :row)})

(defn group-message-view
  [outgoing]
  (let [align (if outgoing :flex-end :flex-start)]
    {:flex-direction :column
     :width          230
     :padding-left   8
     :padding-right  8
     :align-items    align}))

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
  (assoc (style-message-text outgoing) :margin-bottom (if collapsed? 2 0)))

(defnstyle emoji-message
  [{:keys [incoming-group]}]
  {:font-size 40
   :color     colors/text
   :android   {:line-height 45}
   :ios       {:line-height 46}
   :margin-top (if incoming-group 4 0)})

(defn message-view
  [{:keys [content-type outgoing group-chat first-in-group?]}]
  (merge {:padding-vertical   6
          :padding-horizontal 12
          :border-radius      8
          :margin-top         (if (and first-in-group?
                                       (or outgoing
                                           (not group-chat)))
                                16
                                4)}
         (when-not (= content-type constants/content-type-emoji)
           {:background-color (if outgoing colors/blue colors/white)})
         (when (= content-type constants/content-type-command)
           {:padding-top    12
            :padding-bottom 10})))

(def play-image
  {:width  33
   :height 33})

(def status-container
  {:flex           1
   :align-self     :center
   :align-items    :center
   :width          249
   :padding-bottom 16})

(def status-image-view
  {:margin-top 20})

(def status-from
  {:margin-top 20
   :font-size  18
   :color      colors/text})

(def status-text
  {:margin-top  10
   :font-size   14
   :line-height 20
   :text-align  :center
   :color       colors/text-gray})

(defn message-container [window-width]
  {:position :absolute
   :width    window-width})

(def message-author-name
  {:font-size      12
   :padding-top    6
   :color          colors/gray})

(defn quoted-message-container [outgoing]
  {:margin-bottom              6
   :padding-bottom             6
   :border-bottom-color        (if outgoing
                                 colors/white-light-transparent
                                 colors/gray-lighter)
   :border-bottom-width        2
   :border-bottom-left-radius  2
   :border-bottom-right-radius 2})

(def quoted-message-author-container
  {:flex-direction  :row
   :align-items     :center
   :justify-content :flex-start})

(defn quoted-message-author [outgoing]
  {:font-size      12
   :padding-bottom 5
   :padding-top    4
   :color          (if outgoing
                     colors/wild-blue-yonder
                     colors/gray)})

(defn quoted-message-text [outgoing]
  {:font-size 14
   :color (if outgoing
            colors/wild-blue-yonder
            colors/gray)})
