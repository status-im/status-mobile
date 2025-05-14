(ns react-native.safe-area-context
  (:require ["react-native-safe-area-context"
             :refer [initialWindowMetrics SafeAreaProvider]]
            [reagent.core :as reagent]))

(def initial-window-metrics (js->clj initialWindowMetrics :keywordize-keys true))
(def safe-area-provider (reagent/adapt-react-class SafeAreaProvider))
