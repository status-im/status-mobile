(ns status-im.contexts.wallet.common.wizard.utils
  (:require [status-im.contexts.wallet.bridge.flow-config :as wallet-bridge-flow]
            [status-im.contexts.wallet.send.flow-config :as wallet-send-flow]))

(defn wizard-find-next-screen
  [db flow-id current-screen]
  (let [flow-config (case flow-id
                      :wallet-send-flow   wallet-send-flow/steps
                      :wallet-bridge-flow wallet-bridge-flow/steps
                      nil)]
    (first (filter (fn [{:keys [skip-step? screen-id]}]
                     (and (not= screen-id current-screen)
                          (not (and (fn? skip-step?) (skip-step? db)))))
                   flow-config))))
