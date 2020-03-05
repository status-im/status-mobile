(ns status-im.ui.components.reanimated
  (:refer-clojure :exclude [set])
  (:require [reagent.core :as reagent]
            [oops.core :refer [oget ocall]]
            [status-im.react-native.js-dependencies :as js-deps]))

(def animated (oget js-deps/react-native-reanimated "default"))
(def createAnimatedComponent (oget animated "createAnimatedComponent"))

(def view (reagent/adapt-react-class (oget animated "View")))
(def text (reagent/adapt-react-class (oget animated "Text")))
(def scroll-view (reagent/adapt-react-class (oget animated "ScrollView")))
(def code (reagent/adapt-react-class (oget animated "Code")))

(def clock-running (oget js-deps/react-native-reanimated "clockRunning"))
(def Easing (oget js-deps/react-native-reanimated "Easing"))

(def eq (oget animated "eq"))
(def neq (oget animated "neq"))
(def greater-or-eq (oget animated "greaterOrEq"))
(def not* (oget animated "not"))
(def or* (oget animated "or"))
(def and* (oget animated "and"))

(def add (oget animated "add"))
(def sub (oget animated "sub"))
(def multiply (oget animated "multiply"))
(def abs (oget animated "abs"))

(def min* (oget animated "min"))
(def max* (oget animated "max"))

(def set (oget animated "set"))
(def start-clock (oget animated "startClock"))
(def stop-clock (oget animated "stopClock"))
(def bezier (oget Easing "bezier"))
(def linear (oget Easing "linear"))

(defn set-value [anim val]
  (ocall anim "setValue" val))

(def Value (oget animated "Value"))

(defn value [x]
  (new Value x))

(def Clock (oget animated "Clock"))

(defn clock []
  (new Clock))

(def debug (oget animated "debug"))
(def log (oget animated "log"))

(defn event
  ([config]
   (event config {}))
  ([config options]
   (ocall animated "event" (clj->js config) (clj->js options))))

(defn on-change [state node]
  (ocall animated "onChange"
         state
         (if (vector? node)
           (clj->js node)
           node)))

(defn cond*
  ([condition node]
   (ocall animated "cond"
          condition
          (if (vector? node)
            (clj->js node)
            node)))
  ([condition if-node else-node]
   (ocall animated "cond"
          condition
          (if (vector? if-node)
            (clj->js if-node)
            if-node)
          (if (vector? else-node)
            (clj->js else-node)
            else-node))))

(defn block [opts]
  (ocall animated "block" (clj->js opts)))

(defn interpolate [anim-value config]
  (ocall anim-value "interpolate" (clj->js config)))

(defn call* [args callback]
  (ocall animated "call" (clj->js args) callback))

(defn timing [clock-value opts config]
  (ocall animated "timing" clock-value
         (clj->js opts) (clj->js config)))

(defn spring [clock-value opts config]
  (ocall animated "spring" clock-value
         (clj->js opts) (clj->js config)))

(def extrapolate {:clamp (oget animated "Extrapolate" "CLAMP")})

;; Gesture handler

(def tap-gesture-handler
  (reagent/adapt-react-class
   (oget js-deps/react-native-gesture-handler "TapGestureHandler")))

(def pan-gesture-handler
  (reagent/adapt-react-class
   (oget js-deps/react-native-gesture-handler "PanGestureHandler")))

(def long-press-gesture-handler
  (reagent/adapt-react-class
   (oget js-deps/react-native-gesture-handler "LongPressGestureHandler")))

(def pure-native-button (oget js-deps/react-native-gesture-handler "PureNativeButton"))

(def touchable-without-feedback-class
  (oget js-deps/react-native-gesture-handler "TouchableWithoutFeedback"))

(def createNativeWrapper
  (oget js-deps/react-native-gesture-handler "createNativeWrapper"))

(def touchable-without-feedback
  (reagent/adapt-react-class touchable-without-feedback-class))

(def animated-raw-button
  (reagent/adapt-react-class
   (createNativeWrapper
    (createAnimatedComponent touchable-without-feedback-class))))

(def state (oget js-deps/react-native-gesture-handler "State"))

(def states {:began        (oget state "BEGAN")
             :active       (oget state "ACTIVE")
             :cancelled    (oget state "CANCELLED")
             :end          (oget state "END")
             :failed       (oget state "FAILED")
             :undetermined (oget state "UNDETERMINED")})

;; utilities

(def redash js-deps/react-native-redash)

(def clamp (oget redash "clamp"))

(defn with-spring [config]
  (ocall redash "withSpring" (clj->js config)))

(defn with-timing [val config]
  (ocall redash "withTimingTransition" val (clj->js config)))

(defn re-timing [config]
  (ocall redash "timing" (clj->js config)))

(defn on-scroll [opts]
  (ocall redash "onScroll" (clj->js opts)))
