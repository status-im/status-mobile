(ns syng-im.components.invertible-scroll-view
  (:require [reagent.core :as r]))

(set! js/InvertibleScrollView (js/require "react-native-invertible-scroll-view"))

(def invertible-scroll-view (r/adapt-react-class js/Spinner))
;; (defn invertible-scroll-view [props]
;;   (js/React.createElement js/InvertibleScrollView
;;                           (clj->js (merge {:inverted true} props))))
