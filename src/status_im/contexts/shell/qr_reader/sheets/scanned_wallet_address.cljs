(ns status-im.contexts.shell.qr-reader.sheets.scanned-wallet-address
  (:require
    [quo.core :as quo]
    [react-native.clipboard :as clipboard]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.feature-flags :as ff]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- copy-address
  [address]
  (clipboard/set-string address)
  (rf/dispatch [:toasts/upsert
                {:type :positive
                 :text (i18n/label :t/address-copied)}]))

(defn- send-to-address
  [address]
  (rf/dispatch
   [:wallet/init-send-flow-for-address
    {:address   address
     :recipient {:recipient-type :address
                 :label          (utils/get-shortened-address address)}
     :stack-id  :wallet-select-address}]))

(defn view
  [address]
  [:<>
   [quo/drawer-top {:title address :type :address}]
   [quo/action-drawer
    [[{:icon                :i/copy
       :accessibility-label :send-asset
       :label               (i18n/label :t/copy-address)
       :on-press            #(copy-address address)}
      {:icon                :i/send
       :accessibility-label :send-asset
       :label               (i18n/label :t/send-to-this-address)
       :on-press            #(send-to-address address)}
      (when (ff/enabled? :ff/wallet.saved-addresses)
        {:icon                :i/save
         :accessibility-label :save-address
         :label               (i18n/label :t/save-address)
         :on-press            #(js/alert "feature not implemented")})]]]])
