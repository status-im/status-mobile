(ns status-im.contexts.shell.bottom-tabs.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]
    [status-im.contexts.shell.constants :as constants]
    [status-im.contexts.shell.utils :as utils]))

(defn bottom-tabs-container
  [privacy-mode-enabled?]
  [{:background-color    (if privacy-mode-enabled?
                           colors/neutral-80-opa-40
                           colors/neutral-100)
    :align-items         :center
    :margin-top          (- constants/home-stack-radius)
    :height              (utils/bottom-tabs-container-height)
    :accessibility-label :bottom-tabs-container}])

(defn bottom-tabs
  []
  {:flex-direction      :row
   :position            :absolute
   :bottom              (if platform/android? 8 34)
   :flex                1
   :accessibility-label :bottom-tabs})
