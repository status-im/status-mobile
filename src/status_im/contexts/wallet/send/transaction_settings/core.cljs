(ns status-im.contexts.wallet.send.transaction-settings.core
  (:require
    [status-im.constants :as constants]))

(def default-transaction-setting :transaction-setting/fast)

(defn transaction-setting->gas-rate
  [transaction-setting]
  (case transaction-setting
    :transaction-setting/normal constants/gas-rate-low
    :transaction-setting/fast   constants/gas-rate-medium
    :transaction-setting/urgent constants/gas-rate-high
    constants/gas-rate-medium))
