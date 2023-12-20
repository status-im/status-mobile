(ns status-im.contexts.wallet.collectible.tabs.view
  (:require [quo.theme] 
            [status-im.contexts.wallet.collectible.tabs.about.view :as about]
            [status-im.contexts.wallet.collectible.tabs.overview.view :as overview]))
  
  (defn- view-internal
    [{:keys [selected-tab]}]
    (case selected-tab
      :overview [overview/view]
      :about    [about/view]
      [overview/view]))
  
  (def view (quo.theme/with-theme view-internal))