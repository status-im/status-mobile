(ns status-im.contexts.wallet.send.select-collectible-amount.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.common.controlled-input.utils :as controlled-input]
    [status-im.contexts.wallet.collectible.utils :as utils]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.send.select-collectible-amount.style :as style]
    [utils.i18n :as i18n]
    [utils.money :as money]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [on-close                      (rn/use-callback
                                       #(rf/dispatch [:wallet/collectible-amount-navigate-back]))
        send-transaction-data         (rf/sub [:wallet/wallet-send])
        collectible                   (:collectible send-transaction-data)
        sender-address                (rf/sub [:wallet/current-viewing-account-address])
        balance                       (rf/sub [:wallet/total-owned-collectible (:ownership collectible)
                                               sender-address])
        [input-state set-input-state] (rn/use-state (-> controlled-input/init-state
                                                        (controlled-input/set-value-numeric 1)
                                                        (controlled-input/set-lower-limit 1)))
        preview-uri                   (get-in collectible [:preview-url :uri])
        incorrect-value?              (controlled-input/input-error input-state)
        increase-value                (rn/use-callback #(set-input-state controlled-input/increase))
        decrease-value                (rn/use-callback #(set-input-state controlled-input/decrease))
        delete-character              (rn/use-callback
                                       (fn [] (set-input-state #(controlled-input/delete-last % "1"))))
        add-character                 (rn/use-callback
                                       (fn [c]
                                         (set-input-state #(controlled-input/add-character % c))))]
    (rn/use-effect
     (fn []
       (set-input-state #(controlled-input/set-upper-limit % balance)))
     [balance])
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
      {:max-value       (controlled-input/upper-limit input-state)
       :min-value       (controlled-input/lower-limit input-state)
       :value           (controlled-input/value-numeric input-state)
       :on-inc-press    increase-value
       :on-dec-press    decrease-value
       :container-style style/amount-input-container
       :status          (if incorrect-value? :error :default)}]
     [quo/bottom-actions
      {:actions          :one-action
       :button-one-props {:on-press  #(rf/dispatch
                                       [:wallet/set-collectible-amount-to-send
                                        {:stack-id :screen/wallet.select-collectible-amount
                                         :amount   (controlled-input/value-numeric input-state)}])
                          :disabled? incorrect-value?}
       :button-one-label (i18n/label :t/confirm)}]
     [quo/numbered-keyboard
      {:left-action :none
       :delete-key? true
       :on-press    add-character
       :on-delete   delete-character}]]))
