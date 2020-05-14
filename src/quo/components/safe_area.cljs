(ns quo.components.safe-area
  (:require ["react-native-safe-area-context" :as safe-area-context
             :refer (SafeAreaView SafeAreaProvider SafeAreaInsetsContext useSafeAreaInsets)]
            [reagent.core :as reagent]))

(def provider (reagent/adapt-react-class SafeAreaProvider))
(def ^:private consumer-raw (reagent/adapt-react-class (.-Consumer ^js SafeAreaInsetsContext)))
(def view (reagent/adapt-react-class SafeAreaView))

(defn consumer [component]
  [consumer-raw
   (fn [insets]
     (reagent/as-element
      [component (js->clj insets :keywordize-keys true)]))])

(defn use-safe-area []
  (let [insets (useSafeAreaInsets)]
    {:top    (.-top ^js insets)
     :bottom (.-bottom ^js insets)
     :left   (.-left ^js insets)
     :right  (.-right ^js insets)}))
