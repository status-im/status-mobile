(ns status-im.contexts.wallet.home.view
  (:require
    [quo.context]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [status-im.common.home.top-nav.view :as common.top-nav]
    [status-im.common.refreshable-flat-list.view :as refreshable-flat-list]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.home.style :as style]
    [status-im.contexts.wallet.home.tabs.view :as tabs]
    [status-im.contexts.wallet.sheets.buy-token.view :as buy-token]
    [status-im.contexts.wallet.sheets.network-filter.view :as network-filter]
    [status-im.feature-flags :as ff]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn new-account
  []
  (let [watched-accounts             (rf/sub [:wallet/watch-only-accounts])
        reached-max-watched-account? (>= (count watched-accounts)
                                         constants/max-allowed-watched-accounts)
        on-add-address-press         (rn/use-callback
                                      (fn []
                                        (if reached-max-watched-account?
                                          (rf/dispatch [:toasts/upsert
                                                        {:type :negative
                                                         :theme :dark
                                                         :text
                                                         (i18n/label
                                                          :t/saved-addresses-limit-reached-toast)}])
                                          (rf/dispatch [:navigate-to
                                                        :screen/wallet.add-address-to-watch])))
                                      [reached-max-watched-account?])]
    [quo/action-drawer
     [[{:icon                :i/add
        :accessibility-label :start-a-new-chat
        :label               (i18n/label :t/add-account)
        :sub-label           (i18n/label :t/add-account-description)
        :on-press            #(rf/dispatch [:navigate-to :screen/wallet.create-account])}
       {:icon                :i/reveal
        :accessibility-label :add-a-contact
        :label               (i18n/label :t/add-address-to-watch)
        :sub-label           (i18n/label :t/add-address-to-watch-description)
        :on-press            on-add-address-press
        :add-divider?        true}]]]))

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

(defn- call-to-actions
  []
  (let [operable-accounts     (rf/sub [:wallet/operable-accounts])
        testnet-mode?         (rf/sub [:profile/test-networks-enabled?])
        multiple-accounts?    (> (count operable-accounts) 1)
        first-account-address (:address (first operable-accounts))
        on-send-press         (rn/use-callback
                               (fn []
                                 (rf/dispatch [:wallet/clean-send-data])
                                 (when-not multiple-accounts?
                                   (rf/dispatch [:wallet/switch-current-viewing-account
                                                 first-account-address]))
                                 (if multiple-accounts?
                                   (rf/dispatch [:open-modal :screen/wallet.select-from])
                                   (rf/dispatch [:wallet/wizard-navigate-forward
                                                 {:start-flow? true
                                                  :flow-id     :wallet-send-flow}])))
                               [multiple-accounts? first-account-address])
        on-receive-press      (rn/use-callback #(rf/dispatch [:open-modal :screen/share-shell
                                                              {:initial-tab        :wallet
                                                               :status             :receive
                                                               :hide-tab-selector? true}]))
        on-buy-press          (rn/use-callback #(rf/dispatch [:show-bottom-sheet
                                                              {:content buy-token/view}]))
        on-bridge-press       (rn/use-callback
                               (fn []
                                 ;; For a single account, it starts the bridge flow immediately. For
                                 ;; multiple accounts, it sets the transaction type and starts the
                                 ;; bridge flow after account selection.
                                 (rf/dispatch [:wallet/clean-send-data])
                                 (when-not multiple-accounts?
                                   (rf/dispatch [:wallet/switch-current-viewing-account
                                                 first-account-address])
                                   (rf/dispatch [:wallet/start-bridge]))
                                 (when multiple-accounts?
                                   (rf/dispatch [:wallet/set-send-tx-type :tx/bridge])
                                   (rf/dispatch [:open-modal :screen/wallet.select-from])))
                               [multiple-accounts? first-account-address])
        on-swap-press         (rn/use-callback
                               (fn []
                                 (rf/dispatch [:wallet/clean-send-data])
                                 (when-not multiple-accounts?
                                   (rf/dispatch [:wallet/switch-current-viewing-account
                                                 first-account-address]))
                                 (if multiple-accounts?
                                   (rf/dispatch [:open-modal
                                                 :screen/wallet.swap-select-account])
                                   (rf/dispatch [:open-modal
                                                 :screen/wallet.swap-select-asset-to-pay])))
                               [multiple-accounts? first-account-address])]
    [quo/wallet-ctas
     {:container-style  style/cta-buttons
      :send-action      on-send-press
      :receive-action   on-receive-press
      :buy-action       on-buy-press
      :bridge-action    on-bridge-press
      :swap-action      on-swap-press
      :bridge-disabled? testnet-mode?
      :swap-disabled?   testnet-mode?}]))

(defn view
  []
  (let [selected-tab                   (rf/sub [:wallet/home-tab])
        account-list-ref               (rn/use-ref-atom nil)
        tokens-loading?                (rf/sub [:wallet/home-tokens-loading?])
        networks                       (rf/sub [:wallet/filtered-networks])
        networks-filtered?             (rf/sub [:wallet/network-filter?])
        account-cards-data             (rf/sub [:wallet/account-cards-data])
        cards                          (conj account-cards-data (new-account-card-data))
        [init-loaded? set-init-loaded] (rn/use-state false)
        {:keys [formatted-balance]}    (rf/sub [:wallet/aggregated-token-values-and-balance])
        theme                          (quo.context/use-theme)
        show-new-chain-indicator?      (rf/sub [:wallet/show-new-chain-indicator?])
        on-press-network-selector      (rn/use-callback
                                        (fn []
                                          (if networks-filtered?
                                            (rf/dispatch [:wallet/reset-network-balances-filter])
                                            (do (rf/dispatch [:wallet/mark-new-networks-as-seen])
                                                (rf/dispatch [:show-bottom-sheet
                                                              {:content network-filter/view}]))))
                                        [networks-filtered?])]
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
                                  {:state                     (if tokens-loading? :loading :default)
                                   :time-frame                :none
                                   :metrics                   :none
                                   :balance                   formatted-balance
                                   :networks                  networks
                                   :networks-filtered?        networks-filtered?
                                   :dropdown-on-press         on-press-network-selector
                                   :show-new-chain-indicator? (when (not-empty networks)
                                                                show-new-chain-indicator?)}]
                                 (when (ff/enabled? ::ff/wallet.graph)
                                   [quo/wallet-graph {:time-frame :empty}])
                                 [render-cards cards account-list-ref]
                                 [call-to-actions]
                                 [render-tabs tabs-data change-tab selected-tab]]
       :content-container-style style/list-container
       :sticky-header-indices   [0]
       :data                    []
       :render-fn               #()
       :footer                  [tabs/view {:selected-tab selected-tab}]}]]))
