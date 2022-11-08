(ns react-native.safe-area
  (:require ["react-native-safe-area-context" :as safe-area-context
             :refer (SafeAreaInsetsContext)]
            [reagent.core :as reagent]))

(def ^:private consumer-raw (reagent/adapt-react-class (.-Consumer ^js SafeAreaInsetsContext)))

(defn consumer [component]
  [consumer-raw
   (fn [insets]
     (reagent/as-element
      [component (js->clj insets :keywordize-keys true)]))])
