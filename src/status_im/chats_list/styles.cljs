(ns status-im.chats-list.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle]])
  (:require [status-im.components.styles :refer [color-white
                                                 color-light-gray
                                                 color-blue
                                                 color-gray5
                                                 text1-color
                                                 text2-color
                                                 text4-color
                                                 separator-color
                                                 new-messages-count-color]]
            [status-im.components.tabs.styles :as tabs-st]
            [status-im.components.toolbar.styles :refer [toolbar-background1
                                                         toolbar-background2]]
            [status-im.utils.platform :as p]))

(defn toolbar []
  (merge {:background-color toolbar-background1}
         (get-in p/platform-specific [:component-styles :toolbar])))


(def gradient-top-bottom-shadow
  ["rgba(24, 52, 76, 0.165)"
   "rgba(24, 52, 76, 0.03)"
   "rgba(24, 52, 76, 0.01)"])

(def chat-separator-wrapper
  {:background-color color-white
   :opacity          0.5
   :height           1
   :padding-left     72})

(def chat-separator-item
  {:border-bottom-width 1
   :border-bottom-color color-gray5})

(def chat-container
  (merge {:flex-direction   :row
          :background-color color-white}
         (get-in p/platform-specific [:component-styles :chat-list :chat-container])))

(def chat-icon-container
  (merge {:padding-top    18
          :padding-bottom 18
          :padding-left   12
          :padding-right  20
          :width          72}
         (get-in p/platform-specific [:component-styles :chat-list :chat-icon-container])))

(def chat-info-container
  (merge {:margin-bottom   13
          :justify-content :space-between
          :flex            1
          :flex-direction  :column}
         (get-in p/platform-specific [:component-styles :chat-list :chat-info-container])))

(def chat-options-container
  (merge {:margin-right 16
          :padding-top  10}
         (get-in p/platform-specific [:component-styles :chat-list :chat-options-container])))

(def item-upper-container
  {:flex            1
   :flex-direction  :row
   :justify-content :space-between})

(def item-lower-container
  (merge {:flex            1
          :flex-direction  :row
          :justify-content :space-between}
         (get-in p/platform-specific [:component-styles :chat-list :item-lower-container])))

(def name-view
  {:flex-direction :row
   :flex-shrink    1})

(def name-text
  (merge {:color     text1-color
          :font-size 16}
         (get-in p/platform-specific [:component-styles :chat-list :chat-name])))

(def private-group-icon-container
  (merge {:width        16
          :height       12
          :margin-right 6}
         (get-in p/platform-specific [:component-styles :chat-list :private-group-icon-container])))

(def private-group-icon
  {:width  16
   :height 16})

(def public-group-icon-container
  (merge {:width        16
          :height       12
          :margin-right 6}
         (get-in p/platform-specific [:component-styles :chat-list :public-group-icon-container])))

(def public-group-icon
  {:width  16
   :height 16})

(def last-message-container
  {:flex-shrink 1})

(def last-message-text
  (merge {:color text4-color}
         (get-in p/platform-specific [:component-styles :chat-list :last-message])))

(def status-container
  {:flex-direction :row
   :top            16
   :right          16})

(def status-image
  {:marginTop 4
   :width     9
   :height    7})

(def datetime-text
  (merge {:color text4-color}
         (get-in p/platform-specific [:component-styles :chat-list :last-message-timestamp])))

(def new-messages-container
  {:width           22
   :height          22
   :margin-left     15
   :backgroundColor new-messages-count-color
   :borderRadius    50})

(def new-messages-text
  (merge {:left      0
          :fontSize  12
          :color     color-blue
          :textAlign :center}
         (get-in p/platform-specific [:component-styles :chat-list :unread-count])))

(def chats-container
  {:flex 1})

(defnstyle list-container [tabs-hidden?]
  {:android {:background-color color-light-gray
             :margin-bottom    20}

   :ios     {:background-color color-white
             :margin-bottom    (if tabs-hidden? 20 (+ 16 tabs-st/tabs-height))}})

(def toolbar-actions
  {:flex-direction :row
   :padding-right  14})

(def toolbar-btn
  {:width          24
   :height         56
   :margin-left    24
   :alignItems     :center
   :justifyContent :center})

(def opts-btn
  {:width          24
   :height         24
   :alignItems     :center
   :justifyContent :center})

(def create-icon
  {:fontSize 20
   :height   22
   :color    color-white})

(def group-icon
  {:margin-top   8
   :margin-right 6
   :width        14
   :height       9
   :tint-color   :white})
