(ns status-im.components.invertible-scroll-view)

(def class (js/require "react-native-invertible-scroll-view"))

(defn invertible-scroll-view [props]
  (js/React.createElement class (clj->js (merge {:inverted true} props))))

