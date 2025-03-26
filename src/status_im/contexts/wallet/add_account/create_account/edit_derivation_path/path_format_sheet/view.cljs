(ns status-im.contexts.wallet.add-account.create-account.edit-derivation-path.path-format-sheet.view
  (:require
    [quo.core :as quo]
    [quo.theme]
    [status-im.constants :as constants]
    [utils.i18n :as i18n]))

(defn view
  []
  (let [{:keys [customization-color]} (quo.theme/use-screen-params)]
    [:<>
     [quo/drawer-top {:title (i18n/label :t/path-format)}]
     [quo/action-drawer
      [[{:accessibility-label :default-ethereum-format
         :label               (i18n/label :t/default-ethereum-format)
         :state               :selected
         :customization-color customization-color
         :sub-label           constants/path-wallet-root}
        {:accessibility-label :ropsten-testnet
         :label               (i18n/label :t/ropsten-testnet)
         :sub-label           constants/path-ropsten-testnet
         :customization-color customization-color}
        {:accessibility-label :ledger
         :label               (i18n/label :t/ledger)
         :sub-label           constants/path-ledger
         :customization-color customization-color}
        {:accessibility-label :ledger-live
         :label               (i18n/label :t/ledger-live)
         :sub-label           constants/path-ledger-live
         :customization-color customization-color}
        {:accessibility-label :keepkey
         :label               (i18n/label :t/keep-key)
         :sub-label           constants/path-keepkey
         :customization-color customization-color}
        {:icon                :i/customize
         :accessibility-label :custom
         :label               (i18n/label :t/custom)
         :sub-label           (i18n/label :t/type-your-path)
         :add-divider?        true}]]]]))
