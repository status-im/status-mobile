(ns status-im.contexts.wallet.sheets.select-account.view
  (:require [quo.core :as quo]
            quo.theme
            [react-native.gesture :as gesture]
            [status-im.contexts.wallet.common.utils :as utils]
            [status-im.contexts.wallet.sheets.select-account.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(def ^:private rounding-decimals 4)

(defn list-item
  [{:keys [account selected-account-address on-press]}]
  (let [{:keys [color address]} account]
    [quo/account-item
     {:type                :default
      :account-props       (assoc account :customization-color color)
      :customization-color color
      :state               (if (= address selected-account-address) :selected :default)
      :on-press            (fn []
                             (if (fn? on-press)
                               (on-press address)
                               (rf/dispatch [:wallet/switch-current-viewing-account address]))
                             (rf/dispatch [:hide-bottom-sheet]))}]))

(defn list-item-with-balance
  [{:keys [account selected-account-address asset-symbol network on-press]}]
  (let [{:keys [color address tokens]} account
        token                          (->> tokens
                                            (filter #(= (:symbol %) asset-symbol))
                                            first)
        chain-id                       (:chain-id network)
        token-balance-display          (utils/token-balance-display-for-network token
                                                                                chain-id
                                                                                rounding-decimals)]
    [quo/account-item
     {:type                (if (= address selected-account-address) :default :tag)
      :token-props         {:symbol asset-symbol
                            :value  token-balance-display}
      :account-props       (assoc account :customization-color color)
      :customization-color color
      :state               (if (= address selected-account-address) :selected :default)
      :on-press            (fn []
                             (if (fn? on-press)
                               (on-press address)
                               (rf/dispatch [:wallet/switch-current-viewing-account address]))
                             (rf/dispatch [:hide-bottom-sheet]))}]))

(defn view
  [{:keys [show-account-balances? asset-symbol network address on-press-account]}]
  (let [selected-account-address (or address (rf/sub [:wallet/current-viewing-account-address]))
        accounts                 (rf/sub [:wallet/operable-accounts])]
    [:<>
     [quo/drawer-top {:title (i18n/label :t/select-account)}]
     [gesture/flat-list
      {:data                            accounts
       :render-fn                       (fn [account _ _
                                             {:keys [selected-account-address on-press-account]}]
                                          (if show-account-balances?
                                            [list-item-with-balance
                                             {:account                  account
                                              :selected-account-address selected-account-address
                                              :asset-symbol             asset-symbol
                                              :network                  network
                                              :on-press                 on-press-account}]
                                            [list-item
                                             {:account                  account
                                              :selected-account-address selected-account-address
                                              :on-press                 on-press-account}]))
       :render-data                     {:selected-account-address selected-account-address
                                         :on-press-account         on-press-account}
       :content-container-style         style/list-container
       :shows-vertical-scroll-indicator false}]]))
