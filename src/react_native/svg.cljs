(ns react-native.svg
  (:require
   ["react-native-svg" :as Svg]
   [reagent.core       :as reagent]))

(def svg (reagent/adapt-react-class Svg/default))
(def path (reagent/adapt-react-class Svg/Path))
