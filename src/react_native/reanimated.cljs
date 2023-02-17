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
                             Easing
                             Keyframe
                             cancelAnimation
                             SlideInUp
                             SlideOutUp
                             LinearTransition)]
            [reagent.core :as reagent]
            [react-native.flat-list :as rn-flat-list]
            [utils.collection]))

;; Animations
(def slide-in-up-animation SlideInUp)
(def slide-out-up-animation SlideOutUp)
(def linear-transition LinearTransition)

;; Animated Components
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

;; Hooks
(def use-shared-value useSharedValue)
(def use-animated-style useAnimatedStyle)

;; Animations
(def with-timing withTiming)
(def with-delay withDelay)
(def with-spring withSpring)
(def key-frame Keyframe)
(def with-repeat withRepeat)
(def cancel-animation cancelAnimation)

;; Easings
(def bezier (.-bezier ^js Easing))

(def easings
  {:linear  (bezier 0 0 1 1)
   :easing1 (bezier 0.25 0.1 0.25 1) ;; TODO(parvesh) - rename easing functions, (design team input)
   :easing2 (bezier 0 0.3 0.6 0.9)
   :easing3 (bezier 0.3 0.3 0.3 0.9)})

;; Helper functions
(defn get-shared-value
  [anim]
  (when anim
    (.-value anim)))

(defn set-shared-value
  [anim val]
  (when (and anim (some? val))
    (set! (.-value anim) val)))

;; Worklets
(def worklet-factory (js/require "../src/js/worklet_factory.js"))

(defn interpolate
  ([shared-value input-range output-range]
   (interpolate shared-value input-range output-range nil))
  ([shared-value input-range output-range extrapolation]
   (.interpolateValue ^js worklet-factory
                      shared-value
                      (clj->js input-range)
                      (clj->js output-range)
                      (clj->js extrapolation))))

;;;; Component Animations
(defn apply-animations-to-style
  [animations style]
  (use-animated-style
   (.applyAnimationsToStyle ^js worklet-factory (clj->js animations) (clj->js style))))

;; Animators
(defn animate-shared-value-with-timing
  [anim val duration easing]
  (set-shared-value anim
                    (with-timing val
                                 (js-obj "duration" duration
                                         "easing"   (get easings easing)))))

(defn animate-shared-value-with-delay
  [anim val duration easing delay]
  (set-shared-value anim
                    (with-delay delay
                                (with-timing val
                                             (js-obj "duration" duration
                                                     "easing"   (get easings easing))))))

(defn animate-shared-value-with-repeat
  [anim val duration easing number-of-repetitions reverse?]
  (set-shared-value anim
                    (with-repeat (with-timing val
                                              (js-obj "duration" duration
                                                      "easing"   (get easings easing)))
                                 number-of-repetitions
                                 reverse?)))

(defn animate-shared-value-with-delay-repeat
  ([anim val duration easing delay number-of-repetitions]
   (animate-shared-value-with-delay-repeat anim val duration easing delay number-of-repetitions false))
  ([anim val duration easing delay number-of-repetitions reverse?]
   (set-shared-value anim
                     (with-delay delay
                                 (with-repeat
                                  (with-timing val
                                               #js
                                                {:duration duration
                                                 :easing   (get easings easing)})
                                  number-of-repetitions
                                  reverse?)))))

(defn animate-shared-value-with-spring
  [anim val {:keys [mass stiffness damping]}]
  (set-shared-value anim
                    (with-spring val
                                 (js-obj "mass"      mass
                                         "damping"   damping
                                         "stiffness" stiffness))))
