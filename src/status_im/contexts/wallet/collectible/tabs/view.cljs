(ns status-im.contexts.wallet.collectible.tabs.view
  (:require [quo.theme]
            [status-im.contexts.wallet.collectible.tabs.about.view :as about]
            [status-im.contexts.wallet.collectible.tabs.activity.view :as activity]
            [status-im.contexts.wallet.collectible.tabs.overview.view :as overview]))

(defn view
  [{:keys [selected-tab]}]
  (case selected-tab
    :overview [overview/view]
    :about    [about/view]
    :activity [activity/view]
    nil))
