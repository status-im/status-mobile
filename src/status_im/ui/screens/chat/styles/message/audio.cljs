(ns status-im.ui.screens.chat.styles.message.audio
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.chat.styles.message.message :as message.style]
            [status-im.utils.platform :as platform]))

(defn container [window-width]
  {:width (* window-width 0.60)
   :flex-direction :column
   :justify-content :space-between})

(def play-pause-slider-container
  {:flex-direction :row
   :align-items :center})

(def slider-container
  {:flex-direction :column
   :align-items :stretch
   :flex-grow 1})

(defn slider [outgoing]
  {:style (merge {:margin-left 12
                  :height      34}
                 (when platform/ios? {:margin-bottom 4}))
   :thumb-tint-color (if outgoing
                       colors/white
                       colors/blue)
   :minimum-track-tint-color (if outgoing
                               colors/white
                               colors/blue)
   :maximum-track-tint-color (if outgoing
                               colors/white-transparent
                               colors/gray-transparent-40)})

(defn play-pause-container [outgoing? loading?]
  {:background-color (if outgoing? colors/white-persist colors/blue)
   :width            28
   :height           28
   :padding          (if loading? 4 2)
   :border-radius    15})

(def times-container
  {:flex-direction :row
   :justify-content :space-between})

(defn timestamp [outgoing]
  (merge (message.style/message-timestamp-text
          false
          outgoing
          false) {:margin-left 40}))