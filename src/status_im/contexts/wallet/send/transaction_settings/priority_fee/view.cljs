(ns status-im.contexts.wallet.send.transaction-settings.priority-fee.view
  (:require
    [quo.context]
    [status-im.contexts.wallet.send.transaction-settings.view :as transaction-settings]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn priority-fee-hint-text
  [{:keys [current low high]} lower-limit-exceeded? upper-limit-exceeded?]
  (cond
    upper-limit-exceeded? (i18n/label :t/priority-fee-higher {:low low :high high})
    lower-limit-exceeded? (i18n/label :t/priority-fee-lower {:low low :high high})
    :else                 (i18n/label :t/fee-current-gwei {:current current})))

(defn view
  []
  (let [suggested-values (rf/sub [:wallet/tx-settings-priority-fee])
        hint-text        (partial priority-fee-hint-text suggested-values)]
    [transaction-settings/custom-setting-screen
     {:screen-title     (i18n/label :t/priority-fee)
      :token-sybmol     :gwei
      :hint-text-fn     hint-text
      :suggested-values suggested-values
      :info-title       (i18n/label :t/priority-fee)
      :info-content     (i18n/label :t/about-priority-fee)
      :on-save          (fn [new-val]
                          (rf/dispatch [:wallet/set-priority-fee new-val])
                          (rf/dispatch [:navigate-back])
                          (rf/dispatch [:show-bottom-sheet
                                        {:content transaction-settings/custom-settings-sheet}]))}]))
