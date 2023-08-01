(ns status-im2.contexts.chat.home.style
  (:require [react-native.platform :as platform]
            [react-native.safe-area :as safe-area]))

(def tabs
  {:padding-horizontal 20
   :padding-top        16
   :padding-bottom     12})

(def blur
  {:position :absolute
   :top      0
   :right    0
   :left     0
   :bottom   0})

(defn blur-container
  []
  {:overflow    (if platform/ios? :visible :hidden)
   :position    :absolute
   :z-index     1
   :top         0
   :right       0
   :left        0
   :padding-top (safe-area/get-top)})
