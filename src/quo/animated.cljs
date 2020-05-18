(ns quo.animated
  (:refer-clojure :exclude [set])
  (:require [reagent.core :as reagent]
            [oops.core :refer [oget ocall]]
            ["react-native-reanimated" :default animated :refer (clockRunning Easing)]
            ["react-native-redash" :as redash]))

(def view (reagent/adapt-react-class (.-View animated)))
(def text (reagent/adapt-react-class (.-Text animated)))
(def scroll-view (reagent/adapt-react-class (.-ScrollView animated)))
(def code (reagent/adapt-react-class (.-Code animated)))

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
(def clock-running clockRunning)
(def bezier (.-bezier ^js Easing))
(def linear (.-linear ^js Easing))

(def easings {:ease-in  (bezier 0.42 0 1 1)
              :ease-out (bezier 0 0 0.58 1)})

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
  (.block ^js animated (clj->js opts)))

(defn interpolate [anim-value config]
  (.interpolate ^js animated anim-value (clj->js config)))

(defn call* [args callback]
  (.call ^js animated (clj->js args) callback))

(defn timing [clock-value opts config]
  (.timing ^js animated
           clock-value
           (clj->js opts)
           (clj->js config)))

(defn spring [clock-value opts config]
  (.spring ^js animated clock-value
           (clj->js opts) (clj->js config)))

(def extrapolate {:clamp (oget animated "Extrapolate" "CLAMP")})

;; utilities

(def clamp (oget redash "clamp"))

(defn with-spring [config]
  (ocall redash "withSpring" (clj->js config)))

(defn with-spring-transition [val config]
  (.withSpringTransition ^js redash val (clj->js config)))

(defn with-timing-transition [val config]
  (.withTimingTransition ^js redash val (clj->js config)))

(defn re-timing [config]
  (ocall redash "timing" (clj->js config)))

(defn on-scroll [opts]
  (ocall redash "onScrollEvent" (clj->js opts)))

(defn on-gesture [opts]
  (let [gesture-event (event #js [#js {:nativeEvent (clj->js opts)}])]
    {:onHandlerStateChange gesture-event
     :onGestureEvent       gesture-event}))

(defn mix [anim-value a b]
  (.mix ^js redash anim-value a b))

(defn loop* [opts]
  (ocall redash "loop" (clj->js opts)))
