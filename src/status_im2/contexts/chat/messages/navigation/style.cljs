(ns status-im2.contexts.chat.messages.navigation.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]))

(defonce ^:const navigation-bar-height 100)
(defonce ^:const header-offset 56)

(defn background-view
  [theme]
  {:position         :absolute
   :top              0
   :left             0
   :right            0
   :height           navigation-bar-height
   :background-color (colors/theme-colors colors/white-opa-70 colors/neutral-100-opa-70 theme)
   :display          :flex
   :flex-direction   :row
   :overflow         :hidden})

(defn animated-background-view
  [enabled? animation theme]
  (reanimated/apply-animations-to-style
   (when enabled?
     {:opacity animation})
   (background-view theme)))

(def blur-view
  {:position       :absolute
   :top            0
   :left           0
   :right          0
   :height         navigation-bar-height
   :display        :flex
   :flex-direction :row
   :overflow       :hidden})

(defn animated-blur-view
  [enabled? animation]
  (reanimated/apply-animations-to-style
   (when enabled?
     {:opacity animation})
   blur-view))

(defn navigation-view
  [loaded?]
  {:z-index  1
   :top      0
   :right    0
   :left     0
   :position :absolute
   :opacity  (if loaded? 1 0)})

(def header-container
  {:position           :absolute
   :top                header-offset
   :left               0
   :right              0
   :padding-bottom     8
   :padding-horizontal 20
   :display            :flex
   :flex-direction     :row
   :overflow           :hidden})

(def header
  {:flex 1})

(defn animated-header
  [enabled? y-animation opacity-animation]
  (reanimated/apply-animations-to-style
   ;; here using `top` won't work on Android, so we are using `translateY`
   (when enabled?
     {:transform [{:translateY y-animation}]
      :opacity   opacity-animation})
   header))

(def header-content-container
  {:flex-direction :row
   :align-items    :center
   :margin-left    12
   :margin-right   8
   :margin-top     -4
   :height         40})

(def header-avatar-container
  {:margin-right 8})

(def header-text-container
  {:flex 1})

(defn header-display-name
  []
  {:color (colors/theme-colors colors/black colors/white)})

(defn header-status
  []
  {:color (colors/theme-colors colors/neutral-80-opa-50 colors/white-opa-50)})
