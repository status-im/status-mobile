(ns quo.gesture-handler
  (:require [oops.core :refer [oget]]
            ["react-native-reanimated" :default animated]
            [reagent.core :as reagent]
            ["react-native-gesture-handler"
             :refer (TapGestureHandler PanGestureHandler LongPressGestureHandler
                                       PureNativeButton TouchableWithoutFeedback
                                       createNativeWrapper State)]))

(def tap-gesture-handler
  (reagent/adapt-react-class TapGestureHandler))

(def pan-gesture-handler
  (reagent/adapt-react-class PanGestureHandler))

(def long-press-gesture-handler
  (reagent/adapt-react-class LongPressGestureHandler))

(def pure-native-button PureNativeButton)

(def touchable-without-feedback-class TouchableWithoutFeedback)

(def touchable-without-feedback
  (reagent/adapt-react-class touchable-without-feedback-class))

(def raw-button
  (reagent/adapt-react-class
   (createNativeWrapper (.createAnimatedComponent animated PureNativeButton)
                        #js {:shouldActivateOnStart   true
                             :shouldCancelWhenOutside true})))

(def states {:began        (oget State "BEGAN")
             :active       (oget State "ACTIVE")
             :cancelled    (oget State "CANCELLED")
             :end          (oget State "END")
             :failed       (oget State "FAILED")
             :undetermined (oget State "UNDETERMINED")})
