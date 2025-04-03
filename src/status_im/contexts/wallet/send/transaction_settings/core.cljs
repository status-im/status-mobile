(ns status-im.contexts.wallet.send.transaction-settings.core
  (:require
    [status-im.constants :as constants]))

(defn tx-fee-mode->gas-rate
  [tx-fee-mode]
  (case tx-fee-mode
    :tx-fee-mode/normal constants/gas-rate-low
    :tx-fee-mode/fast   constants/gas-rate-medium
    :tx-fee-mode/urgent constants/gas-rate-high
    :tx-fee-mode/custom constants/gas-rate-custom
    constants/gas-rate-medium))

(defn gas-rate->tx-fee-mode
  [gas-rate]
  (condp = gas-rate
    constants/gas-rate-low    :tx-fee-mode/normal
    constants/gas-rate-medium :tx-fee-mode/fast
    constants/gas-rate-high   :tx-fee-mode/urgent
    constants/gas-rate-custom :tx-fee-mode/custom
    :tx-fee-mode/fast))
