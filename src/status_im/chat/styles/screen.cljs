(ns status-im.chat.styles.screen
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.styles :as component.styles]))

(def chat-view
  {:flex             1
   :background-color component.styles/chat-background})

(def toolbar-container
  {})

(def messages-container
  {:flex           1
   :padding-bottom 0
   :margin-bottom  0})

(def toolbar-view
  {:flexDirection   :row
   :height          56
   :backgroundColor component.styles/color-white
   :elevation       2})

(def action
  {:width          56
   :height         56
   :top            0
   :alignItems     :center
   :justifyContent :center})

(def icon-view
  {:width  56
   :height 56})

(def back-icon
  {:marginTop  21
   :marginLeft 23
   :width      8
   :height     14})

(defnstyle chat-name-view [show-actions]
  {:flex            1
   :justify-content :center
   :android         {:align-items    :flex-start
                     :margin-left    (if show-actions 66 18)
                     :padding-bottom 6}
   :ios             {:align-items :center}})

(def chat-name-text
  {:color      component.styles/color-gray6
   :fontSize   16})

(def group-icon
  {:margin-top    4
   :margin-bottom 2.7
   :width         14
   :height        9})

(def up-icon
  {:width  14
   :height 8})

(defstyle members
  {:color   component.styles/text4-color
   :ios     {:font-size  14
             :margin-top 4}
   :android {:font-size 13}})

(defstyle last-activity-text
  {:color   component.styles/text4-color
   :ios     {:font-size  14
             :margin-top 4}
   :android {:font-size 13}})

(defn actions-wrapper [status-bar-height]
  {:backgroundColor component.styles/color-white
   :elevation       2
   :position        :absolute
   :top             (+ 55 status-bar-height)
   :left            0
   :right           0})

(def actions-separator
  {:marginLeft      16
   :height          1.5
   :backgroundColor component.styles/separator-color})

(def actions-view
  {:marginVertical 10})

(def action-icon-row
  {:flexDirection :row
   :height        56})

(def action-icon-view
  (merge icon-view
         {:alignItems     :center
          :justifyContent :center}))

(def action-view
  {:flex           1
   :alignItems     :flex-start
   :justifyContent :center})

(def action-title
  {:margin-top -2.5
   :color      component.styles/text1-color
   :font-size  14})

(def action-subtitle
  {:margin-top 1
   :color      component.styles/text2-color
   :font-size  12})

(def actions-overlay
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(def typing-all
  {:marginBottom 20})

(def typing-view
  {:width        260
   :marginTop    10
   :paddingLeft  8
   :paddingRight 8
   :alignItems   :flex-start
   :alignSelf    :flex-start})

(def typing-background
  {:borderRadius    14
   :padding         12
   :height          38
   :backgroundColor component.styles/selected-message-color})

(def typing-text
  {:marginTop -2
   :fontSize  12
   :color     component.styles/text2-color})

(def overlay-highlight
  {:flex 1})

;; this map looks a bit strange
;; but this way of setting elevation seems to be the only way to set z-index (in RN 0.30)
(def bottom-info-overlay
  {:position         :absolute
   :top              -16
   :bottom           -16
   :left             -16
   :right            -16
   :background-color "#00000055"
   :elevation        8})

(defn bottom-info-container [height]
  {:backgroundColor component.styles/color-white
   :elevation       2
   :position        :absolute
   :bottom          16
   :left            16
   :right           16
   :height          height})

(def bottom-info-list-container
  {:padding-left   16
   :padding-right  16
   :padding-top    8
   :padding-bottom 8})

(def item-height 60)

(def bottom-info-row
  {:flex-direction "row"
   :padding-top    4
   :padding-bottom 4})

(def bottom-info-row-photo
  {:width        42
   :height       42
   :borderRadius 21})

(def bottom-info-row-text-container
  {:margin-left  16
   :margin-right 16})

(def bottom-info-row-text1
  {:color "black"})

(def bottom-info-row-text2
  {:color "#888888"})

(def add-contact
  {:height           35
   :background-color :white
   :justify-content  :center})

(def add-contact-text
  {:text-align          :center
   :text-align-vertical :center
   :color               :#7099e6})
