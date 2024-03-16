(ns react-native.shadow
  (:require
    ["react-native-shadow-2" :refer [Shadow]]
    [reagent.core :as reagent]))

(def shadow (reagent/adapt-react-class Shadow))
