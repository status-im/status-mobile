(ns status-im.ui.components.qr-code
  (:require [reagent.core :as r]
            [status-im.react-native.js-dependencies :as rn-dependencies]))

(defn qr-code [props]
  (r/create-element
    rn-dependencies/qr-code
    (clj->js (merge {:inverted true} props))))
