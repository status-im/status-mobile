(ns syng-im.components.react
  (:require [reagent.core :as r]))

(set! js/React (js/require "react-native"))

(def app-registry (.-AppRegistry js/React))
(def navigator (r/adapt-react-class (.-Navigator js/React)))
(def text (r/adapt-react-class (.-Text js/React)))
(def view (r/adapt-react-class (.-View js/React)))
(def image (r/adapt-react-class (.-Image js/React)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight js/React)))