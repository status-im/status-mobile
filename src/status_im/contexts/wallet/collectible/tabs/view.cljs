(ns status-im.contexts.wallet.collectible.tabs.view
  (:require [quo.context]
            [status-im.contexts.wallet.collectible.tabs.about.view :as about]
            [status-im.contexts.wallet.collectible.tabs.activity.view :as activity]
            [status-im.contexts.wallet.collectible.tabs.overview.view :as overview]))

(defn view
  [{:keys [selected-tab collectible]}]
  (case selected-tab
    :overview [overview/view collectible]
    :about    [about/view collectible]
    :activity [activity/view]
    nil))
