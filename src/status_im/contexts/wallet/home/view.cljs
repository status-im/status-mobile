(ns status-im.contexts.wallet.home.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [status-im.common.home.top-nav.view :as common.top-nav]
    [status-im.config :as config]
    [status-im.contexts.wallet.home.style :as style]
    [status-im.contexts.wallet.home.tabs.view :as tabs]
    [status-im.contexts.wallet.sheets.network-filter.view :as network-filter]
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
   {:id :activity :label (i18n/label :t/activity) :accessibility-label :activity-tab}])

(defn option
  [{:keys [icon label on-press danger? sub-label add-divider? accessibility-label right-icon] :as opt}]
  (when opt
    ^{:key label}
    {:icon                icon
     :label               label
     :on-press            on-press
     :danger?             danger?
     :sub-label           sub-label
     :right-icon          right-icon
     :add-divider?        add-divider?
     :accessibility-label accessibility-label}))

(defn options
  [account-name address]
  [(when config/show-not-implemented-features?
     {:icon                :i/arrow-up
      :label               (i18n/label :t/send-to-user {:user account-name})
      :on-press            #(js/alert "TODO: to be implemented, requires design input")
      :accessibility-label :manage-notifications})
   (when config/show-not-implemented-features?
     {:icon                :i/link
      :right-icon          :i/external
      :label               (i18n/label :t/view-address-on-website {:website "Etherscan"})
      :on-press            #(js/alert "TODO: to be implemented, requires design input")
      :accessibility-label :manage-notifications})
   (when config/show-not-implemented-features?
     {:icon                :i/link
      :right-icon          :i/external
      :label               (i18n/label :t/view-address-on-website {:website "Optimistic"})
      :on-press            #(js/alert "TODO: to be implemented, requires design input")
      :accessibility-label :manage-notifications})
   {:icon                :i/share
    :on-press            #(rf/dispatch
                           [:open-share
                            {:options (if platform/ios?
                                        {:activityItemSources [{:placeholderItem {:type    :text
                                                                                  :content address}
                                                                :item            {:default {:type :text
                                                                                            :content
                                                                                            address}}
                                                                :linkMetadata    {:title address}}]}
                                        {:title     address
                                         :subject   address
                                         :message   address
                                         :isNewTask true})}])
    :label               (i18n/label :t/share-address)
    :accessibility-label :manage-notifications}
   {:icon                :i/qr-code
    :label               (i18n/label :t/show-address-qr)
    :on-press            (fn []
                           (rf/dispatch [:wallet/set-current-viewing-account address])
                           (rf/dispatch [:open-modal :screen/wallet.share-address {:status :share}]))
    :accessibility-label :manage-notifications}
   (when config/show-not-implemented-features?
     {:icon                :i/edit
      :label               (i18n/label :t/edit-account)
      :on-press            #(rf/dispatch [:navigate-to :screen/wallet.edit-account])
      :accessibility-label :manage-notifications})
   (when config/show-not-implemented-features?
     {:icon                :i/delete
      :label               (i18n/label :t/remove-account)
      :on-press            #(js/alert "TODO: to be implemented, requires design input")
      :danger?             true
      :accessibility-label :manage-notifications
      :add-divider?        true})])

(defn sample-options
  [account-name address]
  (keep option (options account-name address)))

(defn account-sheet
  [{:keys [address] account-name :name}]
  [quo/action-drawer
   [(sample-options account-name address)]])

(defn view
  []
  (let [[selected-tab set-selected-tab] (rn/use-state (:id (first tabs-data)))
        account-list-ref                (rn/use-ref-atom nil)
        tokens-loading?                 (rf/sub [:wallet/tokens-loading?])
        networks                        (rf/sub [:wallet/selected-network-details])
        account-cards-data              (rf/sub [:wallet/account-cards-data])
        cards                           (conj account-cards-data (new-account-card-data))
        {:keys [formatted-balance]}     (rf/sub [:wallet/aggregated-token-values-and-balance])]
    (rn/use-effect (fn []
                     (when (and @account-list-ref (pos? (count cards)))
                       (.scrollToOffset ^js @account-list-ref
                                        #js
                                         {:animated true
                                          :offset   0})))
                   [(count cards)])
    [rn/view {:style (style/home-container)}
     [common.top-nav/view]
     [rn/view {:style style/overview-container}
      [quo/wallet-overview
       {:state             (if tokens-loading? :loading :default)
        :time-frame        :none
        :metrics           :none
        :balance           formatted-balance
        :networks          networks
        :dropdown-on-press #(rf/dispatch [:show-bottom-sheet {:content network-filter/view}])}]]
     [quo/wallet-graph {:time-frame :empty}]
     [rn/flat-list
      {:ref                               #(reset! account-list-ref %)
       :style                             style/accounts-list
       :content-container-style           style/accounts-list-container
       :data                              cards
       :horizontal                        true
       :separator                         [rn/view {:style style/separator}]
       :render-fn                         (fn [{:keys        [address ens-name color]
                                                account-name :name
                                                :as          item}]
                                            (let [updated-item (assoc item
                                                                      :on-long-press
                                                                      (fn []
                                                                        (rf/dispatch
                                                                         [:show-bottom-sheet
                                                                          {:selected-item
                                                                           (fn []
                                                                             [quo/saved-address
                                                                              {:active-state? false
                                                                               :user-props
                                                                               {:name account-name
                                                                                :address address
                                                                                :ens ens-name
                                                                                :customization-color
                                                                                color}}])
                                                                           :content (fn []
                                                                                      [account-sheet
                                                                                       item])}])))]
                                              [quo/account-card updated-item]))
       :shows-horizontal-scroll-indicator false}]
     [quo/tabs
      {:style          style/tabs
       :size           32
       :default-active selected-tab
       :data           tabs-data
       :on-change      #(set-selected-tab %)}]
     [tabs/view {:selected-tab selected-tab}]]))
