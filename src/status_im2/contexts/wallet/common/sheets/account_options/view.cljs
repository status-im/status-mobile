(ns status-im2.contexts.wallet.common.sheets.account-options.view
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            quo.theme
            [react-native.clipboard :as clipboard]
            [react-native.core :as rn]
            [status-im2.contexts.wallet.common.sheets.account-options.style :as style]
            [status-im2.contexts.wallet.common.temp :as temp]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- view-internal
  [{:keys [theme]}]
  (let [{:keys [name color emoji address]} (rf/sub [:wallet/current-viewing-account])]
    [:<>
     [quo/drawer-top
      {:title                name
       :type                 :account
       :networks             [{:network-name :ethereum :short-name "eth"}
                              {:network-name :optimism :short-name "opt"}
                              {:network-name :arbitrum :short-name "arb1"}]
       :description          address
       :account-avatar-emoji emoji
       :customization-color  color}]
     [quo/action-drawer
      [[{:icon                :i/edit
         :accessibility-label :edit
         :label               (i18n/label :t/edit-account)
         :on-press            #(rf/dispatch [:navigate-to :wallet-edit-account])}
        {:icon                :i/copy
         :accessibility-label :copy-address
         :label               (i18n/label :t/copy-address)
         :on-press            (fn []
                                (rf/dispatch [:toasts/upsert
                                              {:icon       :i/correct
                                               :icon-color (colors/resolve-color :success theme)
                                               :text       (i18n/label :t/address-copied)}])
                                (clipboard/set-string address))}
        {:icon                :i/share
         :accessibility-label :share-account
         :label               (i18n/label :t/share-account)}
        {:icon                :i/delete
         :accessibility-label :remove-account
         :label               (i18n/label :t/remove-account)
         :danger?             true}]]]
     [quo/divider-line {:container-style {:margin-top 8}}]
     [quo/section-label
      {:section         (i18n/label :t/select-another-account)
       :container-style style/drawer-section-label}]
     [rn/flat-list
      {:data      temp/other-accounts
       :render-fn (fn [account] [quo/account-item {:account-props account}])
       :style     {:margin-horizontal 8}}]]))

(def view (quo.theme/with-theme view-internal))
