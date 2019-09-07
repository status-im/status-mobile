(ns quo.react-native
  (:require [reagent.core :as reagent]
            ["react-native" :as rn]))

(def app-registry (.-AppRegistry rn))

(def platform (.-Platform ^js rn))

(def view (reagent/adapt-react-class (.-View ^js rn)))

(def text (reagent/adapt-react-class (.-Text ^js rn)))

(def scroll-view (reagent/adapt-react-class (.-ScrollView ^js rn)))

(def touchable-opacity (reagent/adapt-react-class (.-TouchableOpacity ^js rn)))
(def touchable-highlight (reagent/adapt-react-class (.-TouchableHighlight ^js rn)))
