(ns status-im.contexts.wallet.common.transaction-settings.gas-amount.view
  (:require
    [quo.context]
    [status-im.contexts.wallet.common.transaction-settings.view :as transaction-settings]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn hint-and-status
  [suggested-gas-amount {:keys [low high]} entered-value]
  (cond
    (> entered-value high)                         {:hint-text (i18n/label :t/gas-amount-higher
                                                                           {:high high})
                                                    :status    :error}
    (> entered-value (* 1.1 suggested-gas-amount)) {:hint-text (i18n/label :t/gas-amount-higher-than
                                                                           {:current
                                                                            suggested-gas-amount})
                                                    :status    :warning}

    (< entered-value low)                          {:hint-text (i18n/label :t/gas-amount-lower
                                                                           {:low low})
                                                    :status    :error}
    (< entered-value (* 0.9 suggested-gas-amount)) {:hint-text (i18n/label :t/gas-amount-lower-than
                                                                           {:current
                                                                            suggested-gas-amount})
                                                    :status    :warning}
    :else                                          {:hint-text (i18n/label :t/current-units
                                                                           {:current
                                                                            suggested-gas-amount})
                                                    :status    :default}))

(defn view
  []
  (let [spectrum             {:low  21000
                              :high 7920027}
        gas-amount           (rf/sub [:wallet/tx-settings-gas-amount])
        suggested-gas-amount (rf/sub [:wallet/tx-settings-suggested-tx-gas-amount])
        conditions           (partial hint-and-status suggested-gas-amount spectrum)]
    [transaction-settings/custom-setting-screen
     {:screen-title   (i18n/label :t/max-gas-amount)
      :token-symbol   :units
      :conditions-fn  conditions
      :current        gas-amount
      :info-title     (i18n/label :t/gas-amount)
      :info-content   (i18n/label :t/about-gas-amount)
      :on-save        (fn [new-val]
                        (rf/dispatch [:wallet/set-max-gas-amount new-val])
                        (rf/dispatch [:navigate-back])
                        (rf/dispatch [:show-bottom-sheet
                                      {:content transaction-settings/custom-settings-sheet}]))
      :with-decimals? false}]))
