(ns status-im.components.invertible-scroll-view
  (:require [reagent.core :as r]))

(def class (js/require "react-native-invertible-scroll-view"))

(defn invertible-scroll-view [props]
  (r/create-element class (clj->js (merge {:inverted true} props))))

