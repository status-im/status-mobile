(ns status-im.contexts.wallet.account.bridge-send.view
  (:require
    [quo.theme]
    [react-native.core :as rn]
    [status-im.contexts.wallet.account.bridge-send.style :as style]
    [status-im.contexts.wallet.send.input-amount.view :as input-amount]
    [utils.re-frame :as rf]))

(defn- view-internal
  []
  (let [send-bridge-data (rf/sub [:wallet/wallet-send])
        send-type        (:type send-bridge-data)]
    [rn/view {:style style/bridge-send-wrapper}
     [input-amount/view
      {:transfer-type    send-type
       :on-navigate-back (fn []
                           (rf/dispatch [:navigate-back-within-stack :wallet-bridge-send]))}]]))

(def view (quo.theme/with-theme view-internal))
