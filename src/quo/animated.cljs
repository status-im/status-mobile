(ns quo.animated
  (:refer-clojure :exclude [abs set delay divide])
  (:require ["react-native-reanimated" :default animated :refer
             (clockRunning Easing Extrapolate)]
            ["react-native-redash" :as redash]
            [oops.core :refer [ocall oget]]
            [quo.gesture-handler :as gh]
            [quo.react]
            [quo.react-native :as rn]
            [reagent.core :as reagent])
  (:require-macros [quo.react :refer [maybe-js-deps]]))

(def create-animated-component (comp reagent/adapt-react-class (.-createAnimatedComponent animated)))

(def view (reagent/adapt-react-class (.-View animated)))
(def text (reagent/adapt-react-class (.-Text animated)))
(def scroll-view (reagent/adapt-react-class (.-ScrollView animated)))

;;;; Animated.Code is deprecated with reanimated version 1.
;;;; commented out to upgrade react-native-reanimated to v3 and react-native to 0.72
;;;;; TODO: replace this with an updated implementation
;(def code (reagent/adapt-react-class (.-Code animated)))
(def animated-flat-list (create-animated-component gh/flat-list-raw))

(defn flat-list
  [props]
  [animated-flat-list (rn/base-list-props props)])

(def useCode (.-useCode animated))

(defn code!
  ([setup-fn]
   (useCode
    (fn [] (setup-fn))))
  ([setup-fn deps]
   (useCode
    (fn [] (setup-fn))
    (maybe-js-deps deps))))

;;;; commented out to upgrade react-native-reanimated to v3 and react-native to 0.72
;;;;; TODO: replace this with an updated implementation
;;;ERROR  Error: Oops, Missing expected object key 'eq'
;(def eq (oget animated "eq"))
;;; ERROR  Error: Oops, Missing expected object key 'neq'
;(def neq (oget animated "neq"))
;;;ERROR  Error: Oops, Missing expected object key 'greaterThan'
;(def greater (oget animated "greaterThan"))
;;;ERROR  Error: Oops, Missing expected object key 'greaterOrEq'
;(def greater-or-eq (oget animated "greaterOrEq"))
;;;ERROR  Error: Oops, Missing expected object key 'lessThan'
;(def less (oget animated "lessThan"))
;;;ERROR  Error: Oops, Missing expected object key 'lessOrEq'
;(def less-or-eq (oget animated "lessOrEq"))
;;;ERROR  Error: Oops, Missing expected object key 'not'
;(def not* (oget animated "not"))
;;;ERROR  Error: Oops, Missing expected object key 'or'
;(def or* (oget animated "or"))
;;;ERROR  Error: Oops, Missing expected object key 'and'
;(def and* (oget animated "and"))

;;;ERROR  Error: Oops, Missing expected object key 'diff'
;(def diff (oget animated "diff"))
;;;ERROR  Error: Oops, Missing expected object key 'add'
;(def add (oget animated "add"))
;;;ERROR  Error: Oops, Missing expected object key 'add'
;(def sub (oget animated "sub"))
;;;ERROR  Error: Oops, Missing expected object key 'multiply'
;(def multiply (oget animated "multiply"))
;;;ERROR  Error: Oops, Missing expected object key 'divide'
;(def divide (oget animated "divide"))
;;;ERROR  Error: Oops, Missing expected object key 'abs'
;(def abs (oget animated "abs"))

;;;ERROR  Error: Oops, Missing expected object key 'min'
;(def min* (oget animated "min"))
;;;ERROR  Error: Oops, Missing expected object key 'max'
;(def max* (oget animated "max"))

;;;ERROR  Error: Oops, Missing expected object key 'set'
;(def set (oget animated "set"))
;;;ERROR  Error: Oops, Missing expected object key 'startClock'
;(def start-clock (oget animated "startClock"))
;;;ERROR  Error: Oops, Missing expected object key 'stopClock'
;(def stop-clock (oget animated "stopClock"))

(def clock-running clockRunning)

(def bezier (.-bezier ^js Easing))

(def linear (.-linear ^js Easing))

(def easings
  {:linear      linear
   :ease-in     (bezier 0.42 0 1 1)
   :ease-out    (bezier 0 0 0.58 1)
   :ease-in-out (bezier 0.42 0 0.58 1)
   :cubic       (bezier 0.55 0.055 0.675 0.19)
   :keyboard    (bezier 0.17 0.59 0.4 0.77)})

