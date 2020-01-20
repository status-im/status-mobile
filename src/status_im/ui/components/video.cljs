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
                        :top          0
                        :bottom       0
                        :left         0
                        :right        0
                        :border-color :white
                        :border-width 4}}]])
