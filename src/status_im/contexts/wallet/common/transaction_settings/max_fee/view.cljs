(ns status-im.contexts.wallet.common.transaction-settings.max-fee.view
  (:require
    [quo.context]
    [status-im.contexts.wallet.common.transaction-settings.view :as transaction-settings]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn hint-and-status
  [network-base-fee priority-fee entered-value]
  (cond
    (> entered-value (* 1.1 network-base-fee)) {:hint-text (i18n/label :t/max-base-fee-higher
                                                                       {:current
                                                                        network-base-fee})
                                                :status    :warning}
    (< entered-value priority-fee)             {:hint-text (i18n/label :t/max-base-fee-lower
                                                                       {:priority-fee
                                                                        priority-fee})
                                                :status    :error}
    (< entered-value (* 0.9 network-base-fee)) {:hint-text (i18n/label
                                                            :t/max-base-fee-lower-recommended
                                                            {:current network-base-fee})
                                                :status    :warning}
    :else                                      {:hint-text (i18n/label
                                                            :t/max-base-fee-current
                                                            {:current network-base-fee})
                                                :status    :default}))

(defn view
  []
  (let [network-base-fee (rf/sub [:wallet/tx-settings-network-base-fee-route])
        max-base-fee     (rf/sub [:wallet/tx-settings-max-base-fee])
        priority-fee     (rf/sub [:wallet/tx-settings-priority-fee])
        conditions       (partial hint-and-status network-base-fee priority-fee)]
    [transaction-settings/custom-setting-screen
     {:screen-title  (i18n/label :t/max-base-fee)
      :token-sybmol  :gwei
      :conditions-fn conditions
      :current       max-base-fee
      :info-title    (i18n/label :t/max-base-fee)
      :info-content  (i18n/label :t/about-max-base-fee)
      :on-save       (fn [new-val]
                       (rf/dispatch [:wallet/set-max-base-fee new-val])
                       (rf/dispatch [:navigate-back])
                       (rf/dispatch [:show-bottom-sheet
                                     {:content transaction-settings/custom-settings-sheet}]))}]))
