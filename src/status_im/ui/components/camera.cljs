(ns status-im.ui.components.camera
  (:require [goog.object :as object]
            [reagent.core :as reagent]
            [clojure.string :as string]
            [clojure.walk :as walk]
            ["react-native-camera" :refer (RNCamera)]))

(defn- constants [t]
  (-> RNCamera
      (object/get "Constants")
      (object/get t)
      (js->clj)
      (walk/keywordize-keys)))

(def aspects (constants "Orientation"))
(def capture-targets (constants "CaptureTarget"))
(def torch-modes (constants "FlashMode"))

(defn set-torch [state]
  (set! (.-flashMode RNCamera) (get torch-modes state)))

(defn request-access-ios [then else]
  (-> (.checkVideoAuthorizationStatus RNCamera)
      (.then (fn [allowed?] (if allowed? (then) (else))))
      (.catch else)))

(defn camera [props]
  (reagent/create-element RNCamera (clj->js (merge {:inverted true} props))))

(defn get-qr-code-data [^js code]
  (when-let [data (.-data code)]
    (string/trim data)))
