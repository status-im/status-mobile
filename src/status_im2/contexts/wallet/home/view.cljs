(ns status-im2.contexts.wallet.home.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.common.home.top-nav.view :as common.top-nav]
    [status-im2.contexts.wallet.common.activity-tab.view :as activity]
    [status-im2.contexts.wallet.common.collectibles-tab.view :as collectibles]
    [status-im2.contexts.wallet.common.temp :as temp]
    [status-im2.contexts.wallet.home.style :as style]
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
  (rf/dispatch [:wallet/request-collectibles
                {:start-at-index 0
                 :new-request?   true}])
  (let [top          (safe-area/get-top)
        selected-tab (reagent/atom (:id (first tabs-data)))]
    (fn []
      (let [networks           (rf/sub [:wallet/network-details])
            account-cards-data (rf/sub [:wallet/account-cards-data])
            cards              (conj account-cards-data (new-account-card-data))]
        [rn/view {:style {:margin-top top :flex 1}}
         [common.top-nav/view]
         [rn/view {:style style/overview-container}
          [quo/wallet-overview (temp/wallet-overview-state networks)]]
         [rn/pressable
          {:on-long-press #(rf/dispatch [:show-bottom-sheet
                                         {:content temp/wallet-temporary-navigation}])}
          [quo/wallet-graph {:time-frame :empty}]]
         [rn/flat-list
          {:style                             style/accounts-list
           :content-container-style           style/accounts-list-container
           :data                              cards
           :horizontal                        true
           :separator                         [rn/view {:style {:width 12}}]
           :render-fn                         quo/account-card
           :shows-horizontal-scroll-indicator false}]
         [quo/tabs
          {:style          style/tabs
           :size           32
           :default-active @selected-tab
           :data           tabs-data
           :on-change      #(reset! selected-tab %)}]
         (case @selected-tab
           :assets       [rn/flat-list
                          {:render-fn               quo/token-value
                           :data                    temp/tokens
                           :key                     :assets-list
                           :content-container-style {:padding-horizontal 8}}]
           :collectibles [collectibles/view]
           [activity/view])]))))
