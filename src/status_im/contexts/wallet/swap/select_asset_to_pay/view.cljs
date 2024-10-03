(ns status-im.contexts.wallet.swap.select-asset-to-pay.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.common.asset-list.view :as asset-list]
    [status-im.contexts.wallet.swap.select-asset-to-pay.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- search-input
  [search-text on-change-text]
  [rn/view {:style style/search-input-container}
   [quo/input
    {:small?         true
     :placeholder    (i18n/label :t/search-assets)
     :icon-name      :i/search
     :value          search-text
     :on-change-text on-change-text}]])

(defn- assets-view
  [search-text on-change-text]
  (let [on-token-press (fn [token]
                         (rf/dispatch [:wallet.swap/start
                                       {:asset-to-pay     token
                                        :open-new-screen? false}]))]
    [:<>
     [search-input search-text on-change-text]
     [asset-list/view
      {:search-text    search-text
       :on-token-press on-token-press}]]))

(defn view
  []
  (let [[search-text set-search-text] (rn/use-state "")
        on-change-text                #(set-search-text %)
        on-close                      (fn []
                                        (rf/dispatch [:wallet/clean-swap])
                                        (rf/dispatch [:navigate-back]))]
    [rn/safe-area-view {:style style/container}
     [account-switcher/view
      {:on-press      on-close
       :switcher-type :select-account}]
     [quo/page-top
      {:title                     (i18n/label :t/select-asset-to-pay)
       :title-accessibility-label :title-label}]
     [assets-view search-text on-change-text]]))
