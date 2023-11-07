(ns status-im2.contexts.wallet.account.bridge.view
  (:require
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [react-native.core :as rn]
    [status-im2.contexts.wallet.account.bridge.style :as style]
    [status-im2.contexts.wallet.common.sheets.account-options.view :as account-options]
    [status-im2.contexts.wallet.common.temp :as temp]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn network-logo
  [item]
  {:source (quo.resources/get-network (:network-name item))})

(defn view
  []
  (let [networks       (rf/sub [:wallet/network-details])
        networks-logos (map network-logo networks)]
    [rn/view {:style {:flex 1}}
     [quo/page-nav
      {:icon-name           :i/close
       :on-press            #(rf/dispatch [:navigate-back])
       :accessibility-label :top-bar
       :right-side          :account-switcher
       :account-switcher    {:customization-color :purple
                             :on-press            #(rf/dispatch [:show-bottom-sheet
                                                                 {:content account-options/view
                                                                  :gradient-cover? true
                                                                  :customization-color :purple}])
                             :emoji               "🍑"}}]
     [quo/text-combinations
      {:container-style style/header-container
       :title           (i18n/label :t/bridge)}]
     [quo/input
      {:container-style style/input-container
       :icon-name       :i/search
       :placeholder     (i18n/label :t/search-assets)}]
     [rn/flat-list
      {:data                    (temp/bridge-token-list networks-logos)
       :render-fn               quo/token-network
       :content-container-style style/list-content-container}]]))
