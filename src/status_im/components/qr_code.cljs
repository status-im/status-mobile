(ns status-im.components.qr-code
  (:require [reagent.core :as r]))

(def class (js/require "react-native-qrcode"))

(defn qr-code [props]
  (r/create-element
    class
    (clj->js (merge {:inverted true} props))))
