(ns status-im2.contexts.wallet.account.view
  (:require [quo2.core :as quo]
            [quo2.foundations.resources :as quo.resources]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [status-im2.contexts.wallet.account.style :as style]
            [status-im2.contexts.wallet.account.tabs.view :as tabs]
            [status-im2.contexts.wallet.common.temp :as temp]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn account-options
  []
  [:<>
   [quo/drawer-top temp/account-data]
   [quo/action-drawer
    [[{:icon                :i/edit
       :accessibility-label :edit
       :label               (i18n/label :t/edit-account)}
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
   [quo/divider-line]
   [rn/view
    {:style {:padding-horizontal 20
             :padding-top        12
             :padding-bottom     8}}
    [quo/section-label {:section (i18n/label :t/select-another-account)}]]
   [rn/flat-list
    {:data      temp/other-accounts
     :render-fn (fn [account] [quo/account-item {:account-props account}])
     :style     {:margin-horizontal 8}}]])

(def ^:private networks-list
  [{:source (quo.resources/get-network :ethereum)}
   {:source (quo.resources/get-network :optimism)}
   {:source (quo.resources/get-network :arbitrum)}])

(def tabs-data
  [{:id :assets :label (i18n/label :t/assets) :accessibility-label :assets-tab}
   {:id :collectibles :label (i18n/label :t/collectibles) :accessibility-label :collectibles-tab}
   {:id :activity :label (i18n/label :t/activity) :accessibility-label :activity-tab}
   {:id :permissions :label (i18n/label :t/permissions) :accessibility-label :permissions}
   {:id :dapps :label (i18n/label :t/dapps) :accessibility-label :dapps}
   {:id :about :label (i18n/label :t/about) :accessibility-label :about}])

(defn view
  []
  (let [top          (safe-area/get-top)
        selected-tab (reagent/atom (:id (first tabs-data)))]
    (fn []
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
        {:send-action #(rf/dispatch [:open-modal :wallet-select-address])}]
       [quo/tabs
        {:style          style/tabs
         :size           32
         :default-active @selected-tab
         :data           tabs-data
         :on-change      #(reset! selected-tab %)
         :scrollable?    true}]
       [tabs/view {:selected-tab @selected-tab}]])))
