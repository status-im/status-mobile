(ns status-im.contexts.chat.menus.pinned-messages.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]))

(defn heading
  [community?]
  {:padding-horizontal 20
   :margin-bottom      (when-not community? 12)})

(defn heading-container
  []
  {:flex-direction    :row
   :background-color  (colors/theme-colors colors/neutral-10 colors/neutral-80)
   :border-radius     20
   :align-items       :center
   :align-self        :flex-start
   :margin-horizontal 20
   :padding           4
   :margin-top        8
   :margin-bottom     16})

(defn heading-text
  []
  {:margin-left  6
   :margin-right 4
   :color        (colors/theme-colors colors/neutral-60 colors/neutral-20)})

(defn chat-name-text
  []
  {:margin-left  4
   :margin-right 8
   :color        (colors/theme-colors colors/neutral-60 colors/neutral-20)})

(def no-pinned-messages-container
  {:justify-content :center
   :align-items     :center
   :margin          12
   :margin-bottom   (when platform/android? 12)})

(def no-pinned-messages-icon
  {:width           80
   :height          80
   :justify-content :center
   :align-items     :center
   :border-width    1})

(def no-pinned-messages-content
  {:margin-top 12})

(def no-pinned-messages-title
  {:text-align :center})

(def no-pinned-messages-text
  {:text-align :center
   :margin-top 2})

(def list-footer
  {:height (when platform/android? 12)})
