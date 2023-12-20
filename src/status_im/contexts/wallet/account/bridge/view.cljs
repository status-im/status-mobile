(ns status-im.contexts.wallet.account.bridge.view
  (:require
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [react-native.core :as rn]
    [status-im.contexts.wallet.account.bridge.style :as style]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.common.temp :as temp]
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
     [account-switcher/view
      {:on-press            #(rf/dispatch [:navigate-back])
       :accessibility-label :top-bar}]
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
