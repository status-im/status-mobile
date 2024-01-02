(ns status-im.contexts.wallet.home.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.common.home.top-nav.view :as common.top-nav]
    [status-im.contexts.wallet.home.style :as style]
    [status-im.contexts.wallet.home.tabs.view :as tabs]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn new-account
  []
  [quo/action-drawer
   [[{:icon                :i/add
      :accessibility-label :start-a-new-chat
      :label               (i18n/label :t/add-account)
      :sub-label           (i18n/label :t/add-account-description)
      :on-press            #(rf/dispatch [:navigate-to :wallet-create-account])}
     {:icon                :i/reveal
      :accessibility-label :add-a-contact
      :label               (i18n/label :t/add-address)
      :sub-label           (i18n/label :t/add-address-description)
      :on-press            #(rf/dispatch [:navigate-to :add-address-to-watch])
      :add-divider?        true}]]])

(defn- new-account-card-data
  []
  {:customization-color (rf/sub [:profile/customization-color])
   :on-press            #(rf/dispatch [:show-bottom-sheet {:content new-account}])
   :type                :add-account})

(def tabs-data
  [{:id :assets :label (i18n/label :t/assets) :accessibility-label :assets-tab}
   {:id :collectibles :label (i18n/label :t/collectibles) :accessibility-label :collectibles-tab}
   {:id :activity :label (i18n/label :t/activity) :accessibility-label :activity-tab}])

(defn view
  []
  (let [selected-tab (reagent/atom (:id (first tabs-data)))]
    (fn []
      (let [tokens-loading?             (rf/sub [:wallet/tokens-loading?])
            networks                    (rf/sub [:wallet/network-details])
            account-cards-data          (rf/sub [:wallet/account-cards-data])
            cards                       (conj account-cards-data (new-account-card-data))
            {:keys [formatted-balance]} (rf/sub [:wallet/aggregated-tokens-and-balance])]
        [rn/view {:style (style/home-container)}
         [common.top-nav/view]
         [rn/view {:style style/overview-container}
          [quo/wallet-overview
           {:state      (if tokens-loading? :loading :default)
            :time-frame :none
            :metrics    :none
            :balance    formatted-balance
            :networks   networks}]]
         [quo/wallet-graph {:time-frame :empty}]
         [rn/flat-list
          {:style                             style/accounts-list
           :content-container-style           style/accounts-list-container
           :data                              cards
           :horizontal                        true
           :separator                         [rn/view {:style style/separator}]
           :render-fn                         quo/account-card
           :shows-horizontal-scroll-indicator false}]
         [quo/tabs
          {:style          style/tabs
           :size           32
           :default-active @selected-tab
           :data           tabs-data
           :on-change      #(reset! selected-tab %)}]
         [tabs/view {:selected-tab @selected-tab}]]))))
