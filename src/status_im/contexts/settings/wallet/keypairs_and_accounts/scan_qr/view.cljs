(ns status-im.contexts.settings.wallet.keypairs-and-accounts.scan-qr.view
  (:require
    [react-native.core :as rn]
    [status-im.common.scan-qr-code.view :as scan-qr-code]
    [status-im.contexts.communities.events]
    [status-im.contexts.syncing.utils :as sync-utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [keypairs-key-uids (rf/sub [:get-screen-params])
        on-success-scan   (rn/use-callback (fn [scanned-text]
                                             (rf/dispatch [:wallet/success-keypair-qr-scan scanned-text
                                                           keypairs-key-uids])
                                             [keypairs-key-uids]))]
    [scan-qr-code/view
     {:title           (i18n/label :t/scan-key-pairs-qr-code)
      :subtitle        (i18n/label :t/find-it-in-setting)
      :share-button?   false
      :validate-fn     sync-utils/valid-connection-string?
      :error-message   (i18n/label :t/invalid-qr)
      :on-success-scan on-success-scan}]))
