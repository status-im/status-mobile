(ns react-native.masked-view
  (:require ["@react-native-community/masked-view" :default MaskedView]
            [reagent.core :as reagent]))

(def masked-view (reagent/adapt-react-class MaskedView))