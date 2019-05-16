(ns status-im.ui.components.camera
  (:require [goog.object :as object]
            [reagent.core :as reagent]
            [clojure.walk :as walk]
            [status-im.react-native.js-dependencies :as js-dependecies]))

(defn default-camera [] (.-default (js-dependecies/camera)))

(defn constants [t]
  (-> (default-camera)
      (object/get "constants")
      (object/get t)
      (js->clj)
      (walk/keywordize-keys)))

(defn aspects [] (constants "Aspect"))
(defn capture-targets [] (constants "CaptureTarget"))
(defn torch-modes [] (constants "TorchMode"))

(defn set-torch [state]
  (set! (.-torchMode (default-camera)) (get (torch-modes) state)))

(defn request-access-ios [then else]
  (-> (.checkVideoAuthorizationStatus (default-camera))
      (.then (fn [allowed?] (if allowed? (then) (else))))
      (.catch else)))

(defn camera [props]
  (reagent/create-element (default-camera) (clj->js (merge {:inverted true} props))))

(defn get-qr-code-data [code]
  (.-data code))
