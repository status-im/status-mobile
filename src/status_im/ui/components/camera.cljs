(ns status-im.ui.components.camera
  (:require [goog.object :as object]
            [reagent.core :as reagent]
            [clojure.walk :as walk]
            [status-im.react-native.js-dependencies :as js-dependencies]))

(defn default-camera [] (.-RNCamera (js-dependencies/camera)))

(defn constants [t]
  (-> (default-camera)
      (object/get "Constants")
      (object/get t)
      (js->clj)
      (walk/keywordize-keys)))

(def aspects (constants "Orientation"))
(def capture-targets (constants "CaptureTarget"))
(def torch-modes (constants "FlashMode"))

(defn set-torch [state]
  (set! (.-flashMode (default-camera)) (get torch-modes state)))

(defn request-access-ios [then else]
  (-> (.checkVideoAuthorizationStatus (default-camera))
      (.then (fn [allowed?] (if allowed? (then) (else))))
      (.catch else)))

(defn camera [props]
  (reagent/create-element (default-camera) (clj->js (merge {:inverted true} props))))

(defn get-qr-code-data [code]
  (.-data code))
