(ns status-im.contexts.wallet.bridge.select-asset.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.wallet.bridge.select-asset.style :as style]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.common.asset-list.view :as asset-list]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [search-text (reagent/atom "")]
    (fn []
      [rn/view {:style {:flex 1}}
       [account-switcher/view
        {:on-press            #(rf/dispatch [:navigate-back])
         :accessibility-label :top-bar
         :switcher-type       :select-account}]
       [quo/page-top {:title (i18n/label :t/bridge)}]
       [quo/input
        {:container-style style/input-container
         :icon-name       :i/search
         :on-change-text  #(reset! search-text %)
         :placeholder     (i18n/label :t/search-assets)}]
       [asset-list/view
        {:search-text    @search-text
         :on-token-press (fn [token]
                           (rf/dispatch [:wallet/bridge-select-token
                                         {:token    token
                                          :stack-id :screen/wallet.bridge-select-asset}]))}]])))
