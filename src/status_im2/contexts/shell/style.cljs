(ns status-im2.contexts.shell.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]
            [status-im2.contexts.shell.animation :as animation]
            [status-im2.contexts.shell.constants :as shell.constants]))

;; Bottom Tabs
(defn bottom-tabs-container
  [pass-through?]
  {:background-color    (if pass-through? colors/neutral-100-opa-70 colors/neutral-100)
   :flex                1
   :align-items         :center
   :flex-direction      :column
   :height              (shell.constants/bottom-tabs-container-height)
   :position            :absolute
   :bottom              -1
   :right               0
   :left                0
   :accessibility-label :bottom-tabs-container})

(defn bottom-tabs
  []
  {:flex-direction      :row
   :position            :absolute
   :bottom              (if platform/android? 8 34)
   :flex                1
   :accessibility-label :bottom-tabs})

;; Home Stack
(defn home-stack
  []
  (let [{:keys [width height]} (shell.constants/dimensions)
        height                 (or @animation/screen-height height)]
    {:border-bottom-left-radius  20
     :border-bottom-right-radius 20
     :background-color           (colors/theme-colors colors/neutral-5 colors/neutral-95)
     :overflow                   :hidden
     :position                   :absolute
     :width                      width
     :height                     (- height (shell.constants/bottom-tabs-container-height))}))

;; Placeholder
(defn placeholder-container
  [status-bar-height]
  {:position            :absolute
   :top                 (+ 112 status-bar-height)
   :left                0
   :right               0
   :bottom              (shell.constants/bottom-tabs-container-height)
   :align-items         :center
   :accessibility-label :shell-placeholder-view})

(def placeholder-image
  {:margin-top    186
   :width         120
   :height        120
   ;; Code to remove once placeholder image/vector will be available
   :border-width  5
   :border-radius 10
   :border-color  :red})

(def placeholder-title
  {:margin-top 20
   :color      colors/white})

(def placeholder-subtitle
  {:margin-top 4
   :color      colors/white})

;; Shell
(defn jump-to-text
  [status-bar-height]
  {:color         colors/white
   :margin-top    (+ 68 status-bar-height)
   :margin-bottom 20
   :margin-left   20})
