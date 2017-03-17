(ns status-im.components.native-action-button
  (:require [reagent.core :as r]))

(def class (js/require "react-native-action-button"))

(def native-action-button (r/adapt-react-class (.-default class)))
(def native-action-button-item (r/adapt-react-class (.. class -default -Item)))
