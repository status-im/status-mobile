(ns react-native.view-shot
  (:require ["react-native-view-shot" :as view-shot]
            [reagent.core :as reagent]))

(def view (reagent/adapt-react-class view-shot/default))

(defn capture
  [^js ref]
  (some-> ^js ref
          (.capture)))

(defn release-capture
  [^js ref uri]
  (some-> ^js ref
          (.releaseCapture uri)))
