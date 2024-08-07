(ns status-im.contexts.shell.qr-reader.sheets.scanned-wallet-address
  (:require
    [quo.core :as quo]
    [react-native.clipboard :as clipboard]
    [status-im.feature-flags :as ff]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- on-press
  [address]
  ;; Originally, the flow went to the send flow, but it has been removed to avoid bugs. Please check
  ;; https://github.com/status-im/status-mobile/issues/20972. The previous code has been commented
  ;; out.
  (clipboard/set-string address)
  (rf/dispatch [:toasts/upsert
                {:type :positive
                 :text (i18n/label :t/address-copied)}])
  #_(let [[_ split-address] (network-utils/split-network-full-address address)]
      (rf/dispatch
       [:wallet/select-send-address
        {:address     address
         :recipient   {:recipient-type :address
                       :label          (utils/get-shortened-address split-address)}
         :stack-id    :wallet-select-address
         :start-flow? true}])))

(defn view
  [address]
  [:<>
   [quo/drawer-top {:title address :type :address}]
   [quo/action-drawer
    [[{:icon                :i/copy
       :accessibility-label :send-asset
       :label               (i18n/label :t/copy-address)
       :on-press            #(on-press address)}
      (when (ff/enabled? :ff/wallet.saved-addresses)
        {:icon                :i/save
         :accessibility-label :save-address
         :label               (i18n/label :t/save-address)
         :on-press            #(js/alert "feature not implemented")})]]]])
