(ns status-im.contexts.wallet.send.select-collectible-amount.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.collectible.utils :as utils]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.send.select-collectible-amount.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [on-close              (rn/use-callback #(rf/dispatch [:navigate-back]))
        [value set-value]     (rn/use-state 1)
        inc-value             (rn/use-callback (fn [] (set-value (inc value)))
                                               [value])
        dec-value             (rn/use-callback (fn [] (set-value (dec value)))
                                               [value])
        add-digit             (rn/use-callback (fn [digit]
                                                 (set-value (+ (js/parseInt digit)
                                                               (* value 10))))
                                               [value])
        delete-digit          (rn/use-callback (fn []
                                                 (set-value (Math/floor (/ value 10))))
                                               [value])

        send-transaction-data (rf/sub [:wallet/wallet-send])
        collectible           (:collectible send-transaction-data)
        balance               (utils/collectible-balance collectible)
        preview-uri           (get-in collectible [:preview-url :uri])
        incorrect-value?      (or (< value 1) (> value balance))]
    [rn/view
     [account-switcher/view
      {:icon-name     :i/arrow-left
       :on-press      on-close
       :switcher-type :select-account}]
     [quo/expanded-collectible
      {:image-src       preview-uri
       :square?         true
       :supported-file? (utils/supported-file? (get-in collectible
                                                       [:collectible-data :animation-media-type]))
       :container-style style/collectible-container}]
     [quo/network-tags
      {:title           (i18n/label :t/max {:number balance})
       :status          (if incorrect-value? :error :default)
       :container-style style/network-tags-container}]

     [quo/amount-input
      {:max-value       (if (integer? balance) balance 0)
       :min-value       1
       :value           value
       :on-inc-press    inc-value
       :on-dec-press    dec-value
       :container-style style/amount-input-container
       :status          (if incorrect-value? :error :default)}]
     [quo/bottom-actions
      {:actions          :one-action
       :button-one-props {:on-press  #(rf/dispatch [:wallet/send-collectibles-amount
                                                    {:collectible collectible
                                                     :amount      value
                                                     :stack-id    :screen/wallet.select-asset}])
                          :disabled? incorrect-value?}
       :button-one-label (i18n/label :t/confirm)}]
     [quo/numbered-keyboard
      {:left-action :none
       :delete-key? true
       :on-press    add-digit
       :on-delete   delete-digit}]]))

