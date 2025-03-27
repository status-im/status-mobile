(ns status-im.contexts.wallet.send.transaction-settings.max-fee.view
  (:require
    [quo.context]
    [status-im.contexts.wallet.send.transaction-settings.view :as transaction-settings]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn max-base-fee-hint-text
  [current lower-limit-exceeded? upper-limit-exceeded?]
  (cond
    upper-limit-exceeded? (i18n/label :t/max-base-fee-higher {:current current})
    lower-limit-exceeded? (i18n/label :t/max-base-fee-lower {:current current})
    :else                 (i18n/label :t/fee-current-gwei {:current current})))

(defn view
  []
  (let [suggested-values (rf/sub [:wallet/tx-settings-max-base-fee])
        hint-text        (partial max-base-fee-hint-text (:current suggested-values))]
    [transaction-settings/custom-setting-screen
     {:screen-title     (i18n/label :t/max-base-fee)
      :token-sybmol     :gwei
      :hint-text-fn     hint-text
      :suggested-values suggested-values
      :info-title       (i18n/label :t/max-base-fee)
      :info-content     (i18n/label :t/about-max-base-fee)
      :on-save          (fn [new-val]
                          (rf/dispatch [:wallet/set-max-base-fee new-val])
                          (rf/dispatch [:navigate-back])
                          (rf/dispatch [:show-bottom-sheet
                                        {:content transaction-settings/custom-settings-sheet}]))}]))
