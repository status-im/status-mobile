(ns status-im2.contexts.wallet.account.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.contexts.wallet.account.style :as style]
    [status-im2.contexts.wallet.account.tabs.view :as tabs]
    [status-im2.contexts.wallet.common.sheets.account-options.view :as account-options]
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

(def tabs-data
  [{:id :assets :label (i18n/label :t/assets) :accessibility-label :assets-tab}
   {:id :collectibles :label (i18n/label :t/collectibles) :accessibility-label :collectibles-tab}
   {:id :activity :label (i18n/label :t/activity) :accessibility-label :activity-tab}
   {:id :permissions :label (i18n/label :t/permissions) :accessibility-label :permissions}
   {:id :dapps :label (i18n/label :t/dapps) :accessibility-label :dapps}
   {:id :about :label (i18n/label :t/about) :accessibility-label :about}])

(defn view
  []
  (let [selected-tab (reagent/atom (:id (first tabs-data)))]
    (fn []
      (let [{:keys [name color emoji balance]} (rf/sub [:wallet/current-viewing-account])
            networks                           (rf/sub [:wallet/network-details])]
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
         [quo/account-overview
          {:current-value       (utils/prettify-balance balance)
           :account-name        name
           :account             :default
           :customization-color color}]
         [quo/wallet-graph {:time-frame :empty}]
         [quo/wallet-ctas
          {:send-action   #(rf/dispatch [:open-modal :wallet-select-address])
           :buy-action    #(rf/dispatch [:show-bottom-sheet
                                         {:content buy-drawer}])
           :bridge-action #(rf/dispatch [:open-modal :wallet-bridge])}]
         [quo/tabs
          {:style            style/tabs
           :size             32
           :default-active   @selected-tab
           :data             tabs-data
           :on-change        #(reset! selected-tab %)
           :scrollable?      true
           :scroll-on-press? true}]
         [tabs/view {:selected-tab @selected-tab}]]))))
