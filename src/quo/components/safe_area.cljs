(ns quo.components.safe-area
  (:require ["react-native-safe-area-context" :as safe-area-context
             :refer (SafeAreaView SafeAreaProvider SafeAreaConsumer)]
            [reagent.core :as reagent]))

(def provider (reagent/adapt-react-class SafeAreaProvider))
(def ^:private consumer-raw (reagent/adapt-react-class SafeAreaConsumer))
(def view (reagent/adapt-react-class SafeAreaView))

(defn consumer [component]
  [consumer-raw
   (fn [insets]
     (reagent/as-element
      [component (js->clj insets :keywordize-keys true)]))])
