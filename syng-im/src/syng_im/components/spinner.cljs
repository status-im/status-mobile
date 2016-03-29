(ns syng-im.components.spinner
  (:require [reagent.core :as r]))

(def react-spinner (.-default (js/require "react-native-loading-spinner-overlay")))

(def spinner (r/adapt-react-class react-spinner))
