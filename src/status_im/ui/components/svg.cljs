(ns status-im.ui.components.svg
  (:require [reagent.core :as reagent]
            ["react-native-svg" :refer (SvgXml)]))

(def svgxml (reagent/adapt-react-class SvgXml))
