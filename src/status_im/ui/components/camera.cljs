(ns status-im.ui.components.camera
  (:require [reagent.core :as r]
            [clojure.walk :refer [keywordize-keys]]
            [status-im.utils.platform :as platform]
            [status-im.react-native.js-dependencies :as rn-dependecies]))

(def default-camera (.-default rn-dependecies/camera))

(defn constants [t]
  (-> (aget rn-dependecies/camera "constants" t)
      (js->clj)
      (keywordize-keys)))

(def aspects (constants "Aspect"))
(def capture-targets (constants "CaptureTarget"))
(def torch-modes (constants "TorchMode"))

(defn set-torch [state]
  (set! (.-torchMode default-camera) (get torch-modes state)))

(defn request-access [callback]
  (if platform/android?
      (callback true)
      (-> (.checkVideoAuthorizationStatus default-camera)
          (.then #(callback %))
          (.catch #(callback false)))))

(defn camera [props]
  (r/create-element default-camera (clj->js (merge {:inverted true} props))))

(defn get-qr-code-data [code]
  (.-data code))