(ns status-im.contexts.chat.messenger.messages.navigation.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.reanimated :as reanimated]))

(defn navigation-view
  [navigation-view-height pinned-banner-height]
  {:top            0
   :left           0
   :right          0
   :position       :absolute
   :pointer-events :box-none
   :height         (+ navigation-view-height pinned-banner-height)
   :z-index        1})

(defn animated-background-view
  [background-opacity navigation-view-height]
  (reanimated/apply-animations-to-style
   {:opacity background-opacity}
   {:height   navigation-view-height
    :top      0
    :left     0
    :right    0
    :overflow :hidden
    :position :absolute}))

(defn header-container
  [top-insets top-bar-height]
  {:margin-top         top-insets
   :flex-direction     :row
   :padding-horizontal 20
   :overflow           :hidden
   :height             top-bar-height
   :align-items        :center})

(defn button-animation-container
  [opacity-value]
  (reanimated/apply-animations-to-style
   {:opacity opacity-value}
   {}))

;;;; Content

(defn header-content-container
  [header-opacity header-position]
  (reanimated/apply-animations-to-style
   {:transform [{:translate-y header-position}]
    :opacity   header-opacity}
   {:flex-direction    :row
    :align-items       :center
    :flex              1
    :margin-horizontal 12
    :height            40}))

(def header-text-container
  {:margin-left 8})

(defn header-display-name
  [theme]
  {:color (colors/theme-colors colors/black colors/white theme)})

(defn header-status
  [theme]
  {:color (colors/theme-colors colors/neutral-80-opa-50 colors/white-opa-40 theme)})
