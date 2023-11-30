(ns status-im2.contexts.wallet.collectible.tabs.view
    (:require
    [quo.theme]
    [status-im2.contexts.wallet.collectible.tabs.overview.view :as overview]))
  
  (defn- view-internal
    [{:keys [selected-tab]}]
    (case selected-tab
      :overview [overview/view]
      nil))
  
  (def view (quo.theme/with-theme view-internal))