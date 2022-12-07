(ns status-im.ui.screens.chat.styles.message.audio
  (:require [quo.design-system.colors :as colors]
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

(defn slider []
  {:style (merge {:margin-left 12
                  :height      34}
                 (when platform/ios? {:margin-bottom 4}))
   :thumb-tint-color colors/white
   :minimum-track-tint-color colors/white
   :maximum-track-tint-color colors/white-transparent})

(defn play-pause-container [loading?]
  {:background-color colors/white-persist
   :width            28
   :height           28
   :padding          (if loading? 4 2)
   :border-radius    15})

(def times-container
  {:flex-direction :row
   :justify-content :space-between})

(defn timestamp []
  (merge (message.style/audio-message-timestamp-text)
         {:margin-left 40}))