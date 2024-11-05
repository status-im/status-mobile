(ns status-im.contexts.wallet.bridge.input-amount.view
  (:require
    [react-native.core :as rn]
    [status-im.contexts.wallet.bridge.input-amount.style :as style]
    [status-im.contexts.wallet.send.input-amount.view :as input-amount]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  [rn/view {:style style/bridge-send-wrapper}
   [input-amount/view
    {:current-screen-id      :screen/wallet.bridge-input-amount
     :button-one-label       (i18n/label :t/review-bridge)
     :enabled-from-chain-ids (rf/sub
                              [:wallet/bridge-from-chain-ids])
     :from-enabled-networks  (rf/sub [:wallet/bridge-from-networks])
     :on-confirm             (fn [amount]
                               (rf/dispatch [:wallet/set-token-amount-to-bridge
                                             {:amount   amount
                                              :stack-id :screen/wallet.bridge-input-amount}]))
     :on-navigate-back       (fn []
                               (rf/dispatch-sync [:wallet/stop-and-clean-suggested-routes])
                               (rf/dispatch [:wallet/clean-send-amount]))}]])
