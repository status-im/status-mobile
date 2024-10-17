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
  [{:keys [account selected-account-address]}]
  (let [{:keys [color address]} account]
    [quo/account-item
     {:type                :default
      :account-props       (assoc account :customization-color color)
      :customization-color color
      :state               (if (= address selected-account-address) :selected :default)
      :on-press            (fn []
                             (rf/dispatch [:wallet/switch-current-viewing-account address])
                             (rf/dispatch [:hide-bottom-sheet]))}]))

(defn list-item-with-balance
  [{:keys [account selected-account-address asset-symbol network]}]
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
                             (rf/dispatch [:wallet/switch-current-viewing-account address])
                             (rf/dispatch [:hide-bottom-sheet]))}]))

(defn view
  [{:keys [show-account-balances? asset-symbol network]}]
  (let [selected-account-address (rf/sub [:wallet/current-viewing-account-address])
        accounts                 (rf/sub [:wallet/operable-accounts])]
    [:<>
     [quo/drawer-top {:title (i18n/label :t/select-account)}]
     [gesture/flat-list
      {:data                            accounts
       :render-fn                       (fn [account _ _ {:keys [selected-account-address]}]
                                          (if show-account-balances?
                                            [list-item-with-balance
                                             {:account                  account
                                              :selected-account-address selected-account-address
                                              :asset-symbol             asset-symbol
                                              :network                  network}]
                                            [list-item
                                             {:account                  account
                                              :selected-account-address selected-account-address}]))
       :render-data                     {:selected-account-address selected-account-address}
       :content-container-style         style/list-container
       :shows-vertical-scroll-indicator false}]]))
