(ns quo.animated
  (:refer-clojure :exclude [abs set delay divide])
  (:require ["react-native-reanimated" :default animated :refer (clockRunning EasingNode)]
            ["react-native-redash" :as redash]
            [oops.core :refer [ocall oget]]
            [quo.gesture-handler :as gh]
            quo.react
            [quo.react-native :as rn]
            [reagent.core :as reagent])
  (:require-macros [quo.react :refer [maybe-js-deps]]))

(def create-animated-component (comp reagent/adapt-react-class (.-createAnimatedComponent animated)))

(def view (reagent/adapt-react-class (.-View animated)))
(def text (reagent/adapt-react-class (.-Text animated)))
(def scroll-view (reagent/adapt-react-class (.-ScrollView animated)))
(def code (reagent/adapt-react-class (.-Code animated)))
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

(def eq (oget animated "eq"))
(def neq (oget animated "neq"))
(def greater (oget animated "greaterThan"))
(def greater-or-eq (oget animated "greaterOrEq"))
(def less (oget animated "lessThan"))
(def less-or-eq (oget animated "lessOrEq"))
(def not* (oget animated "not"))
(def or* (oget animated "or"))
(def and* (oget animated "and"))

(def diff (oget animated "diff"))
(def add (oget animated "add"))
(def sub (oget animated "sub"))
(def multiply (oget animated "multiply"))
(def divide (oget animated "divide"))
(def abs (oget animated "abs"))

(def min* (oget animated "min"))
(def max* (oget animated "max"))

(def set (oget animated "set"))
(def start-clock (oget animated "startClock"))
(def stop-clock (oget animated "stopClock"))
(def clock-running clockRunning)
(def bezier (.-bezier ^js EasingNode))
(def linear (.-linear ^js EasingNode))

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

(defn set-value
  [anim val]
  (ocall anim "setValue" val))

(def Value (oget animated "Value"))

(defn value
  [x]
  (new Value x))

(def Clock (oget animated "Clock"))

(defn clock
  []
  (new Clock))

(def debug (oget animated "debug"))
(def log (oget animated "log"))

(defn event
  ([config]
   (event config {}))
  ([config options]
   (ocall animated "event" (clj->js config) (clj->js options))))

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

(defn block
  [opts]
  (.block ^js animated (to-array opts)))

(defn interpolate
  [anim-value config]
  (.interpolateNode ^js animated anim-value (clj->js config)))

(defn call*
  [args callback]
  (.call ^js animated (to-array args) callback))

(defn timing
  [clock-value opts config]
  (.timing ^js animated
           clock-value
           (clj->js opts)
           (clj->js config)))

(defn spring
  [clock-value opts config]
  (.spring ^js animated
           clock-value
           (clj->js opts)
           (clj->js config)))

(def extrapolate {:clamp (oget animated "Extrapolate" "CLAMP")})

;; utilities

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
  [val config]
  (.withSpringTransition ^js redash val (clj->js config)))

(defn with-timing-transition
  [val config]
  (.withTimingTransition ^js redash val (clj->js config)))

(defn use-spring-transition
  [val config]
  (.useSpringTransition ^js redash val (clj->js config)))

(defn use-timing-transition
  [val config]
  (.useTimingTransition ^js redash val (clj->js config)))

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
  [value velocity snap-points]
  (.snapPoint ^js redash value velocity (to-array snap-points)))

(defn cancelable-loop
  [{:keys [clock duration finished on-reach]}]
  (let [time       (value 0)
        frame-time (value 0)
        position   (value 0)
        to-value   (value 1)
        state      {:time      time
                    :frameTime frame-time
                    :finished  finished
                    :position  position}
        config     {:toValue  to-value
                    :duration duration
                    :easing   (:linear easings)}]
    (block
     [(timing clock state config)
      (cond* (and* finished
                   (eq position to-value))
             (call* [] on-reach))
      (cond* finished
             [(set finished 0)
              (set time 0)
              (set frame-time 0)
              (set position 0)])
      position])))

(defn with-easing
  [{val   :value
    :keys [snap-points velocity offset state easing duration
           animation-over]
    :or   {duration       250
           animation-over (value 1)
           easing         (:ease-out easings)}}]
  (let [position         (value 0)
        c                (clock)
        interrupted      (and* (eq state (:began gh/states))
                               (clock-running c))
        vel              (multiply velocity 1.5)
        to               (snap-point position vel snap-points)
        finish-animation [(set offset position)
                          (stop-clock c)
                          (set animation-over 1)]]
    (block
     [(cond* interrupted finish-animation)
      (cond* animation-over
             (set position offset))
      (cond* (neq state (:end gh/states))
             [(set animation-over 0)
              (set position (add offset val))])
      (cond* (and* (eq state (:end gh/states))
                   (not* animation-over))
             [(set position
                   (re-timing
                    {:clock    c
                     :easing   easing
                     :duration duration
                     :from     position
                     :to       to}))
              (cond* (not* (clock-running c))
                     finish-animation)])
      position])))
