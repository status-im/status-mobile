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
                             withSequence
                             withDecay
                             Easing
                             Keyframe
                             cancelAnimation
                             SlideInUp
                             SlideOutUp
                             FadeIn
                             runOnJS)]
            [reagent.core :as reagent]
            ["react-native-redash" :refer (withPause)]
            [react-native.flat-list :as rn-flat-list]
            [utils.worklets.core :as worklets.core]))

(def ^:const default-duration 300)

;; Animations
(def slide-in-up-animation SlideInUp)
(def slide-out-up-animation SlideOutUp)

;;;; LinearTransition is deprecated
;;;; https://github.com/software-mansion/react-native-reanimated/issues/4362#issuecomment-1508682400
;;;; temporarily using FadeIn as a replacement to Linear Transition
(def linear-transition FadeIn)

;; Animated Components
(def create-animated-component (comp reagent/adapt-react-class (.-createAnimatedComponent reanimated)))

(def view (reagent/adapt-react-class (.-View reanimated)))
(def text (reagent/adapt-react-class (.-Text reanimated)))
(def scroll-view (reagent/adapt-react-class (.-ScrollView reanimated)))
(def image (reagent/adapt-react-class (.-Image reanimated)))

;; TODO: This one should use FlatList from Reanimated.
;; Trying to use Flatlist from RA causes test to fail: "The first argument must be a component. Instead
;; received: object"
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
(def with-decay withDecay)
(def key-frame Keyframe)
(def with-repeat withRepeat)
(def with-sequence withSequence)
(def with-pause withPause)
(def cancel-animation cancelAnimation)

(def run-on-js runOnJS)

;; Easings
(def bezier (.-bezier ^js Easing))

(def in-out
  (.-inOut ^js Easing))

;; trying to put default-easing inside easings map causes test to fail
(defn default-easing [] (in-out (.-quad ^js Easing)))

(def easings
  {:linear  (bezier 0 0 1 1)
   :easing1 (bezier 0.25 0.1 0.25 1)
   :easing2 (bezier 0 0.3 0.6 0.9)
   :easing3 (bezier 0.3 0.3 0.3 0.9)
   :easing4 (bezier 0.3 0.3 1 1)})

;; Helper functions
(defn get-shared-value
  [anim]
  (when anim
    (.-value anim)))

(defn set-shared-value
  [anim v]
  (when (and anim (some? v))
    (set! (.-value anim) v)))

(defn interpolate
  ([shared-value input-range output-range]
   (interpolate shared-value input-range output-range nil))
  ([shared-value input-range output-range extrapolation]
   (worklets.core/interpolate-value
    shared-value
    (clj->js input-range)
    (clj->js output-range)
    (clj->js extrapolation))))

(defn apply-animations-to-style
  [animations style]
  (use-animated-style
   (worklets.core/apply-animations-to-style (clj->js animations) (clj->js style))))

;; Animators
(defn animate-shared-value-with-timing
  [anim v duration easing]
  (set-shared-value anim
                    (with-timing v
                                 (js-obj "duration" duration
                                         "easing"   (get easings easing)))))

(defn animate-shared-value-with-delay
  [anim v duration easing delay]
  (set-shared-value
   anim
   (with-delay delay
               (with-timing v
                            (js-obj "duration" duration
                                    "easing"   (get easings easing))))))

(defn animate-delay
  ([animation v delay]
   (animate-delay animation v delay default-duration))
  ([animation v delay duration]
   (set-shared-value
    animation
    (with-delay delay
                (with-timing v
                             (clj->js {:duration duration
                                       :easing   (default-easing)}))))))

(defn animate-shared-value-with-repeat
  [anim v duration easing number-of-repetitions reverse?]
  (set-shared-value anim
                    (with-repeat (with-timing v
                                              (js-obj "duration" duration
                                                      "easing"   (get easings easing)))
                                 number-of-repetitions
                                 reverse?)))

(defn animate-shared-value-with-delay-repeat
  ([anim v duration easing delay number-of-repetitions]
   (animate-shared-value-with-delay-repeat anim v duration easing delay number-of-repetitions false))
  ([anim v duration easing delay number-of-repetitions reverse?]
   (set-shared-value anim
                     (with-delay delay
                                 (with-repeat
                                  (with-timing v
                                               #js
                                                {:duration duration
                                                 :easing   (get easings easing)})
                                  number-of-repetitions
                                  reverse?)))))

(defn animate-shared-value-with-spring
  [anim v {:keys [mass stiffness damping]}]
  (set-shared-value anim
                    (with-spring v
                                 (js-obj "mass"      mass
                                         "damping"   damping
                                         "stiffness" stiffness))))

(defn animate-shared-value-with-decay
  [anim velocity clamp]
  (set-shared-value anim
                    (with-decay (clj->js {:velocity velocity
                                          :clamp    clamp}))))

(defn animate
  ([animation value]
   (animate animation value default-duration))
  ([animation value duration]
   (set-shared-value animation
                     (with-timing value
                                  (clj->js {:duration duration
                                            :easing   (default-easing)})))))


(defn with-timing-duration
  [v duration]
  (with-timing v
               (clj->js {:duration duration
                         :easing   (in-out (.-quad ^js Easing))})))
