(ns status-im2.contexts.wallet.account.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.contexts.wallet.account.style :as style]
    [status-im2.contexts.wallet.account.tabs.view :as tabs]
    [status-im2.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im2.contexts.wallet.common.temp :as temp]
    [status-im2.contexts.wallet.common.utils :as utils]
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
<<<<<<< HEAD
      (let [{:keys [name color balance type]} (rf/sub [:wallet/current-viewing-account])
            watch-only?                       (= type :watch)]
=======
<<<<<<< HEAD
      (let [{:keys [name color balance]} (rf/sub [:wallet/current-viewing-account])
           ]
>>>>>>> bb9b6daa9 (updates)
        [rn/view {:style {:flex 1}}
         [account-switcher/view {:on-press #(rf/dispatch [:wallet/close-account-page])}]
=======
<<<<<<< HEAD
      (let [{:keys [name color emoji balance]} (rf/sub [:wallet/current-viewing-account])
            networks                           (rf/sub [:wallet/network-details])]
=======
      (let [account-address (or account-address (rf/sub [:get-screen-params :wallet-accounts]))
            account         (rf/sub [:wallet/account account-address])
            networks        (rf/sub [:wallet/network-details])]
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> 74f8b4535 (updates)
=======
<<<<<<< HEAD
>>>>>>> 347a673ba (updates)
=======
>>>>>>> 22cc56b49 (lint)
        [rn/view {:style {:flex 1}}
         [quo/page-nav
          {:type              :wallet-networks
           :background        :blur
           :icon-name         :i/close
           :on-press          #(rf/dispatch [:wallet/close-account-page])
           :networks          networks
           :networks-on-press #(js/alert "Pressed Networks")
           :right-side        :account-switcher
           :account-switcher  {:customization-color color
                               :on-press            #(rf/dispatch [:show-bottom-sheet
                                                                   {:content account-options/view
                                                                    :gradient-cover? true
                                                                    :customization-color color}])
                               :emoji               emoji}}]
>>>>>>> f327a7f0e (updates)
         [quo/account-overview
          {:current-value       (utils/prettify-balance balance)
           :account-name        name
           :account             (if watch-only? :watched-address :default)
           :customization-color color}]
         [quo/wallet-graph {:time-frame :empty}]
         (when (not watch-only?)
           [quo/wallet-ctas
            {:send-action   #(rf/dispatch [:open-modal :wallet-select-address])
             :buy-action    #(rf/dispatch [:show-bottom-sheet
                                           {:content buy-drawer}])
             :bridge-action #(rf/dispatch [:open-modal :wallet-bridge])}])
         [quo/tabs
<<<<<<< HEAD
          {:style            style/tabs
           :size             32
           :default-active   @selected-tab
           :data             (tabs-data watch-only?)
           :on-change        #(reset! selected-tab %)
           :scrollable?      true
           :scroll-on-press? true}]
         [tabs/view {:selected-tab @selected-tab}]]))))
=======
          {:style          style/tabs
           :size           32
           :default-active @selected-tab
           :data           tabs-data
           :on-change      #(reset! selected-tab %)
           :scrollable?    true}]
<<<<<<< HEAD
         [tabs/view {:selected-tab @selected-tab
                     :account-address account-address
                     :account account}]]))))
>>>>>>> 731614145 (updates)
=======
         [tabs/view
          {:selected-tab    @selected-tab
<<<<<<< HEAD
           :account-address account-address
           :account         account}]]))))
>>>>>>> e7cf3bcf4 (wallet: account real data)
=======
           :account-address account-address}]]))))
>>>>>>> 49ea0601d (lint)
