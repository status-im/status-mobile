(ns quo.animated
  (:refer-clojure :exclude [set])
  (:require [reagent.core :as reagent]
            [oops.core :refer [oget ocall]]
            ["react-native-reanimated" :default animated :refer (clockRunning Easing)]
            ["react-native-redash" :as redash]
            ["react-native" :as rn]
            [taoensso.timbre :as log]))

;(def view (reagent/adapt-react-class (.-View animated)))
;(def text (reagent/adapt-react-class (.-Text animated)))
;(def scroll-view (reagent/adapt-react-class (.-ScrollView animated)))
(def view (reagent/adapt-react-class (.-View ^js rn)))
(def text (reagent/adapt-react-class (.-Text ^js rn)))
(def scroll-view (reagent/adapt-react-class (.-ScrollView ^js rn)))

;(def code (reagent/adapt-react-class (.-Code animated)))
(def code view)


;(def eq (oget animated "eq"))

;(def neq (oget animated "neq"))
;(def greater-or-eq (oget animated "greaterOrEq"))
(def greater-or-eq nil)
;(def not* (oget animated "not"))
(def not* nil)
;(def or* (oget animated "or"))
(def or* nil)
;(def and* (oget animated "and"))
(def and* nil)

;(def add (oget animated "add"))
;(def sub (oget animated "sub"))
;(def multiply (oget animated "multiply"))
;(def abs (oget animated "abs"))

;(def min* (oget animated "min"))
;(def max* (oget animated "max"))

;(def set (oget animated "set"))
(def set nil)
;(def start-clock (oget animated "startClock"))
;(def stop-clock (oget animated "stopClock"))
;(def clock-running clockRunning)
;(def bezier (.-bezier ^js Easing))
;(def linear (.-linear ^js Easing))

;(def easings {:ease-in  (bezier 0.42 0 1 1)
;              :ease-out (bezier 0 0 0.58 1)})

(def easings nil)
;(defn set-value [anim val]
;  (ocall anim "setValue" val))

;(def Value (oget animated "Value"))
;
;(defn value [x]
;  (new Value x))

(defn value [x])

;(def Clock (oget animated "Clock"))

;(defn clock []
;  (new Clock))

;(def debug (oget animated "debug"))
;(def log (oget animated "log"))

;(defn event
;  ([config]
;   (event config {}))
;  ([config options]
;   (ocall animated "event" (clj->js config) (clj->js options))))

;(defn on-change [state node]
;  (ocall animated "onChange"
;         state
;         (if (vector? node)
;           (clj->js node)
;           node)))

;(defn cond*
;  ([condition node]
;   (ocall animated "cond"
;          condition
;          (if (vector? node)
;            (clj->js node)
;            node)))
;  ([condition if-node else-node]
;   (ocall animated "cond"
;          condition
;          (if (vector? if-node)
;            (clj->js if-node)
;            if-node)
;          (if (vector? else-node)
;            (clj->js else-node)
;            else-node))))
(defn cond*
  ([condition node]
   )
  ([condition if-node else-node]
   ))

;(defn block [opts]
;  (ocall animated "block" (clj->js opts)))

;(defn interpolate [anim-value config]
;  (ocall anim-value "interpolate" (clj->js config)))

(defn interpolate [anim-value config])

;(defn call* [args callback]
;  (ocall animated "call" (clj->js args) callback))

;(defn timing [clock-value opts config]
;  (ocall animated "timing" clock-value
;         (clj->js opts) (clj->js config)))

;(defn spring [clock-value opts config]
;  (ocall animated "spring" clock-value
;         (clj->js opts) (clj->js config)))

;(def extrapolate {:clamp (oget animated "Extrapolate" "CLAMP")})
(def extrapolate nil)
;; utilities

;(def clamp (oget redash "clamp"))

;(defn with-spring [config]
;  (ocall redash "withSpring" (clj->js config)))

(defn with-timing [val config])

;(defn re-timing [config]
;  (ocall redash "timing" (clj->js config)))

;(defn on-scroll [opts]
;  (ocall redash "onScrollEvent" (clj->js opts)))

(defn on-scroll [opts])


;(defn b-interpolate [anim-value a b]
;  (ocall redash "bInterpolate" anim-value a b))

(defn b-interpolate [anim-value a b])


;(defn loop* [opts]
;  (ocall redash "loop" (clj->js opts)))
(log/debug "quo.animated loading finished")