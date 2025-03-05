(ns status-im.contexts.wallet.send.transaction-settings.gas-amount.view
  (:require
    [quo.theme]
    [status-im.contexts.wallet.send.transaction-settings.view :as transaction-settings]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn gas-amount-hint-text
  [{:keys [current]} lower-limit-exceeded? _upper-limit-exceeded?]
  (cond
    lower-limit-exceeded? (i18n/label :t/gas-amount-lower {:current current})
    :else                 (i18n/label :t/current-units {:current current})))

(defn view
  []
  (let [suggested-values (rf/sub [:wallet/tx-settings-max-gas-amount])
        hint-text        (partial gas-amount-hint-text suggested-values)]
    [transaction-settings/custom-setting-screen
     {:screen-title     (i18n/label :t/max-gas-amount)
      :token-symbol     :units
      :hint-text-fn     hint-text
      :suggested-values suggested-values
      :info-title       (i18n/label :t/gas-amount)
      :info-content     (i18n/label :t/about-gas-amount)
      :on-save          (fn [new-val]
                          (rf/dispatch [:wallet/set-max-gas-amount new-val])
                          (rf/dispatch [:navigate-back])
                          (rf/dispatch [:show-bottom-sheet
                                        {:content transaction-settings/custom-settings-sheet}]))
      :with-decimals?   false}]))
