(ns quo.gesture-handler
  (:require [oops.core :refer [oget]]
            [quo.animated :as animated]
            [reagent.core :as reagent]
            [status-im.react-native.js-dependencies :as js-deps]))

(def tap-gesture-handler
  (reagent/adapt-react-class
   (oget js-deps/react-native-gesture-handler "TapGestureHandler")))

(def pan-gesture-handler
  (reagent/adapt-react-class
   (oget js-deps/react-native-gesture-handler "PanGestureHandler")))

(def long-press-gesture-handler
  (reagent/adapt-react-class
   (oget js-deps/react-native-gesture-handler "LongPressGestureHandler")))

(def pure-native-button (oget js-deps/react-native-gesture-handler "PureNativeButton"))

(def touchable-without-feedback-class
  (oget js-deps/react-native-gesture-handler "TouchableWithoutFeedback"))

(def createNativeWrapper
  (oget js-deps/react-native-gesture-handler "createNativeWrapper"))

(def touchable-without-feedback
  (reagent/adapt-react-class touchable-without-feedback-class))

(def animated-raw-button
  (reagent/adapt-react-class
   (createNativeWrapper
    (animated/createAnimatedComponent touchable-without-feedback-class))))

(def state (oget js-deps/react-native-gesture-handler "State"))

(def states {:began        (oget state "BEGAN")
             :active       (oget state "ACTIVE")
             :cancelled    (oget state "CANCELLED")
             :end          (oget state "END")
             :failed       (oget state "FAILED")
             :undetermined (oget state "UNDETERMINED")})