(def springs
  {:lazy {:damping           50
          :mass              0.3
          :stiffness         120
          :overshootClamping true
          :bouncyFactor      1}
   :jump {:damping                   13
          :mass                      0.5
          :stiffness                 170
          :overshootClamping         false
          :bouncyFactor              1
          :restSpeedThreshold        0.001
          :restDisplacementThreshold 0.001}})
;
(defn set-value
  [anim v]
  (ocall anim "setValue" v))

;;;; commented out to upgrade react-native-reanimated to v3 and react-native to 0.72
;;;;; TODO: replace this with an updated implementation
;;;ERROR  Error: Oops, Missing expected object key 'Value'
;(def Value (oget animated "Value"))
;;;ERROR  Error: Oops, Missing expected object key 'Value'
;(defn value
;  [x]
;  (new Value x))

;;;ERROR  Error: Oops, Missing expected object key 'Clock'
;(def Clock (oget animated "Clock"))
;;;ERROR  Error: Oops, Missing expected object key 'Clock'
;(defn clock
;  []
;  (new Clock))

;;;ERROR  Error: Oops, Missing expected object key 'debug'
;(def debug (oget animated "debug"))

;;;ERROR  Error: Oops, Missing expected object key 'log'
;(def log (oget animated "log"))
;
(defn event
  ([config]
   (event config {}))
  ([config options]
   (ocall animated "event" (clj->js config) (clj->js options))))
;
(defn on-change
  [state node]
  (ocall animated
         "onChange"
         state
         (if (vector? node)
           (clj->js node)
           node)))

(defn cond*
  ([condition node]
   (.cond ^js animated
          condition
          (if (vector? node)
            (clj->js node)
            node)))
  ([condition if-node else-node]
   (.cond ^js animated
          condition
          (if (vector? if-node)
            (clj->js if-node)
            if-node)
          (if (vector? else-node)
            (clj->js else-node)
            else-node))))
;
(defn block
  [opts]
  (.block ^js animated (to-array opts)))


(defn call*
  [args callback]
  (.call ^js animated (to-array args) callback))

(defn timing
  [clock-value opts config]
  (.timing ^js animated
           clock-value
           (clj->js opts)
           (clj->js config)))
;
(defn spring
  [clock-value opts config]
  (.spring ^js animated
           clock-value
           (clj->js opts)
           (clj->js config)))

(def extrapolate {:clamp (oget Extrapolate "CLAMP")})

;;; utilities

(def clamp (oget redash "clamp"))
(def diff-clamp (.-diffClamp ^js redash))

(defn with-spring
  [config]
  (ocall redash "withSpring" (clj->js config)))

(defn with-decay
  [config]
  (.withDecay ^js redash (clj->js config)))

(defn with-offset
  [config]
  (.withOffset ^js redash (clj->js config)))

(defn with-spring-transition
  [v config]
  (.withSpringTransition ^js redash v (clj->js config)))

(defn with-timing-transition
  [v config]
  (.withTimingTransition ^js redash v (clj->js config)))

(defn use-spring-transition
  [v config]
  (.useSpringTransition ^js redash v (clj->js config)))

(defn use-timing-transition
  [v config]
  (.useTimingTransition ^js redash v (clj->js config)))

(defn re-timing
  [config]
  (.timing ^js redash (clj->js config)))

(defn re-spring
  [config]
  (.spring ^js redash (clj->js config)))

(defn on-scroll
  [opts]
  (ocall redash "onScrollEvent" (clj->js opts)))

(defn on-gesture
  [opts]
  (let [gesture-event (event #js [#js {:nativeEvent (clj->js opts)}])]
    {:onHandlerStateChange gesture-event
     :onGestureEvent       gesture-event}))

(def mix (.-mix ^js redash))

(def mix-color (.-mixColor ^js redash))

(def delay (.-delay ^js redash))

(defn loop*
  [opts]
  (ocall redash "loop" (clj->js opts)))

(def use-value (.-useValue ^js redash))

(def use-clock (.-useClock ^js redash))

(defn use-gesture
  [opts]
  (let [gesture (.useGestureHandler ^js redash (clj->js opts))]
    {:onHandlerStateChange (.-onHandlerStateChange ^js gesture)
     :onGestureEvent       (.-onGestureEvent ^js gesture)}))

(defn snap-point
  [v velocity snap-points]
  (.snapPoint ^js redash v velocity (to-array snap-points)))
