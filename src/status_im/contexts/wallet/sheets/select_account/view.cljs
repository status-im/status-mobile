(ns status-im.contexts.wallet.sheets.select-account.view
  (:require [quo.core :as quo]
            quo.theme
            [react-native.gesture :as gesture]
            [status-im.contexts.wallet.sheets.select-account.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- render-account-item
  [{:keys [color address] :as account} _ _ {:keys [selected-account-address]}]
  [quo/account-item
   {:type                :tag
    :token-props         {:symbol "SNT"
                          :value  "1,000"}
    :account-props       (assoc account :customization-color color)
    :customization-color color
    :state               (if (= address selected-account-address) :selected :default)
    :on-press            (fn []
                           (rf/dispatch [:wallet/switch-current-viewing-account address])
                           (rf/dispatch [:hide-bottom-sheet]))}])

(defn view
  [{:keys [show-token-balance? token-symbol]}]
  (let [selected-account-address (rf/sub [:wallet/current-viewing-account-address])
        accounts                 (rf/sub [:wallet/operable-accounts])]
    (tap> show-token-balance?)
    (tap> token-symbol)
    [:<>
     [quo/drawer-top {:title (i18n/label :t/select-account)}]
     [gesture/flat-list
      {:data                            accounts
       :render-fn                       render-account-item
       :render-data                     {:selected-account-address selected-account-address}
       :content-container-style         style/list-container
       :shows-vertical-scroll-indicator false}]]))
