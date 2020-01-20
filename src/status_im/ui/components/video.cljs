(ns status-im.ui.components.video
  (:require [reagent.core :as reagent]
            [status-im.ui.components.react :as react]
            [goog.object :as object]))

(def rn-video (js/require "react-native-video"))

(def video-player (reagent/adapt-react-class (object/get rn-video "default")))

(defn video [props]
  [react/view {:style (:style props)}
   [video-player props]
   [react/view {:style {:position     :absolute
                        :top          -4
                        :bottom       -4
                        :left         -4
                        :right        -4
                        :border-color :white
                        :border-width 8}}]])
