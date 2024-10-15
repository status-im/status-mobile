(ns react-native.pdf-viewer
  (:require
    ["react-native-pdf" :default rn-pdf]
    [reagent.core :as reagent]))

(def view (reagent/adapt-react-class rn-pdf))
