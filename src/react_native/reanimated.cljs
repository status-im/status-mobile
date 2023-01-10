(ns react-native.reanimated
  (:require ["react-native" :as rn]
            ["react-native-linear-gradient" :default LinearGradient]
            ["react-native-fast-image" :as FastImage]
            ["@react-native-community/blur" :as blur]
            ["react-native-reanimated" :default reanimated :refer
             (useSharedValue useAnimatedStyle
                             withTiming
                             withDelay
                             withSpring
                             withRepeat
                             withDecay
                             Easing
                             Keyframe
                             cancelAnimation
                             SlideInUp
                             SlideOutUp
                             LinearTransition)]
            [reagent.core :as reagent]
            [react-native.flat-list :as rn-flat-list]
            [utils.collection]))

;;; Constants
(def ^:const default-duration 300)

;;; Worklets
(def worklet-factory (js/require "../src/js/worklet_factory.js"))

;;; Animated Components
(def create-animated-component (comp reagent/adapt-react-class (.-createAnimatedComponent reanimated)))

(def view (reagent/adapt-react-class (.-View reanimated)))
(def image (reagent/adapt-react-class (.-Image reanimated)))
(def reanimated-flat-list (reagent/adapt-react-class (.-FlatList ^js rn)))
(defn flat-list
  [props]
  [reanimated-flat-list (rn-flat-list/base-list-props props)])
(def touchable-opacity (create-animated-component (.-TouchableOpacity ^js rn)))
(def linear-gradient (create-animated-component LinearGradient))
(def fast-image (create-animated-component FastImage))
(def blur-view (create-animated-component (.-BlurView blur)))

;;; Easings
(def in-out (.-inOut ^js Easing))
(def bezier (.-bezier ^js Easing))
(def easings
  {:default (in-out (.-quad ^js Easing))
   :linear  (bezier 0 0 1 1)
   :easing1 (bezier 0.25 0.1 0.25 1)
   :easing2 (bezier 0 0.3 0.6 0.9)
   :easing3 (bezier 0.3 0.3 0.3 0.9)})

;;; Animations
(def with-timing withTiming)
(def with-delay withDelay)
(def with-spring withSpring)
(def with-decay withDecay)
(def key-frame Keyframe)
(def with-repeat withRepeat)
(def cancel-animation cancelAnimation)
(defn with-repeat-timing
  [val reps reverse? duration easing]
  (with-repeat
   (with-timing val
                (clj->js {:duration duration
                          :easing   (get easings easing)}))
   reps
   reverse?))


;;; Hooks and Helpers
(def use-val useSharedValue)
(defn get-val
  [anim]
  (when anim
    (.-value anim)))
(defn set-val
  [anim val]
  (when (and anim (some? val))
    (set! (.-value anim) val)))

;;; Executing animation functions
(defn animate
  ([animation value]
   (animate animation value default-duration :default))
  ([animation value duration]
   (animate animation value duration :default))
  ([animation value duration easing]
   (set-val animation
            (with-timing value
                         (clj->js {:duration duration
                                   :easing   (get easings easing)})))))

(defn animate-spring
  [animation val {:keys [damping mass stiffness]}]
  (set-val animation
           (with-spring val
                        (clj->js {:damping   damping
                                  :mass      mass
                                  :stiffness stiffness}))))

(defn animate-decay
  ([animation velocity]
   (animate-decay animation velocity []))
  ([animation velocity clamp]
   (set-val animation
            (with-decay (clj->js {:velocity velocity
                                  :clamp    clamp})))))

(defn animate-delay
  ([animation val delay]
   (animate-delay animation val delay default-duration :default))
  ([animation val delay duration]
   (animate-delay animation val delay duration :default))
  ([animation val delay duration easing]
   (set-val animation
            (with-delay delay
                        (with-timing val
                                     (clj->js {:duration duration
                                               :easing   (get easings easing)}))))))

(defn animate-repeat
  ([animation val reps]
   (animate-repeat animation val reps false default-duration :default))
  ([animation val reps reverse?]
   (animate-repeat animation val reps reverse? default-duration :default))
  ([animation val reps reverse? duration]
   (animate-repeat animation val reps reverse? duration :default))
  ([animation val reps reverse? duration easing]
   (set-val animation (with-repeat-timing val reps reverse? duration easing))))

(defn animate-delay-repeat
  ([animation val duration easing delay reps]
   (animate-delay-repeat animation val duration easing delay reps false))
  ([animation val duration easing delay reps reverse?]
   (set-val animation (with-delay delay (with-repeat-timing val reps reverse? duration easing)))))

(defn interpolate
  ([val input-range output-range]
   (interpolate val input-range output-range nil))
  ([val input-range output-range extrapolation]
   (.interpolateValue ^js worklet-factory
                      val
                      (clj->js input-range)
                      (clj->js output-range)
                      (clj->js extrapolation))))


;;; Styling
(def use-animated-style useAnimatedStyle)
(defn apply-animations-to-style
  [animations style]
  (use-animated-style
   (.applyAnimationsToStyle ^js worklet-factory (clj->js animations) (clj->js style))))


;;; Built-in Animations
(def slide-in-up-animation SlideInUp)
(def slide-out-up-animation SlideOutUp)
(def linear-transition LinearTransition)
