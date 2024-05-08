(ns status-im.contexts.wallet.collectible.tabs.activity.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]))

(defn activity-item
  [item]
  [:<>
   [quo/divider-date (:timestamp item)]
   [quo/wallet-activity item]])

(defn view
  []
  [rn/flat-list
   {:data      nil
    :style     {:flex 1}
    :render-fn activity-item}])
