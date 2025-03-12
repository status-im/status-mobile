(ns status-im.contexts.wallet.send.transaction-settings.gas-amount.view
  (:require
    [quo.theme]
    [status-im.contexts.wallet.send.transaction-settings.view :as transaction-settings]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn hint-and-status
  [gas-amount-from-route {:keys [low high]} entered-value]
  (cond
    (> entered-value high)                          {:hint-text (i18n/label :t/gas-amount-higher
                                                                            {:high high})
                                                     :status    :error}
    (> entered-value (* 1.1 gas-amount-from-route)) {:hint-text (i18n/label :t/gas-amount-higher-than
                                                                            {:current
                                                                             gas-amount-from-route})
                                                     :status    :warning}

    (< entered-value low)                           {:hint-text (i18n/label :t/gas-amount-lower
                                                                            {:low low})
                                                     :status    :error}
    (< entered-value (* 0.9 gas-amount-from-route)) {:hint-text (i18n/label :t/gas-amount-lower-than
                                                                            {:current
                                                                             gas-amount-from-route})
                                                     :status    :warning}
    :else                                           {:hint-text (i18n/label :t/current-units
                                                                            {:current
                                                                             gas-amount-from-route})
                                                     :status    :default}))

(defn view
  []
  (let [spectrum              {:low  21000
                               :high 7920027}
        gas-amount            (rf/sub [:wallet/tx-settings-gas-amount])
        gas-amount-from-route (rf/sub [:wallet/tx-settings-gas-amount-route])
        conditions            (partial hint-and-status gas-amount-from-route spectrum)]
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
