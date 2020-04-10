(ns quo.react-native
  (:require [oops.core :refer [oget]]
            [reagent.core :as reagent]
            [status-im.react-native.js-dependencies :refer [react-native]]))

(def platform (oget react-native "Platform"))

(def view (reagent/adapt-react-class (oget react-native "View")))

(def text (reagent/adapt-react-class (oget react-native "Text")))

(def scroll-view (reagent/adapt-react-class (oget react-native "ScrollView")))

(def touchable-opacity (reagent/adapt-react-class (oget react-native "TouchableOpacity")))
(def touchable-highlight (reagent/adapt-react-class (oget react-native "TouchableHighlight")))
