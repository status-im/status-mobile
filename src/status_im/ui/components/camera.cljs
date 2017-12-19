(ns status-im.ui.components.camera
  (:require [reagent.core :as r]
            [clojure.walk :refer [keywordize-keys]]
            [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]
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

(defn try-capture [{:keys [metadata]
                    :or {metadata {}}}]
  (fn [event]
    (try
      (.capture default-camera {:metadata metadata})
      (catch :default e
        (log/error "Error handling touch event on camera")))))

(defn wrap-touch-handler [m]
  (if platform/android?
    (assoc m :on-press (try-capture m))
    m))

(defn camera [props]
    (->> {:inverted true}
         wrap-touch-handler
         (merge props)
         clj->js
         (r/create-element default-camera)))

(defn get-qr-code-data [code]
  (.-data code))
