(ns status-im.contexts.wallet.account.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.wallet.account.style :as style]
    [status-im.contexts.wallet.account.tabs.view :as tabs]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.common.temp :as temp]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn buy-drawer
  []
  [:<>
   [quo/drawer-top {:title (i18n/label :t/buy-tokens)}]
   [rn/flat-list
    {:data      temp/buy-tokens-list
     :style     {:padding-horizontal 8
                 :padding-bottom     8}
     :render-fn quo/settings-item}]])

(def first-tab-id :assets)

(defn tabs-data
  [watch-only?]
  (cond-> [{:id :assets :label (i18n/label :t/assets) :accessibility-label :assets-tab}
           {:id :collectibles :label (i18n/label :t/collectibles) :accessibility-label :collectibles-tab}
           {:id :activity :label (i18n/label :t/activity) :accessibility-label :activity-tab}]
    (not watch-only?) (conj {:id :dapps :label (i18n/label :t/dapps) :accessibility-label :dapps})
    true              (conj {:id :about :label (i18n/label :t/about) :accessibility-label :about})))

(defn view
  []
  (let [selected-tab (reagent/atom first-tab-id)]
    (fn []
      (let [{:keys [name color formatted-balance
                    watch-only?]} (rf/sub [:wallet/current-viewing-account])]
        [rn/view {:style {:flex 1}}
         [account-switcher/view {:on-press #(rf/dispatch [:wallet/close-account-page])}]
         [quo/account-overview
          {:current-value       formatted-balance
           :account-name        name
           :account             (if watch-only? :watched-address :default)
           :customization-color color}]
         [quo/wallet-graph {:time-frame :empty}]
         (when (not watch-only?)
           [quo/wallet-ctas
            {:send-action    #(rf/dispatch [:open-modal :wallet-select-address])
             :receive-action #(rf/dispatch [:open-modal :wallet-receive])
             :buy-action     #(rf/dispatch [:show-bottom-sheet
                                            {:content buy-drawer}])
             :bridge-action  #(rf/dispatch [:open-modal :wallet-bridge])}])
         [quo/tabs
          {:style            style/tabs
           :size             32
           :default-active   @selected-tab
           :data             (tabs-data watch-only?)
           :on-change        #(reset! selected-tab %)
           :scrollable?      true
           :scroll-on-press? true}]
         [tabs/view {:selected-tab @selected-tab}]]))))
