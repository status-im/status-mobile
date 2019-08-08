(ns status-im.ui.screens.chat.styles.message.message
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.chat.styles.photos :as photos]
            [status-im.utils.platform :as platform]
            [status-im.constants :as constants]))

(defn style-message-text
  [outgoing]
  {:color (if outgoing colors/white colors/text)})

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
  [{:keys [outgoing] :as message}]
  (let [align (if outgoing :flex-end :flex-start)
        direction (if outgoing :row-reverse :row)]
    {:flex-direction direction
     :padding-top    (message-padding-top message)
     :align-self     align
     :align-items    align}))

(def message-timestamp
  {:font-size  10
   :align-self :flex-end})

(defn message-timestamp-placeholder
  [outgoing]
  (assoc message-timestamp
         :color (if outgoing
                  colors/blue
                  colors/blue-light)))

(defn message-timestamp-text
  [justify-timestamp? outgoing rtl? emoji?]
  (merge message-timestamp
         {:color (if (and outgoing (not emoji?))
                   colors/white-transparent-70
                   colors/gray)}
         (when justify-timestamp? {:position              :absolute
                                   :bottom                7
                                   (if rtl? :left :right) 12})))

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

(defn group-message-wrapper [{:keys [outgoing] :as message}]
  (merge {:flex-direction :column}
         (if outgoing
           {:margin-left 64}
           {:margin-right 64})
         (last-message-padding message)))

(defn timestamp-content-wrapper [outgoing]
  {:flex-direction (if outgoing :row-reverse :row)})

(defn group-message-view
  [outgoing display-photo?]
  (let [align (if outgoing :flex-end :flex-start)]
    (merge {:flex-direction :column
            :max-width      (if platform/desktop? 500 320)
            :align-items    align}
           (if outgoing
             {:margin-right 8}
             (when-not display-photo?
               {:margin-left 8})))))

(defn delivery-status [outgoing]
  (if outgoing
    {:align-self    :flex-end
     :padding-right (if platform/desktop? 24 8)}
    {:align-self    :flex-start
     :padding-left  (if platform/desktop? 24 8)}))

(defn message-author [outgoing]
  (merge
   {:width      (+ 16 photos/default-size) ;; 16 is for the padding
    :align-self :flex-end}
   (if outgoing
     {:padding-left 8}
     {:padding-horizontal 8
      :padding-right 8})))

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

(defnstyle emoji-message
  [{:keys [incoming-group]}]
  {:font-size 40
   :desktop   {:line-height 46}
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
         (if (= content-type constants/content-type-emoji)
           {:flex-direction :row}
           {:background-color (if outgoing colors/blue colors/blue-light)})
         (when (= content-type constants/content-type-command)
           {:padding-top    12
            :padding-bottom 10})))

(def play-image
  {:width  33
   :height 33})

(def status-container
  {:padding-horizontal 5})

(def status-text
  {:margin-top  9
   :font-size   14
   :color       colors/gray})

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
                                 colors/white-transparent-10
                                 colors/black-transparent)
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
                            colors/white-transparent-70
                            colors/gray)))

(defn quoted-message-text [outgoing]
  {:font-size 14
   :color (if outgoing
            colors/white-transparent-70
            colors/gray)})
