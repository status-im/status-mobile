(ns quo2.reanimated
  (:require ["react-native" :as rn]
            [reagent.core :as reagent]
            [clojure.string :as string]
            ["react-native-reanimated" :default reanimated
             :refer (useSharedValue useAnimatedStyle withTiming withDelay withSpring Easing Keyframe)]))

;; Animated Components
(def create-animated-component (comp reagent/adapt-react-class (.-createAnimatedComponent reanimated)))

(def view (reagent/adapt-react-class (.-View reanimated)))
(def image (reagent/adapt-react-class (.-Image reanimated)))
(def touchable-opacity (create-animated-component (.-TouchableOpacity ^js rn)))

;; Hooks 
(def use-shared-value useSharedValue)
(def use-animated-style useAnimatedStyle)

;; Animations
(def with-timing withTiming)
(def with-delay withDelay)
(def with-spring withSpring)
(def key-frame Keyframe)

;; Easings
(def bezier (.-bezier ^js Easing))

(def easings {:linear  (bezier 0 0 1 1)
              :easing1 (bezier 0.25 0.1 0.25 1) ;; TODO(parvesh) - rename easing functions, (design team input)
              :easing2 (bezier 0 0.3 0.6 0.9)
              :easing3 (bezier 0.3 0.3 0.3 0.9)})

;; Helper functions
(defn get-shared-value [anim]
  (.-value anim))

(defn set-shared-value [anim val]
  (set! (.-value anim) val))

(defn kebab-case->camelCase [k]
  (let [words (string/split (name k) #"-")]
    (->> (map string/capitalize (rest words))
         (apply str (first words))
         keyword)))

(defn map-keys [f m]
  (->> (map (fn [[k v]] [(f k) v]) m)
       (into {})))

;; Worklets
(def worklet-factory (js/require "../src/js/worklet_factory.js"))

;;;; Component Animations

;; kebab-case styles are not working for worklets
;; so first convert kebab case styles into camel case styles
(defn apply-animations-to-style [animations style]
  (let [animations (map-keys kebab-case->camelCase animations)
        style      (apply dissoc (map-keys kebab-case->camelCase style) (keys animations))]
    (use-animated-style
     (.applyAnimationsToStyle ^js worklet-factory (clj->js animations) (clj->js style)))))

;; Animators
(defn animate-shared-value-with-timing [anim val duration easing]
  (set-shared-value anim (with-timing val (js-obj "duration" duration
                                                  "easing"   (get easings easing)))))

(defn animate-shared-value-with-delay [anim val duration easing delay]
  (set-shared-value anim (with-delay delay (with-timing val (js-obj "duration" duration
                                                                    "easing"   (get easings easing))))))
