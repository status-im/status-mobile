(ns status-im2.contexts.shell.jump-to.components.jump-to-screen.style
  (:require [quo2.foundations.colors :as colors]
            [status-im2.contexts.shell.jump-to.utils :as utils]))

;;;; Placeholder
(defn placeholder-container
  [status-bar-height]
  {:position            :absolute
   :top                 (+ 112 status-bar-height)
   :left                0
   :right               0
   :bottom              (utils/bottom-tabs-container-height)
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

;;;; Shell
(defn jump-to-text
  [status-bar-height]
  {:color         colors/white
   :margin-top    (+ 68 status-bar-height)
   :margin-bottom 20
   :margin-left   20})

(def jump-to-list
  {:top      0
   :left     0
   :right    0
   :bottom   0
   :position :absolute})

(defn top-nav-blur-overlay-container
  [height pass-through?]
  {:height           height
   :position         :absolute
   :left             0
   :top              0
   :right            0
   :overflow         :hidden
   :background-color (if pass-through? :transparent colors/neutral-100)})

(def top-nav-blur-overlay
  {:height           100
   :position         :absolute
   :left             0
   :right            0
   :top              0
   :background-color colors/neutral-100-opa-70-blur})
