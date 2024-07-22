(ns status-im.contexts.wallet.common.scan-account.view
  (:require
   [status-im.common.scan-qr-code.view :as scan-qr-code]
   [status-im.constants :as constants]
   [status-im.contexts.wallet.common.utils.address :as utils-address]
   [utils.debounce :as debounce]
   [utils.i18n :as i18n]
   [utils.re-frame :as rf]))

(comment
  (def ma "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0x1")
  (def sa "arb1:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2")
  (utils-address/supported-address? ma)
  (utils-address/supported-address? sa)
  (utils-address/supported-address->status-address sa)
  (utils-address/supported-address->status-address ma))

(defn view
  []
  (let [{:keys [on-result]} (rf/sub [:get-screen-params])]
    [scan-qr-code/view
     {:title           (i18n/label :t/scan-qr)
      :subtitle        (i18n/label :t/scan-an-address-qr-code)
      :error-message   (i18n/label :t/oops-this-qr-does-not-contain-an-address)
      :validate-fn     #(utils-address/supported-address? %)
      :on-success-scan (fn [result]
                         (let [address (utils-address/supported-address->status-address result)]
                           (when on-result (on-result address))
                           (debounce/debounce-and-dispatch
                            [:wallet/scan-address-success address]
                            300)))}]))
