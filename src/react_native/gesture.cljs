(ns react-native.gesture
  (:require ["react-native-gesture-handler" :refer (GestureDetector Gesture)]
            [reagent.core :as reagent]))

(def gesture-detector (reagent/adapt-react-class GestureDetector))

(defn gesture-pan [] (.Pan ^js Gesture))

(defn on-update [^js pan handler] (.onUpdate pan handler))

(defn on-start [^js pan handler] (.onStart pan handler))

(defn on-end [^js pan handler] (.onEnd pan handler))