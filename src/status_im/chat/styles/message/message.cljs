(ns status-im.chat.styles.message.message
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.styles :as styles]
            [status-im.constants :as constants]))

(defstyle style-message-text
  {:fontSize 15
   :color    styles/text1-color
   :android  {:line-height 22}
   :ios      {:line-height 23}})

(def style-sub-text
  {:top        -2
   :fontSize   12
   :color      styles/text2-color
   :lineHeight 14
   :height     16})

(defn message-padding-top
  [{:keys [first-in-date? same-author? same-direction?]}]
  (cond
    first-in-date?  20
    same-author?    8
    same-direction? 16
    :else           24))

(defn last-message-padding
  [{:keys [last? typing]}]
  (when (and last? (not typing))
    {:paddingBottom 16}))

(def message-datemark
  {:margin-top 10
   :height     34})

(def message-empty-spacing
  {:height 16})

(def message-body-base
  {:padding-right 10
   :padding-left  10})

(defn message-body
  [{:keys [outgoing] :as message}]
  (let [align     (if outgoing :flex-end :flex-start)
        direction (if outgoing :row-reverse :row)]
    (merge message-body-base
           {:flexDirection direction
            :width         260
            :paddingTop    (message-padding-top message)
            :alignSelf     align
            :alignItems    align}
           (last-message-padding message))))

(def selected-message
  {:marginTop  18
   :marginLeft 40
   :fontSize   12
   :color      styles/text2-color})

(def group-message-wrapper
  {:flexDirection :column})

(defn group-message-view
  [{:keys [outgoing] :as message}]
  (let [align (if outgoing :flex-end :flex-start)]
    {:flexDirection :column
     :width         260
     :padding-left  10
     :padding-right 10
     :alignItems    align}))

(def message-author
  {:width     36
   :alignSelf :flex-start})

(def photo
  {:borderRadius 18
   :width        36
   :height       36})

(def delivery-view
  {:flexDirection :row
   :marginTop     2
   :opacity       0.5})

(defstyle delivery-text
  {:color      styles/color-gray4
   :marginLeft 5
   :android    {:font-size 13}
   :ios        {:font-size 14}})

(defn text-message
  [{:keys [outgoing group-chat incoming-group]}]
  (merge style-message-text
         {:marginTop (if incoming-group 4 0)}))

(defn message-view
  [{:keys [content-type outgoing group-chat selected]}]
  (merge {:padding         12
          :backgroundColor styles/color-white
          :border-radius   8}
         (when (= content-type constants/content-type-command)
           {:paddingTop    10
            :paddingBottom 14})))

(defstyle author
  {:color         styles/color-gray4
   :margin-bottom 5
   :android       {:font-size 13}
   :ios           {:font-size 14}})

(def command-request-view
  {:paddingRight 16})

(def command-request-message-view
  {:borderRadius     14
   :padding-vertical 10
   :paddingRight     28
   :backgroundColor  styles/color-white})

(def command-request-from-text
  (merge style-sub-text {:marginBottom 2}))

(defn command-request-image-touchable []
  {:position       :absolute
   :top            0
   :right          -8
   :alignItems     :center
   :justifyContent :center
   :width          48
   :height         48})

(defn command-request-image-view [command scale]
  {:width           32
   :height          32
   :borderRadius    16
   :backgroundColor (:color command)
   :transform       [{:scale scale}]})

(def command-image-view
  {:position   :absolute
   :top        0
   :right      0
   :width      24
   :height     24
   :alignItems :center})

(def command-request-image
  {:position :absolute
   :top      9
   :left     10
   :width    12
   :height   13})

(def command-request-text-view
  {:marginTop 4
   :height    14})

(def content-command-view
  {:flexDirection :column
   :alignItems    :flex-start})

(def command-container
  {:flexDirection :row
   :margin-top    4
   :marginRight   32})

(def command-image
  {:margin-top 9
   :width      12
   :height     13
   :tint-color :#a9a9a9cc})

(def command-text
  (merge style-message-text
         {:marginTop        8
          :marginHorizontal 0}))

(def audio-container
  {:flexDirection :row
   :alignItems    :center})

(def play-view
  {:width        33
   :height       33
   :borderRadius 16
   :elevation    1})

(def play-image
  {:width  33
   :height 33})

(def track-container
  {:marginTop  10
   :marginLeft 10
   :width      120
   :height     26
   :elevation  1})

(def track
  {:position        :absolute
   :top             4
   :width           120
   :height          2
   :backgroundColor :#EC7262})

(def track-mark
  {:position        :absolute
   :left            0
   :top             0
   :width           2
   :height          10
   :backgroundColor :#4A5258})

(def track-duration-text
  {:position      :absolute
   :left          1
   :top           11
   :fontSize      11
   :color         :#4A5258
   :letterSpacing 1
   :lineHeight    15})

(def status-container
  {:flex           1
   :alignSelf      :center
   :alignItems     :center
   :width          249
   :padding-bottom 16})

(def status-image-view
  {:marginTop 20})

(def status-from
  {:marginTop 20
   :fontSize  18
   :color     styles/text1-color})

(def status-text
  {:marginTop  10
   :fontSize   14
   :lineHeight 20
   :textAlign  :center
   :color      styles/text2-color})

(defn message-animated-container [height]
  {:height height})

(defn message-container [window-width]
  {:position :absolute
   :width    window-width})

(defn new-message-container [margin on-top?]
  {:background-color styles/color-white
   :margin-bottom    margin
   :elevation        (if on-top? 6 5)})
