(ns status-im.ui.components.camera
  (:require [goog.object :as object]
            [reagent.core :as reagent]
            [clojure.string :as string]
            [clojure.walk :as walk]
            [oops.core :refer [oget]]
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

(def camera (reagent/adapt-react-class RNCamera))

(defn get-qr-code-data [^js code]
  (when-let [data (.-data code)]
    (string/trim data)))

(defn on-layout [layout]
  (fn [evt]
    (reset! layout {:width (oget evt "nativeEvent" "layout" "width")
                    :height (oget evt "nativeEvent" "layout" "height")})))

(defn on-tap [camera-ref layout focus-object]
  (fn [coord]
    (when (and @camera-ref (:width @layout))
      (let [{:keys [width height]} @layout
            {:keys [x y]} (js->clj coord :keywordize-keys true)]
        (reset! focus-object (clj->js {:x (/ x width) :y (/ y height) :autoExposure true}))))))