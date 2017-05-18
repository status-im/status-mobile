(ns status-im.components.camera
  (:require [reagent.core :as r]
            [clojure.walk :refer [keywordize-keys]]
            [status-im.utils.platform :as platform]))

(def camera-class (js/require "react-native-camera"))
(def camera-default (.-default camera-class))

(defn constants [t]
  (-> (aget camera-default "constants" t)
      (js->clj)
      (keywordize-keys)))

(def aspects (constants "Aspect"))
(def capture-targets (constants "CaptureTarget"))

(defn request-access [callback]
  (if platform/android?
      (callback true)
      (-> (.checkVideoAuthorizationStatus camera-default)
          (.then #(callback %))
          (.catch #(callback false)))))

(defn camera [props]
  (r/create-element camera-default (clj->js (merge {:inverted true} props))))
