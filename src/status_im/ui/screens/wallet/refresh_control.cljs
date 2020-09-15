(ns status-im.ui.screens.wallet.refresh-control
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [quo.react-native :as rn]))

(defn refresh-action []
  (re-frame/dispatch-sync [:wallet.ui/pull-to-refresh])
  (reagent/flush))

(defn refresh-control []
  (reagent/as-element
   [rn/refresh-control {:refreshing @(re-frame/subscribe [:prices-loading?])
                        :onRefresh  refresh-action}]))
