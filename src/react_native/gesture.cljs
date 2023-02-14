(ns react-native.gesture
  (:require ["react-native-gesture-handler" :refer
             (GestureDetector Gesture gestureHandlerRootHOC)]
            [reagent.core :as reagent]))

(def gesture-detector (reagent/adapt-react-class GestureDetector))

(def gesture-handler-root-hoc gestureHandlerRootHOC)

(defn gesture-tap [] (.Tap Gesture))

(defn gesture-pan [] (.Pan Gesture))

(defn gesture-pinch [] (.Pinch Gesture))

(defn on-start [gesture handler] (.onStart gesture handler))

(defn on-update [gesture handler] (.onUpdate gesture handler))

(defn on-end [gesture handler] (.onEnd gesture handler))

(defn number-of-taps [gesture count] (.numberOfTaps gesture count))

(defn enabled [gesture enabled?] (.enabled gesture enabled?))

(defn average-touches [gesture average-touches?] (.averageTouches gesture average-touches?))

(defn simultaneous
  ([g1 g2] (.Simultaneous Gesture g1 g2))
  ([g1 g2 g3] (.Simultaneous Gesture g1 g2 g3)))

(defn exclusive [g1 g2] (.Exclusive Gesture g1 g2))
