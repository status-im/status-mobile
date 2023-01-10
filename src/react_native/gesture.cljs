(ns react-native.gesture
  (:require ["react-native-gesture-handler" :refer
             (GestureDetector Gesture gestureHandlerRootHOC TapGestureHandler State)]
            [reagent.core :as reagent]))

(def gesture Gesture)

(def gesture-detector (reagent/adapt-react-class GestureDetector))

(def gesture-handler-root-hoc gestureHandlerRootHOC)

(defn gesture-pan [] (.Pan ^js Gesture))

(defn on-update [^js pan handler] (.onUpdate pan handler))

(defn on-start [^js pan handler] (.onStart pan handler))

(defn on-end [^js pan handler] (.onEnd pan handler))
