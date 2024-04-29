(ns status-im.contexts.wallet.home.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.common.home.top-nav.view :as common.top-nav]
    [status-im.contexts.wallet.home.style :as style]
    [status-im.contexts.wallet.home.tabs.view :as tabs]
    [status-im.contexts.wallet.sheets.network-filter.view :as network-filter]
    [status-im.feature-flags :as ff]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn new-account
  []
  [quo/action-drawer
   [[{:icon                :i/add
      :accessibility-label :start-a-new-chat
      :label               (i18n/label :t/add-account)
      :sub-label           (i18n/label :t/add-account-description)
      :on-press            #(rf/dispatch [:navigate-to :screen/wallet.create-account])}
     {:icon                :i/reveal
      :accessibility-label :add-a-contact
      :label               (i18n/label :t/add-address)
      :sub-label           (i18n/label :t/add-address-description)
      :on-press            #(rf/dispatch [:navigate-to :screen/wallet.add-address-to-watch])
      :add-divider?        true}]]])

(defn- new-account-card-data
  []
  {:customization-color (rf/sub [:profile/customization-color])
   :on-press            #(rf/dispatch [:show-bottom-sheet {:content new-account}])
   :type                :add-account})

(def tabs-data
  [{:id :assets :label (i18n/label :t/assets) :accessibility-label :assets-tab}
   {:id :collectibles :label (i18n/label :t/collectibles) :accessibility-label :collectibles-tab}
   (when (ff/enabled? ::ff/wallet.home-activity)
     {:id :activity :label (i18n/label :t/activity) :accessibility-label :activity-tab})])

(defn view
  []
  (let [[selected-tab set-selected-tab] (rn/use-state (:id (first tabs-data)))
        account-list-ref                (rn/use-ref-atom nil)
        tokens-loading?                 (rf/sub [:wallet/tokens-loading?])
        networks                        (rf/sub [:wallet/selected-network-details])
        account-cards-data              (rf/sub [:wallet/account-cards-data])
        cards                           (conj account-cards-data (new-account-card-data))
        [init-loaded? set-init-loaded]  (rn/use-state false)
        {:keys [formatted-balance]}     (rf/sub [:wallet/aggregated-token-values-and-balance])]
    (rn/use-effect (fn []
                     (when (and @account-list-ref (pos? (count cards)))
                       (.scrollToOffset ^js @account-list-ref
                                        #js
                                         {:animated true
                                          :offset   0})))
                   [(count cards)])
    (rn/use-effect
     #(when (and (boolean? tokens-loading?) (not tokens-loading?) (not init-loaded?))
        (set-init-loaded true))
     [tokens-loading?])
    [rn/view {:style (style/home-container)}
     [common.top-nav/view]
     [rn/scroll-view
      {:refresh-control (reagent/as-element
                         [rn/refresh-control {:refreshing (and tokens-loading? init-loaded?)
                                              :colors     colors/neutral-40
                                              :tint-color colors/neutral-40
                                              :on-refresh #(rf/dispatch [:wallet/get-accounts])}])
       :style {:flex 1}}
      [rn/view
       [quo/wallet-overview
        {:state             (if tokens-loading? :loading :default)
         :time-frame        :none
         :metrics           :none
         :balance           formatted-balance
         :networks          networks
         :dropdown-on-press #(rf/dispatch [:show-bottom-sheet {:content network-filter/view}])}]]
      (when (ff/enabled? ::ff/wallet.graph) [quo/wallet-graph {:time-frame :empty}])
      [rn/flat-list
       {:ref                               #(reset! account-list-ref %)
        :style                             style/accounts-list
        :content-container-style           style/accounts-list-container
        :data                              cards
        :horizontal                        true
        :separator                         [rn/view {:style style/separator}]
        :render-fn                         (fn [item] [quo/account-card item])
        :shows-horizontal-scroll-indicator false}]
      [quo/tabs
       {:style          style/tabs
        :size           32
        :default-active selected-tab
        :data           tabs-data
        :on-change      #(set-selected-tab %)}]
      [tabs/view {:selected-tab selected-tab}]]]))
