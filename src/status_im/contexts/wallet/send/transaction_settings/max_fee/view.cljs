(ns status-im.contexts.wallet.send.transaction-settings.max-fee.view
  (:require
    [quo.theme]
    [status-im.contexts.wallet.send.transaction-settings.view :as transaction-settings]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn hint-and-status
  [max-base-fee-from-route priority-fee entered-value]
  (cond
    (> entered-value (* 1.1 max-base-fee-from-route)) {:hint-text (i18n/label :t/max-base-fee-higher
                                                                              {:current
                                                                               max-base-fee-from-route})
                                                       :status    :warning}
    (< entered-value priority-fee)                    {:hint-text (i18n/label :t/max-base-fee-lower
                                                                              {:priority-fee
                                                                               priority-fee})
                                                       :status    :error}
    (< entered-value (* 0.9 max-base-fee-from-route)) {:hint-text (i18n/label
                                                                   :t/max-base-fee-lower-recommended
                                                                   {:current max-base-fee-from-route})
                                                       :status    :warning}
    :else                                             {:hint-text (i18n/label
                                                                   :t/max-base-fee-current
                                                                   {:current max-base-fee-from-route})
                                                       :status    :default}))

(defn view
  []
  (let [max-base-fee-from-route (rf/sub [:wallet/tx-settings-max-base-fee-route])
        max-base-fee            (rf/sub [:wallet/tx-settings-max-base-fee])
        priority-fee            (rf/sub [:wallet/tx-settings-priority-fee])
        conditions              (partial hint-and-status max-base-fee-from-route priority-fee)]
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
