(ns status-im.contexts.wallet.home.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [status-im.common.home.top-nav.view :as common.top-nav]
    [status-im.common.refreshable-flat-list.view :as refreshable-flat-list]
    [status-im.contexts.wallet.home.style :as style]
    [status-im.contexts.wallet.home.tabs.view :as tabs]
    [status-im.contexts.wallet.sheets.network-filter.view :as network-filter]
    [status-im.feature-flags :as ff]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn new-account
  []
  (let [keycard? (rf/sub [:keycard/keycard-profile?])]
    [quo/action-drawer
     [[{:icon                :i/add
        :accessibility-label :start-a-new-chat
        :label               (i18n/label :t/add-account)
        :sub-label           (i18n/label :t/add-account-description)
        :on-press            (fn []
                               (if keycard?
                                 (rf/dispatch [:keycard/feature-unavailable-show
                                               {:feature-name :wallet.new-account}])
                                 (rf/dispatch [:navigate-to :screen/wallet.create-account])))}
       (when (ff/enabled? ::ff/wallet.add-watched-address)
         {:icon                :i/reveal
          :accessibility-label :add-a-contact
          :label               (i18n/label :t/add-address-to-watch)
          :sub-label           (i18n/label :t/add-address-to-watch-description)
          :on-press            #(rf/dispatch [:navigate-to :screen/wallet.add-address-to-watch])
          :add-divider?        true})]]]))

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

(defn- change-tab [id] (rf/dispatch [:wallet/select-home-tab id]))

(defn- render-cards
  [cards ref]
  [rn/flat-list
   {:ref                               #(reset! ref %)
    :style                             style/accounts-list
    :content-container-style           style/accounts-list-container
    :data                              cards
    :horizontal                        true
    :separator                         [rn/view {:style style/separator}]
    :render-fn                         (fn [item] [quo/account-card item])
    :shows-horizontal-scroll-indicator false}])

(defn- render-tabs
  [data on-change default-active]
  [quo/tabs
   {:style          style/tabs
    :size           32
    :default-active default-active
    :data           data
    :on-change      on-change}])

(defn view
  []
  (let [selected-tab                   (rf/sub [:wallet/home-tab])
        account-list-ref               (rn/use-ref-atom nil)
        tokens-loading?                (rf/sub [:wallet/home-tokens-loading?])
        networks                       (rf/sub [:wallet/selected-network-details])
        account-cards-data             (rf/sub [:wallet/account-cards-data])
        cards                          (conj account-cards-data (new-account-card-data))
        [init-loaded? set-init-loaded] (rn/use-state false)
        {:keys [formatted-balance]}    (rf/sub [:wallet/aggregated-token-values-and-balance])
        theme                          (quo.theme/use-theme)]
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
     [refreshable-flat-list/view
      {:refresh-control         [rn/refresh-control
                                 {:refreshing (and tokens-loading? init-loaded?)
                                  :colors     [colors/neutral-40]
                                  :tint-color colors/neutral-40
                                  :on-refresh #(rf/dispatch [:wallet/get-accounts])}]
       :header                  [rn/view {:style (style/header-container theme)}
                                 [quo/wallet-overview
                                  {:state             (if tokens-loading? :loading :default)
                                   :time-frame        :none
                                   :metrics           :none
                                   :balance           formatted-balance
                                   :networks          networks
                                   :dropdown-on-press #(rf/dispatch [:show-bottom-sheet
                                                                     {:content network-filter/view}])}]
                                 (when (ff/enabled? ::ff/wallet.graph)
                                   [quo/wallet-graph {:time-frame :empty}])
                                 [render-cards cards account-list-ref]
                                 [render-tabs tabs-data change-tab selected-tab]]
       :content-container-style style/list-container
       :sticky-header-indices   [0]
       :data                    []
       :render-fn               #()
       :footer                  [tabs/view {:selected-tab selected-tab}]}]]))
