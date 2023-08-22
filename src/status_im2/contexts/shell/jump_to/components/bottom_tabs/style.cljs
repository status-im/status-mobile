(ns status-im2.contexts.shell.jump-to.components.bottom-tabs.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.shell.jump-to.utils :as utils]))

(defn bottom-tabs-container
  [pass-through? height]
  (reanimated/apply-animations-to-style
   {:height height}
   {:background-color    (if pass-through? :transparent colors/neutral-100)
    :flex                1
    :align-items         :center
    :height              (utils/bottom-tabs-container-height)
    :position            :absolute
    :bottom              0
    :right               0
    :left                0
    :overflow            :hidden
    :accessibility-label :bottom-tabs-container}))

(defn bottom-tabs
  []
  {:flex-direction      :row
   :position            :absolute
   :bottom              (if platform/android? 8 34)
   :flex                1
   :accessibility-label :bottom-tabs})

(defn bottom-tabs-blur-overlay
  [height]
  (reanimated/apply-animations-to-style
   {:height height}
   {:position         :absolute
    :left             0
    :right            0
    :bottom           0
    :height           (utils/bottom-tabs-container-height)
    :background-color colors/neutral-100-opa-70-blur}))
