(ns status-im.components.action-button
  (:require [reagent.core :as r]))

(def class (js/require "react-native-action-button"))

(def action-button (r/adapt-react-class (.-default class)))
(def action-button-item (r/adapt-react-class (.. class -default -Item)))
