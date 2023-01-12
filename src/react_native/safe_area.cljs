(ns react-native.safe-area
  (:require ["react-native-safe-area-context" :as safe-area-context :refer
             (SafeAreaProvider SafeAreaInsetsContext)]
            [reagent.core :as reagent]))

(def ^:private consumer-raw (reagent/adapt-react-class (.-Consumer ^js SafeAreaInsetsContext)))

(def provider (reagent/adapt-react-class SafeAreaProvider))

(defn consumer
  [component]
  [consumer-raw
   (fn [^js insets]
     (reagent/as-element
      [component (js->clj insets :keywordize-keys true)]))])
