(ns quo.gesture-handler
  (:require ["react-native-gesture-handler" :refer
             (TapGestureHandler PanGestureHandler
                                LongPressGestureHandler
                                TouchableWithoutFeedback
                                TouchableOpacity
                                TouchableHighlight
                                State
                                NativeViewGestureHandler
                                FlatList
                                ScrollView)]
            [oops.core :refer [oget]]
            [quo.design-system.colors :as colors]
            [reagent.core :as reagent]))

(def flat-list-raw FlatList)

(def flat-list (reagent/adapt-react-class FlatList))

(def scroll-view (reagent/adapt-react-class ScrollView))

(def tap-gesture-handler
  (reagent/adapt-react-class TapGestureHandler))

(def pan-gesture-handler
  (reagent/adapt-react-class PanGestureHandler))

(def long-press-gesture-handler
  (reagent/adapt-react-class LongPressGestureHandler))

(def touchable-without-feedback-class TouchableWithoutFeedback)

(def touchable-without-feedback
  (reagent/adapt-react-class touchable-without-feedback-class))

(def touchable-highlight-class (reagent/adapt-react-class TouchableHighlight))

(defn touchable-highlight
  [props & children]
  (into [touchable-highlight-class
         (merge {:underlay-color (:interactive-02 @colors/theme)}
                props)]
        children))

(def touchable-opacity
  (reagent/adapt-react-class TouchableOpacity))

(def native-view-gesture-handler (reagent/adapt-react-class NativeViewGestureHandler))

(def states
  {:began        (oget State "BEGAN")
   :active       (oget State "ACTIVE")
   :cancelled    (oget State "CANCELLED")
   :end          (oget State "END")
   :failed       (oget State "FAILED")
   :undetermined (oget State "UNDETERMINED")})
