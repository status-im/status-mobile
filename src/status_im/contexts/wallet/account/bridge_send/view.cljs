(ns status-im.contexts.wallet.account.bridge-send.view
  (:require
    [quo.theme]
    [react-native.core :as rn]
    [status-im.contexts.wallet.account.bridge-send.style :as style]
    [status-im.contexts.wallet.send.input-amount.view :as input-amount]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- view-internal
  []
  [rn/view {:style style/bridge-send-wrapper}
   [input-amount/view
    {:current-screen-id :screen/wallet.bridge-send
     :button-one-label  (i18n/label :t/confirm-bridge)
     :button-one-props  {:icon-left :i/bridge}
     :on-navigate-back  (fn []
                          (rf/dispatch [:navigate-back-within-stack :screen/wallet.bridge-send]))}]])

(def view (quo.theme/with-theme view-internal))
