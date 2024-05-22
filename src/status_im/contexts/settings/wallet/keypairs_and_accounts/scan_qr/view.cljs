(ns status-im.contexts.settings.wallet.keypairs-and-accounts.scan-qr.view
  (:require
    [react-native.core :as rn]
    [status-im.common.scan-qr-code.view :as scan-qr-code]
    [status-im.contexts.communities.events]
    [status-im.contexts.syncing.utils :as sync-utils]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn show-invalid-qr-toast
  []
  (debounce/debounce-and-dispatch
   [:toasts/upsert
    {:type  :negative
     :theme :dark
     :text  (i18n/label :t/invalid-qr)}]
   300))

(defn- on-valid-connection-received
  [connection-string keypairs-key-uids]
  (rf/dispatch
   [:standard-auth/authorize-with-password
    {:blur?             true
     :theme             :dark
     :auth-button-label (i18n/label :t/confirm)
     :on-auth-success   (fn [password]
                          (rf/dispatch [:hide-bottom-sheet])
                          (rf/dispatch
                           [:wallet/connection-string-for-key-pair-import
                            {:connection-string connection-string
                             :keypairs-key-uids keypairs-key-uids
                             :sha3-pwd          password}]))}]))

(defn view
  []
  (let [keypairs          (rf/sub [:get-screen-params])
        keypairs-key-uids (rn/use-memo #(map :key-uid keypairs) [keypairs])
        on-success-scan   (rn/use-callback (fn [scanned-text]
                                             (if (sync-utils/valid-connection-string? scanned-text)
                                               (on-valid-connection-received scanned-text
                                                                             keypairs-key-uids)
                                               (show-invalid-qr-toast)))
                                           [keypairs-key-uids])]
    [scan-qr-code/view
     {:title           (i18n/label :t/scan-key-pairs-qr-code)
      :subtitle        (i18n/label :t/find-it-in-setting)
      :share-button?   false
      :on-success-scan on-success-scan}]))
