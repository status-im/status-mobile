(ns status-im.contexts.chat.messenger.composer.gradients.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.reanimated :as reanimated]
    [status-im.contexts.chat.messenger.composer.constants :as constants]))

(defn- top-gradient-style
  [opacity z-index showing-extra-space?]
  (reanimated/apply-animations-to-style
   {:opacity opacity}
   {:height   (when (pos-int? z-index) 80)
    :position :absolute
    :z-index  z-index
    :top      (+ constants/bar-container-height
                 (if showing-extra-space?
                   constants/edit-container-height
                   0))
    :left     0
    :right    0}))

(defn top-gradient
  [opacity z-index showing-extra-space? theme]
  {:colors [(colors/theme-colors colors/white-opa-0 colors/neutral-95-opa-0 theme)
            (colors/theme-colors colors/white colors/neutral-95 theme)]
   :start  {:x 0 :y 1}
   :end    {:x 0 :y 0}
   :style  (top-gradient-style opacity z-index showing-extra-space?)})

(def bottom-gradient-style
  {:height   constants/line-height
   :position :absolute
   :bottom   0
   :left     0
   :right    0})

(defn bottom-gradient
  [theme]
  {:colors [(colors/theme-colors colors/white colors/neutral-95 theme)
            (colors/theme-colors colors/white-opa-0 colors/neutral-95-opa-0 theme)]
   :start  {:x 0 :y 1}
   :end    {:x 0 :y 0}
   :style  bottom-gradient-style})
