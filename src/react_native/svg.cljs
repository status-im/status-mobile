(ns react-native.svg
  (:require ["react-native-svg" :refer [default Path Rect ClipPath Defs Circle SvgXml G]]
            [reagent.core :as reagent]
            [react-native.core :as rn]))

(def svg (reagent/adapt-react-class default))
(def path (reagent/adapt-react-class Path))
(def rect (reagent/adapt-react-class Rect))
(def clippath (reagent/adapt-react-class ClipPath))
(def defs (reagent/adapt-react-class Defs))
(def circle (reagent/adapt-react-class Circle))
(def svgxml rn/view) ; FIX
(def g (reagent/adapt-react-class G))
