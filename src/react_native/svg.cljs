(ns react-native.svg
  (:require ["react-native-svg" :as Svg]
            [reagent.core :as reagent]))

(def svg (reagent/adapt-react-class Svg/default))
(def path (reagent/adapt-react-class Svg/Path))
(def rect (reagent/adapt-react-class Svg/Rect))
(def clippath (reagent/adapt-react-class Svg/ClipPath))
(def defs (reagent/adapt-react-class Svg/Defs))
