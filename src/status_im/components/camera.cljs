(ns status-im.components.camera
  (:require [reagent.core :as r]))

(def class (.-default (js/require "react-native-camera")))

(defn camera [props]
  (r/create-element class (clj->js (merge {:inverted true} props))))