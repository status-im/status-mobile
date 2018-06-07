(ns status-im.chat.styles.message.message
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.styles :as styles]
            [status-im.chat.styles.photos :as photos]
            [status-im.ui.components.colors :as colors]
            [status-im.constants :as constants]))

(defstyle style-message-text
  {:font-size 15
   :color     styles/text1-color
   :android   {:line-height 22}
   :ios       {:line-height 23}})

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
    8
    4))

(def message-empty-spacing
  {:height 16})

(def message-body-base
  {:padding-right 10
   :padding-left  10})

(defn last-message-padding
  [{:keys [last? typing]}]
  (when (and last? (not typing))
    {:padding-bottom 16}))

(defn message-body
  [{:keys [outgoing] :as message}]
  (let [align     (if outgoing :flex-end :flex-start)
        direction (if outgoing :row-reverse :row)]
    (merge message-body-base
           {:flex-direction direction
            :width          230
            :padding-top    (message-padding-top message)
            :align-self     align
            :align-items    align})))

(def message-timestamp
  {:font-size  10
   :align-self :flex-end})

(defn message-timestamp-text [justify-timestamp? outgoing]
  (merge message-timestamp
         {:color (if outgoing colors/wild-blue-yonder colors/gray)}
         (when justify-timestamp? {:position :absolute
                                   :bottom   6
                                   :right    12})))

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
     :width          260
     :padding-left   10
     :padding-right  10
     :align-items    align}))

(defn delivery-status [outgoing]
  (if outgoing
    {:align-self    :flex-end
     :padding-right 22}
    {:align-self    :flex-start
     :padding-left  16}))

(def message-author
  {:width      photos/default-size
   :align-self :flex-end})

(def delivery-view
  {:flex-direction :row
   :margin-top     4
   :opacity        0.5})

(def delivery-text
  {:color       styles/color-gray4
   :margin-left 5
   :font-size   12})

(def not-sent-view
  (assoc delivery-view
         :opacity 1
         :margin-bottom 2
         :padding-top 2))

(def not-sent-text
  (assoc delivery-text
         :color styles/color-red
         :opacity 1
         :font-size 12
         :text-align :right
         :padding-top 4))

(def not-sent-icon
  {:padding-top  3
   :padding-left 3})

(def message-activity-indicator
  {:padding-top 4})

(defn text-message
  [{:keys [incoming-group]} collapsed?]
  (merge style-message-text
         {:margin-top    (if incoming-group 4 0)
          :margin-bottom (if collapsed? 2 0)}))

(defnstyle emoji-message
  [{:keys [incoming-group]}]
  {:font-size 40
   :color     styles/text1-color
   :android   {:line-height 45}
   :ios       {:line-height 46}
   :margin-top (if incoming-group 4 0)})

