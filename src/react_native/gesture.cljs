(ns react-native.gesture
  (:require ["react-native-gesture-handler" :refer
             (GestureDetector Gesture gestureHandlerRootHOC)]
            [reagent.core :as reagent]))

(def gesture-detector (reagent/adapt-react-class GestureDetector))

(def gesture-handler-root-hoc gestureHandlerRootHOC)

(defn gesture-tap [] (.Tap ^js Gesture))

(defn gesture-pan [] (.Pan ^js Gesture))

(defn gesture-pinch [] (.Pinch ^js Gesture))

(defn on-begin [gesture handler] (.onBegin ^js gesture handler))

(defn on-start [gesture handler] (.onStart ^js gesture handler))

(defn on-update [gesture handler] (.onUpdate ^js gesture handler))

(defn on-end [gesture handler] (.onEnd ^js gesture handler))

(defn number-of-taps [gesture count] (.numberOfTaps ^js gesture count))

(defn enabled [gesture enabled?] (.enabled ^js gesture enabled?))

(defn average-touches [gesture average-touches?] (.averageTouches ^js gesture average-touches?))

(defn simultaneous
  ([g1 g2] (.Simultaneous ^js Gesture g1 g2))
  ([g1 g2 g3] (.Simultaneous ^js Gesture g1 g2 g3)))

(defn exclusive [g1 g2] (.Exclusive ^js Gesture g1 g2))
