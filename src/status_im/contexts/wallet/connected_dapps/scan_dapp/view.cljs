(ns status-im.contexts.wallet.connected-dapps.scan-dapp.view
  (:require [quo.core :as quo]
            [quo.foundations.resources :as quo.resources]
            [quo.theme]
            [react-native.core :as rn]
            [react-native.hooks :as hooks]
            [status-im.common.scan-qr-code.view :as scan-qr-code]
            [status-im.contexts.wallet.connected-dapps.scan-dapp.style :as style]
            [utils.debounce :as debounce]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [{:keys [keyboard-shown]}   (hooks/use-keyboard)
        {:keys [name emoji color]} (rf/sub [:wallet/current-viewing-account])]
    (rn/use-mount
     (fn []
       (when keyboard-shown
         (rn/dismiss-keyboard!))))
    [scan-qr-code/view
     {:title           (i18n/label :t/scan-qr)
      :subtitle        [rn/view style/subtitle-container
                        [quo/context-tag
                         {:theme               :dark
                          :type                :account
                          :size                24
                          :account-name        name
                          :emoji               emoji
                          :customization-color color}]
                        [quo/text
                         {:style style/subtitle-text
                          :size  :paragraph-1}
                         (i18n/label :t/wallet-connect-via)]
                        [quo/context-tag
                         {:theme     :dark
                          :type      :dapp
                          :size      24
                          :dapp-name (i18n/label :t/wallet-connect-label)
                          :dapp-logo (quo.resources/get-dapp :wallet-connect)}]]
      :on-success-scan (fn [scanned-text]
                         (debounce/debounce-and-dispatch
                          [:wallet-connect/on-scan-connection scanned-text]
                          300))}]))