(defn message-view
  [{:keys [content-type outgoing group-chat first-in-group?]}]
  (merge {:padding-top        6
          :padding-horizontal 12
          :padding-bottom     8
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

(def author
  {:color         styles/color-gray4
   :margin-bottom 4
   :font-size     12})

(defn command-request-message-view [outgoing]
  {:border-radius    14
   :padding-vertical 4
   :background-color (if outgoing colors/hawkes-blue styles/color-white)})

(def command-request-from-text
  (merge style-sub-text {:margin-bottom 2}))

(defn command-request-image-touchable []
  {:position        :absolute
   :top             0
   :right           -8
   :align-items     :center
   :justify-content :center
   :width           48
   :height          48})

(defn command-request-image-view [command scale]
  {:width            32
   :height           32
   :border-radius    16
   :background-color (:color command)
   :transform        [{:scale scale}]})

(def command-image-view
  {:position    :absolute
   :top         0
   :right       0
   :width       24
   :height      24
   :align-items :center})

(def command-request-image
  {:position :absolute
   :top      9
   :left     10
   :width    12
   :height   13})

(def command-request-separator-line
  {:background-color colors/gray-light
   :height           1
   :border-radius    8
   :margin-top       10})

(def command-request-button
  {:align-items :center
   :padding-top 8})

(defn command-request-button-text [answered?]
  {:font-size 15
   :color     (if answered? colors/gray colors/blue)})

(def command-request-text-view
  {:margin-top 4
   :height     14})

(defn command-request-header-text [outgoing]
  {:font-size 12
   :color     (if outgoing colors/wild-blue-yonder colors/gray)})

(def command-request-network-text
  {:color colors/red})

(def command-request-row
  {:flex-direction :row
   :margin-top     6})

(def command-request-fiat-amount-row
  {:margin-top 6})

(def command-request-fiat-amount-text
  {:font-size 12
   :color     colors/black})

(def command-request-timestamp-row
  {:margin-top 6})

(defn command-request-timestamp-text [outgoing]
  {:font-size 12
   :color     (if outgoing colors/wild-blue-yonder colors/gray)})

(defstyle command-request-amount-text
  {:font-size   22
   :ios         {:letter-spacing -0.5}
   :color       colors/black})

(defn command-amount-currency-separator [outgoing]
  {:opacity 0
   :color (if outgoing colors/hawkes-blue colors/white)})

(defn command-request-currency-text [outgoing]
  {:font-size      22
   :letter-spacing 1
   :color          (if outgoing colors/wild-blue-yonder colors/gray)})

(def command-request-recipient-text
  {:color       colors/blue
   :font-size   14
   :line-height 18})

(def content-command-view
  {:flex-direction :column
   :align-items    :flex-start})

(def command-container
  {:flex-direction :row
   :margin-top     4
   :margin-right   32})

(def command-image
  {:margin-top 9
   :width      12
   :height     13
   :tint-color :#a9a9a9cc})

(def command-text
  (merge style-message-text
         {:margin-top        8
          :margin-horizontal 0}))

(def command-send-message-view
  {:flex-direction :column
   :align-items    :flex-start})

(def command-send-amount-row
  {:flex-direction  :row
   :justify-content :space-between})

(def command-send-amount
  {:flex-direction :column
   :align-items    :flex-end
   :max-width      250})

(defstyle command-send-amount-text
  {:font-size   22
   :color       colors/blue
   :ios         {:letter-spacing -0.5}})

(def command-send-currency
  {:flex-direction :column
   :align-items    :flex-end})

(defn command-send-currency-text [outgoing]
  {:font-size      22
   :margin-left    4
   :letter-spacing 1
   :color          (if outgoing colors/wild-blue-yonder colors/blue-transparent-40)})

(def command-send-fiat-amount
  {:flex-direction  :column
   :justify-content :flex-end
   :margin-top      6})

(def command-send-fiat-amount-text
  {:font-size 12
   :color     colors/black})

(def command-send-recipient-text
  {:color       colors/blue
   :font-size   14
   :line-height 18})

(defn command-send-timestamp [outgoing]
  {:color      (if outgoing colors/wild-blue-yonder colors/gray)
   :margin-top 6
   :font-size  12})

(def command-send-status-container
  {:margin-top     6
   :flex-direction :row})

(defn command-send-status-icon [outgoing]
  {:background-color (if outgoing
                       colors/blue-darker
                       colors/blue-transparent)
   :width            24
   :height           24
   :border-radius    16
   :padding-top      4
   :padding-left     4})

(defstyle command-send-status-text
  {:color       colors/blue
   :android     {:margin-top 3}
   :ios         {:margin-top 4}
   :margin-left 6
   :font-size   12})

(def audio-container
  {:flex-direction :row
   :align-items    :center})

(def play-view
  {:width         33
   :height        33
   :border-radius 16
   :elevation     1})

(def play-image
  {:width  33
   :height 33})

(def track-container
  {:margin-top  10
   :margin-left 10
   :width       120
   :height      26
   :elevation   1})

(def track
  {:position         :absolute
   :top              4
   :width            120
   :height           2
   :background-color :#EC7262})

(def track-mark
  {:position         :absolute
   :left             0
   :top              0
   :width            2
   :height           10
   :background-color :#4A5258})

(def track-duration-text
  {:position       :absolute
   :left           1
   :top            11
   :font-size      11
   :color          :#4A5258
   :letter-spacing 1
   :line-height    15})

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
   :letter-spacing -0.2
   :padding-top    6
   :padding-bottom 4
   :color          colors/gray})
