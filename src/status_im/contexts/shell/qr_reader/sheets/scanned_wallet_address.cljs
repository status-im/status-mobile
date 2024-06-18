(ns status-im.contexts.shell.qr-reader.sheets.scanned-wallet-address
  (:require
    [quo.core :as quo]
    [status-im.feature-flags :as ff]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  [address]
  [:<>
   [quo/drawer-top
    {:title address
     :type  :address}]
   [quo/action-drawer
    [[{:icon                :i/send
       :accessibility-label :send-asset
       :label               (i18n/label :t/send-to-this-address)
       :on-press            (fn []
                              (rf/dispatch [:wallet/select-send-address
                                            {:address     address
                                             :recipient   address
                                             :stack-id    :wallet-select-address
                                             :start-flow? true}]))}
      (when (ff/enabled? :ff/wallet.saved-addresses)
        {:icon                :i/save
         :accessibility-label :save-address
         :label               (i18n/label :t/save-address)
         :on-press            #(js/alert "feature not implemented")})]]]])
