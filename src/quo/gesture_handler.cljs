(ns quo.gesture-handler
  (:require [oops.core :refer [oget]]
            ["react-native-reanimated" :default animated]
            [reagent.core :as reagent]
            ["react-native-gesture-handler"
             :refer (TapGestureHandler PanGestureHandler LongPressGestureHandler
                                       PureNativeButton TouchableWithoutFeedback createNativeWrapper State)]))

(def tap-gesture-handler
  (reagent/adapt-react-class TapGestureHandler))

(def pan-gesture-handler
  (reagent/adapt-react-class PanGestureHandler))

(def long-press-gesture-handler
  (reagent/adapt-react-class LongPressGestureHandler))

(def pure-native-button PureNativeButton)

(def touchable-without-feedback-class TouchableWithoutFeedback)

(def createNativeWrapper createNativeWrapper)

(def touchable-without-feedback
  (reagent/adapt-react-class touchable-without-feedback-class))

(def animated-raw-button
  (reagent/adapt-react-class
   (createNativeWrapper
    (.createAnimatedComponent animated touchable-without-feedback-class))))

(def state State)

(def states {:began        (oget state "BEGAN")
             :active       (oget state "ACTIVE")
             :cancelled    (oget state "CANCELLED")
             :end          (oget state "END")
             :failed       (oget state "FAILED")
             :undetermined (oget state "UNDETERMINED")})
