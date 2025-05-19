(ns status-im.contexts.wallet.common.transaction-settings.gas-price.view
  (:require [status-im.contexts.wallet.common.transaction-settings.view :as transaction-settings]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn hint-and-status
  [suggested-gas-price current-gas-price {:keys [low high]} entered-value]
  (cond
    (> entered-value high)                {:hint-text (i18n/label :t/gas-price-higher
                                                                  {:high high})
                                           :status    :error}
    (> entered-value suggested-gas-price) {:hint-text (i18n/label :t/gas-price-higher-than
                                                                  {:current
                                                                   suggested-gas-price})
                                           :status    :warning}
    (< entered-value low)                 {:hint-text (i18n/label :t/gas-price-lower
                                                                  {:low low})
                                           :status    :error}
    (< entered-value suggested-gas-price) {:hint-text (i18n/label :t/gas-price-lower-than
                                                                  {:current
                                                                   suggested-gas-price})
                                           :status    :warning}
    :else                                 {:hint-text (i18n/label :t/gas-price-current
                                                                  {:current
                                                                   current-gas-price})
                                           :status    :default}))

(defn view
  []
  (let [gas-price           (rf/sub [:wallet/tx-settings-gas-price])
        current-gas-price   (rf/sub [:wallet/tx-settings-gas-price-route])
        suggested-gas-price (rf/sub [:wallet/tx-settings-suggested-gas-price])
        ;; We allow the user to go low (-10%) or high (+20%) of the suggested gas price. This logic
        ;; is aligned with the Desktop.
        spectrum            {:low  (* 0.9 suggested-gas-price)
                             :high (* 1.2 suggested-gas-price)}
        conditions          (partial hint-and-status suggested-gas-price current-gas-price spectrum)]
    [transaction-settings/custom-setting-screen
     {:screen-title  (i18n/label :t/gas-price)
      :token-sybmol  :gwei
      :conditions-fn conditions
      :current       gas-price
      :info-title    (i18n/label :t/gas-price)
      :info-content  (i18n/label :t/about-gas-price)
      :on-save       (fn [new-val]
                       (rf/dispatch [:wallet/set-gas-price new-val])
                       (rf/dispatch [:navigate-back])
                       (rf/dispatch [:show-bottom-sheet
                                     {:content transaction-settings/custom-settings-sheet}]))}]))
