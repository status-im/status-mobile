(ns status-im.ui.components.svg
  (:require [goog.object :as object]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [reagent.core :as reagent]))

(defn get-property [name]
  (object/get js-dependencies/svg name))

(defn adapt-class [class]
  (when class
    (reagent/adapt-react-class class)))

(defn get-class [name]
  (adapt-class (get-property name)))

(def svgxml (get-class "SvgXml"))