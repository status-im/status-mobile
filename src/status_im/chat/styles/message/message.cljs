(ns status-im.chat.styles.message.message
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.styles :as styles]
            [status-im.ui.components.colors :as colors]
            [status-im.constants :as constants]))

(def photo-size 36)

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
  [{:keys [first-in-group?]}]
  (if first-in-group?
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
            :width          260
            :padding-top    (message-padding-top message)
            :align-self     align
            :align-items    align})))

(def message-timestamp
  {:margin-left     5
   :margin-right    5
   :margin-bottom   -2
   :color           colors/gray
   :opacity         0.5
   :align-self      :flex-end})

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

(def delivery-status
  {:align-self    :flex-end
   :padding-right 22})

(def message-author
  {:width      photo-size
   :align-self :flex-end})

(def photo
  {:border-radius (/ photo-size 2)
   :width         photo-size
   :height        photo-size})

(def delivery-view
  {:flex-direction :row
   :margin-top     2
   :opacity       0.5})

(defstyle delivery-text
  {:color      styles/color-gray4
   :margin-left 5
   :android    {:font-size 13}
   :ios        {:font-size 14}})

(defn text-message
  [{:keys [outgoing group-chat incoming-group]}]
  (merge style-message-text
         {:margin-top (if incoming-group 4 0)}))

(defn emoji-message
  [{:keys [incoming-group]}]
  {:font-size 40
   :color     styles/text1-color
   :android   {:line-height 45}
   :ios       {:line-height 46}
   :margin-top (if incoming-group 4 0)})

(defn message-view
  [{:keys [content-type outgoing group-chat selected]}]
  (merge {:padding         12
          :border-radius   8}
         (when-not (= content-type constants/content-type-emoji)
          {:background-color styles/color-white})
         (when (= content-type constants/content-type-command)
           {:padding-top    10
            :padding-bottom 14})))

(defstyle author
  {:color         styles/color-gray4
   :margin-bottom 5
   :android       {:font-size 13}
   :ios           {:font-size 14}})

(def command-request-view
  {:padding-right 16})

(def command-request-message-view
  {:border-radius    14
   :padding-vertical 10
   :padding-right    28
   :background-color styles/color-white})

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

(def command-request-text-view
  {:margin-top 4
   :height     14})

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
   :padding-bottom 4
   :color          colors/gray})

