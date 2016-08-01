(ns status-im.components.camera
  (:require [reagent.core :as r]
            [clojure.walk :refer [keywordize-keys]]))

(def camera-class (js/require "react-native-camera"))

(defn constants [t]
  (-> (aget camera-class "default" "constants" t)
      (js->clj)
      (keywordize-keys)))

(def aspects (constants "Aspect"))
(def capture-targets (constants "CaptureTarget"))

(defn camera [props]
  (r/create-element (.-default camera-class) (clj->js (merge {:inverted true} props))))
