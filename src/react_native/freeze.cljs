(ns react-native.freeze
  (:require ["react-freeze" :refer [Freeze]]
            [reagent.core :as reagent]))

(def view (reagent/adapt-react-class Freeze))
