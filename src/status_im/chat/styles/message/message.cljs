(ns status-im.chat.styles.message.message
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.styles :as styles]
            [status-im.chat.styles.photos :as photos]
            [status-im.ui.components.colors :as colors]
            [status-im.constants :as constants]))

(defstyle style-message-text
  {:font-size      15
   :color          styles/text1-color
   :letter-spacing -0.2
   :android        {:line-height 22}
   :ios            {:line-height 22}})

(def style-sub-text
  {:top         -2
   :font-size   12
   :color       styles/text2-color
   :line-height 14
   :height      16})

(defn message-padding-top
  [{:keys [first-in-group? display-username?]}]
  (if (and display-username?
           first-in-group?)
    6
    2))

(def message-empty-spacing
  {:height 16})

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
         (if outgoing colors/hawkes-blue styles/color-white)))

(def message-expand-button
  {:color         colors/gray
   :font-size     12
   :opacity       0.7
   :margin-bottom 1})

(def selected-message
  {:margin-top  18
   :margin-left 40
   :font-size   12
   :color      styles/text2-color})

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
     :padding-right 8}
    {:align-self    :flex-start
     :padding-left  8}))

(def message-author
  {:width      photos/default-size
   :align-self :flex-end})

(def delivery-view
  {:flex-direction :row
   :margin-top     2})

(def delivery-text
  {:color       styles/color-gray4
   :font-size   12})

(def not-sent-view
  (assoc delivery-view
         :margin-bottom 2
         :padding-top 2))

(def not-sent-text
  (assoc delivery-text
         :color styles/color-red
         :text-align :right
         :padding-top 4))

(def not-sent-icon
  {:padding-top  3
   :padding-left 3})

(def message-activity-indicator
  {:padding-top 4})

(defn text-message
  [collapsed?]
  (assoc style-message-text :margin-bottom (if collapsed? 2 0)))

(defnstyle emoji-message
  [{:keys [incoming-group]}]
  {:font-size 40
   :color     styles/text1-color
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
           {:background-color (if outgoing colors/hawkes-blue styles/color-white)})
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
   :color      styles/text1-color})

(def status-text
  {:margin-top  10
   :font-size   14
   :line-height 20
   :text-align  :center
   :color       styles/text2-color})

(defn message-animated-container [height]
  {:height height})

(defn message-container [window-width]
  {:position :absolute
   :width    window-width})

(defn new-message-container [margin on-top?]
  {:background-color styles/color-white
   :margin-bottom    margin
   :elevation        (if on-top? 6 5)})

(def message-author-name
  {:font-size      12
   :padding-top    6
   :color          colors/gray})
