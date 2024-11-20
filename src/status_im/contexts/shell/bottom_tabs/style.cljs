(ns status-im.contexts.shell.bottom-tabs.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]
    [status-im.contexts.shell.utils :as utils]))

(defn bottom-tabs-container
  [height]
  [{:height height}
   {:background-color    colors/neutral-100
    :align-items         :center
    :height              (utils/bottom-tabs-container-height)
    :accessibility-label :bottom-tabs-container}])

(defn bottom-tabs
  []
  {:flex-direction      :row
   :position            :absolute
   :bottom              (if platform/android? 8 34)
   :flex                1
   :accessibility-label :bottom-tabs})
