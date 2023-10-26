(ns status-im2.contexts.wallet.scan-account.view
  (:require [status-im2.common.scan-qr-code.view :as scan-qr-code]
            [status-im2.constants :as constants]
            [utils.debounce :as debounce]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- contains-address?
  [s]
  (boolean (re-find constants/regx-address-contains s)))

(defn- extract-address
  [scanned-text]
  (first (re-seq constants/regx-address-contains scanned-text)))

(defn view
  []
  (let [{:keys [show-subtitle? bottom-padding?]} (rf/sub [:get-screen-params])]
    [scan-qr-code/view
     {:title           (i18n/label :t/scan-qr)
      :subtitle        (i18n/label :t/scan-an-account-qr-code)
      :error-message   (i18n/label :t/oops-this-qr-does-not-contain-an-address)
      :validate-fn     #(contains-address? %)
      :bottom-padding? bottom-padding?
      :show-subtitle?  show-subtitle?
      :on-success-scan #(debounce/debounce-and-dispatch [:wallet/scan-address-success
                                                         (extract-address %)]
                                                        300)}]))
