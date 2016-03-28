(ns syng-im.components.spinner
  (:require [reagent.core :as r]))

(set! js/Spinner (.-default (js/require "react-native-loading-spinner-overlay")))

(def spinner (r/adapt-react-class js/Spinner))
;; (defn spinner [props]
;;   (js/React.createElement js/Spinner
;;                           (clj->js props)))
