(ns status-im.contexts.wallet.common.scan-account.view
  (:require
    [quo.context]
    [status-im.common.scan-qr-code.view :as scan-qr-code]
    [utils.address :as utils-address]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]))

(defn view
  []
  (let [{:keys [on-result]} (quo.context/use-screen-params)]
    [scan-qr-code/view
     {:title           (i18n/label :t/scan-qr)
      :subtitle        (i18n/label :t/scan-an-address-qr-code)
      :error-message   (i18n/label :t/oops-this-qr-does-not-contain-an-address)
      :validate-fn     #(utils-address/supported-scan-address? %)
      :on-success-scan (fn [result]
                         (let [address (utils-address/supported-address->eth-address result)]
                           (when on-result (on-result address))
                           (debounce/debounce-and-dispatch
                            [:wallet/scan-address-success address]
                            300)))}]))
