(ns syng-im.components.invertible-scroll-view
  (:require [reagent.core :as r]))

(def react-invertible-scroll-view (js/require "react-native-invertible-scroll-view"))

(def invertible-scroll-view (r/adapt-react-class react-invertible-scroll-view))
