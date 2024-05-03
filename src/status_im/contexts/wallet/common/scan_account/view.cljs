(ns status-im.contexts.wallet.common.scan-account.view
  (:require [status-im.common.scan-qr-code.view :as scan-qr-code]
            [status-im.constants :as constants]
            [utils.debounce :as debounce]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- contains-address?
  [s]
  (boolean (re-find constants/regx-address-contains s)))

(defn- extract-address
  [scanned-text]
  (first (re-seq constants/regx-multichain-address scanned-text)))

(defn view
  []
  (let [{:keys [on-result]} (rf/sub [:get-screen-params])]
    [scan-qr-code/view
     {:title           (i18n/label :t/scan-qr)
      :subtitle        (i18n/label :t/scan-an-account-qr-code)
      :error-message   (i18n/label :t/oops-this-qr-does-not-contain-an-address)
      :validate-fn     #(contains-address? %)
      :on-success-scan (fn [result]
                         (let [address (extract-address result)]
                           (when on-result (on-result address))
                           (debounce/debounce-and-dispatch
                            [:wallet/scan-address-success address]
                            300)))}]))
