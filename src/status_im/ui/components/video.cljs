(ns status-im.ui.components.video
  (:require [reagent.core :as reagent]
            [goog.object :as object]))

(def rn-video (js/require "react-native-video"))

(def video (reagent/adapt-react-class (object/get rn-video "default")))
