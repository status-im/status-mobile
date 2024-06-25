(ns status-im.contexts.wallet.wallet-connect.modals.common.data-block.view
  (:require [status-im.common.raw-data-block.view :as raw-data-block]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [display-data (rf/sub [:wallet-connect/current-request-display-data])]
    [raw-data-block/view display-data]))
