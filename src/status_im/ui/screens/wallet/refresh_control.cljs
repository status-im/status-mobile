(ns status-im.ui.screens.wallet.refresh-control
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [quo.react-native :as rn]))

(defn refresh-action []
  (fn []
    (when (false? @(re-frame/subscribe [:prices-loading?]))
      (re-frame/dispatch [:wallet.ui/pull-to-refresh]))))

(defn refresh-control
  []
  (fn []
    (reagent/as-element
     [rn/refresh-control {:refreshing @(re-frame/subscribe [:prices-loading?])}])))
