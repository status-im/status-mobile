(ns status-im.contexts.wallet.common.transaction-settings.priority-fee.view
  (:require
    [quo.context]
    [status-im.contexts.wallet.common.transaction-settings.view :as transaction-settings]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn hint-and-status
  [spectrum max-base-fee entered-value]
  (let [upper-limit-exceeded? (> entered-value (:high spectrum))
        lower-limit-exceeded? (< entered-value (:low spectrum))]
    (cond
      (> entered-value max-base-fee) {:hint-text (i18n/label
                                                  :t/priority-fee-higher-max-base
                                                  {:max-base-fee max-base-fee})
                                      :status    :error}
      upper-limit-exceeded?          {:hint-text (i18n/label :t/priority-fee-higher
                                                             spectrum)
                                      :status    :warning}
      lower-limit-exceeded?          {:hint-text (i18n/label :t/priority-fee-lower spectrum)
                                      :status    :warning}
      :else                          {:hint-text (i18n/label :t/priority-fee-current spectrum)
                                      :status    :default})))

(defn view
  []
  (let [priority-fee (rf/sub [:wallet/tx-settings-custom-priority-fee])
        max-base-fee (rf/sub [:wallet/tx-settings-max-base-fee])
        spectrum     {:low  (rf/sub [:wallet/tx-settings-suggested-min-priority-fee])
                      :high (rf/sub [:wallet/tx-settings-suggested-max-priority-fee])}
        conditions   (partial hint-and-status spectrum max-base-fee)]
    [transaction-settings/custom-setting-screen
     {:screen-title  (i18n/label :t/priority-fee)
      :token-symbol  :gwei
      :conditions-fn conditions
      :current       priority-fee
      :info-title    (i18n/label :t/priority-fee)
      :info-content  (i18n/label :t/about-priority-fee)
      :on-save       (fn [new-val]
                       (rf/dispatch [:wallet/set-priority-fee new-val])
                       (rf/dispatch [:navigate-back])
                       (rf/dispatch [:show-bottom-sheet
                                     {:content transaction-settings/custom-settings-sheet}]))}]))
