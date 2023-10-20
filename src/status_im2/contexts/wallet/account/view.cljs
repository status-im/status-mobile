(ns status-im2.contexts.wallet.account.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.contexts.wallet.account.style :as style]
    [status-im2.contexts.wallet.account.tabs.view :as tabs]
    [status-im2.contexts.wallet.common.temp :as temp]
    [status-im2.contexts.wallet.common.utils :as utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn account-options
  []
  [:<>
   [quo/drawer-top temp/account-data]
   [quo/action-drawer
    [[{:icon                :i/edit
       :accessibility-label :edit
       :label               (i18n/label :t/edit-account)
       :on-press            #(rf/dispatch [:navigate-to :wallet-edit-account])}
      {:icon                :i/copy
       :accessibility-label :copy-address
       :label               (i18n/label :t/copy-address)}
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
     :style     {:margin-horizontal 8}}]])

(defn buy-drawer
  []
  [:<>
   [quo/drawer-top {:title (i18n/label :t/buy-tokens)}]
   [rn/flat-list
    {:data      temp/buy-tokens-list
     :style     {:padding-horizontal 8
                 :padding-bottom     8}
     :render-fn quo/settings-item}]])

(def tabs-data
  [{:id :assets :label (i18n/label :t/assets) :accessibility-label :assets-tab}
   {:id :collectibles :label (i18n/label :t/collectibles) :accessibility-label :collectibles-tab}
   {:id :activity :label (i18n/label :t/activity) :accessibility-label :activity-tab}
   {:id :permissions :label (i18n/label :t/permissions) :accessibility-label :permissions}
   {:id :dapps :label (i18n/label :t/dapps) :accessibility-label :dapps}
   {:id :about :label (i18n/label :t/about) :accessibility-label :about}])

(defn view
  [account-address]
  (let [selected-tab (reagent/atom (:id (first tabs-data)))]
    (fn []
<<<<<<< HEAD
      (let [account-address (or account-address (rf/sub [:get-screen-params :wallet-accounts]))
            account         (rf/sub [:wallet/account account-address])
            networks        (rf/sub [:wallet/network-details])]
        [rn/view {:style style/container}
=======
<<<<<<< HEAD
      (let [networks (rf/sub [:wallet/network-details])]
        [rn/view
         {:style {:flex       1
                  :margin-top top}}
>>>>>>> 7a5ddb266 (updates)
         [quo/page-nav
          {:type              :wallet-networks
           :background        :blur
           :icon-name         :i/close
           :on-press          #(rf/dispatch [:navigate-back])
           :networks          networks
           :networks-on-press #(js/alert "Pressed Networks")
           :right-side        :account-switcher
           :account-switcher  {:customization-color :purple
                               :on-press            #(rf/dispatch [:show-bottom-sheet
                                                                   {:content             account-options
                                                                    :gradient-cover?     true
                                                                    :customization-color :purple}])
                               :emoji               "🍑"}}]
         [quo/account-overview
          {:current-value       (utils/prettify-balance (:balance account))
           :account-name        (:name account)
           :account             :default
           :customization-color :blue}]
         [quo/wallet-graph {:time-frame :empty}]
         [quo/wallet-ctas
          {:send-action #(rf/dispatch [:open-modal :wallet-select-address])
           :buy-action  #(rf/dispatch [:show-bottom-sheet
                                       {:content buy-drawer}])}]
         [quo/tabs
          {:style          style/tabs
           :size           32
           :default-active @selected-tab
           :data           tabs-data
           :on-change      #(reset! selected-tab %)
           :scrollable?    true}]
         [tabs/view {:selected-tab @selected-tab}]]))))
=======
      [rn/view
       {:style {:flex       1
                :margin-top top}}
       [quo/page-nav
        {:type              :wallet-networks
         :background        :blur
         :icon-name         :i/close
         :on-press          #(rf/dispatch [:navigate-back])
         :networks          networks-list
         :networks-on-press #(js/alert "Pressed Networks")
         :right-side        :account-switcher
         :account-switcher  {:customization-color :purple
                             :on-press            #(rf/dispatch [:show-bottom-sheet
                                                                 {:content account-options}])
                             :emoji               "🍑"}}]
       [quo/account-overview temp/account-overview-state]
       [quo/wallet-graph {:time-frame :empty}]
       [quo/wallet-ctas
        {:send-action #(rf/dispatch [:open-modal :wallet-select-address])
         :buy-action  #(rf/dispatch [:show-bottom-sheet
                                     {:content buy-drawer}])
         :bridge-action #(rf/dispatch [:open-modal :wallet-bridge])}]
       [quo/tabs
        {:style          style/tabs
         :size           32
         :default-active @selected-tab
         :data           tabs-data
         :on-change      #(reset! selected-tab %)
         :scrollable?    true}]
       [tabs/view {:selected-tab @selected-tab}]])))
>>>>>>> 3e4945851 (updates)
