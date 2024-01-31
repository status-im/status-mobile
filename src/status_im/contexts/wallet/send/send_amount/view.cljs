(ns status-im.contexts.wallet.send.send-amount.view
  (:require
    [quo.theme :as quo.theme]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.send.input-amount.view :as input-amount]
    [utils.re-frame :as rf]))

(defn- view-internal
  []
  (let [token           (rf/sub [:wallet/wallet-send-token])
        total-balance   (utils/total-token-units-in-all-chains token)
        limit-crypto    (utils/get-standard-crypto-format token total-balance)
        crypto-decimals (utils/get-crypto-decimals-count token)]
    [input-amount/view
     {:crypto-decimals crypto-decimals
      :limit-crypto    limit-crypto}]))

(def view (quo.theme/with-theme view-internal))
