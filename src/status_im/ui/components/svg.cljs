(ns status-im.ui.components.svg
  (:require [reagent.core :as reagent]
            ["react-native-svg" :refer (Svg SvgXml Rect Path Image G Defs ClipPath)]))

(def svgxml (reagent/adapt-react-class SvgXml))
(def svg (reagent/adapt-react-class Svg))
(def rect (reagent/adapt-react-class Rect))
(def image (reagent/adapt-react-class Image))
(def g (reagent/adapt-react-class G))
(def path (reagent/adapt-react-class Path))
(def clip-path (reagent/adapt-react-class ClipPath))
(def defs (reagent/adapt-react-class Defs))
