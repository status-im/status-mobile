(ns status-im2.contexts.wallet.account.bridge.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im2.contexts.wallet.account.bridge.style :as style]
    [status-im2.contexts.wallet.common.account-options.view :as account-options]
    [status-im2.contexts.wallet.common.temp :as temp]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [top (safe-area/get-top)]
    [rn/view {:style (style/container top)}
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
                             :state               :default
                             :emoji               "🍑"}}]
     [quo/text-combinations
      {:container-style style/header-container
       :title           (i18n/label :t/bridge)}]
     [quo/input
      {:container-style style/input-container
       :icon-name       :i/search
       :placeholder     (i18n/label :t/search-assets)}]
     [rn/flat-list
      {:data                    temp/bridge-token-list
       :render-fn               quo/token-network
       :content-container-style style/list-content-container}]]))
