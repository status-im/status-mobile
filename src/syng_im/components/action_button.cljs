(ns syng-im.components.action-button
  (:require [reagent.core :as r]))

(set! js/window.ActionButton (js/require "react-native-action-button"))

(def action-button (r/adapt-react-class (.-default js/ActionButton)))
(def action-button-item (r/adapt-react-class (.. js/ActionButton -default -Item)))