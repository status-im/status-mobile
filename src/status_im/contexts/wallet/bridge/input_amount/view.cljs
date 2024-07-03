(ns status-im.contexts.wallet.bridge.input-amount.view
  (:require
    [react-native.core :as rn]
    [status-im.contexts.wallet.bridge.input-amount.style :as style]
    [status-im.contexts.wallet.send.input-amount.view :as input-amount]
    [status-im.setup.hot-reload :as hot-reload]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  (hot-reload/use-safe-unmount #(rf/dispatch [:wallet/stop-get-suggested-routes]))
  [rn/view {:style style/bridge-send-wrapper}
   [input-amount/view
    {:current-screen-id      :screen/wallet.bridge-input-amount
     :button-one-label       (i18n/label :t/review-bridge)
     :button-one-props       {:icon-left :i/bridge}
     :enabled-from-chain-ids (rf/sub
                              [:wallet/bridge-from-chain-ids])
     :from-enabled-networks  (rf/sub [:wallet/bridge-from-networks])
     :on-confirm             (fn [amount]
                               (rf/dispatch [:wallet/set-token-amount-to-bridge
                                             {:amount   amount
                                              :stack-id :screen/wallet.bridge-input-amount}]))
     :on-navigate-back       (fn []
                               (rf/dispatch [:wallet/clean-disabled-from-networks])
                               (rf/dispatch [:wallet/clean-send-amount]))}]])
