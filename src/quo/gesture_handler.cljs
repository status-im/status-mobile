(ns quo.gesture-handler
  (:require [oops.core :refer [oget]]
            ["react-native-reanimated" :default animated]
            [reagent.core :as reagent]
            [quo.design-system.colors :as colors]
            ["react-native-gesture-handler"
             :refer (TapGestureHandler PanGestureHandler LongPressGestureHandler
                                       PureNativeButton TouchableWithoutFeedback
                                       TouchableHighlight
                                       createNativeWrapper State NativeViewGestureHandler
                                       FlatList ScrollView)]))

(def flat-list-raw FlatList)

(def flat-list (reagent/adapt-react-class FlatList))

(def scroll-view (reagent/adapt-react-class ScrollView))

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

(def touchable-hightlight-class (reagent/adapt-react-class TouchableHighlight))

(defn touchable-hightlight [props & children]
  (into [touchable-hightlight-class (merge {:underlay-color (:interactive-02 @colors/theme)}
                                           props)]
        children))

(def raw-button
  (reagent/adapt-react-class
   (createNativeWrapper (.createAnimatedComponent animated PureNativeButton)
                        #js {:shouldActivateOnStart   true
                             :shouldCancelWhenOutside true})))

(def native-view-gesture-handler (reagent/adapt-react-class NativeViewGestureHandler))

(def states {:began        (oget State "BEGAN")
             :active       (oget State "ACTIVE")
             :cancelled    (oget State "CANCELLED")
             :end          (oget State "END")
             :failed       (oget State "FAILED")
             :undetermined (oget State "UNDETERMINED")})
