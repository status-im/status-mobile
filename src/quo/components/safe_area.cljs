(ns quo.components.safe-area
  (:require [status-im.react-native.js-dependencies :refer [safe-area-context]]
            [reagent.core :as reagent]
            [oops.core :refer [oget]]))

(def provider (reagent/adapt-react-class (oget safe-area-context "SafeAreaProvider")))
(def ^:private consumer-raw (reagent/adapt-react-class (oget safe-area-context "SafeAreaConsumer")))
(def view (reagent/adapt-react-class (oget safe-area-context "SafeAreaView")))

(defn consumer [component]
  [consumer-raw
   (fn [insets]
     (reagent/as-element
      [component (js->clj insets :keywordize-keys true)]))])
