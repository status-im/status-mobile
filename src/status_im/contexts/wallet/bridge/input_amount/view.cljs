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
    {:current-screen-id :screen/wallet.bridge-input-amount
     :button-one-label  (i18n/label :t/confirm-bridge)
     :button-one-props  {:icon-left :i/bridge}
     :on-navigate-back  (fn []
                          (rf/dispatch [:wallet/clean-disabled-from-networks])
                          (rf/dispatch [:wallet/clean-send-amount])
                          (rf/dispatch [:navigate-back]))}]])
