(ns status-im2.contexts.wallet.home.view
  (:require
    [react-native.core :as rn]
    [quo2.core :as quo]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.common.home.top-nav.view :as common.top-nav]
    [status-im2.contexts.wallet.home.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [status-im2.contexts.wallet.common.temp :as temp]))

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
      :add-divider?        true}]]])

(def account-cards
  [{:name                "Account 1"
    :balance             "€0.00"
    :percentage-value    "€0.00"
    :customization-color :blue
    :type                :empty
    :emoji               "🍑"
    :on-press            #(rf/dispatch [:navigate-to :wallet-accounts])}
   {:customization-color :blue
    :on-press            #(rf/dispatch
                           [:show-bottom-sheet {:content new-account}])
    :type                :add-account}])

(def tabs-data
  [{:id :assets :label (i18n/label :t/assets) :accessibility-label :assets-tab}
   {:id :collectibles :label (i18n/label :t/collectibles) :accessibility-label :collectibles-tab}
   {:id :activity :label (i18n/label :t/activity) :accessibility-label :activity-tab}])

(defn view
  []
  (let [top          (safe-area/get-top)
        selected-tab (reagent/atom (:id (first tabs-data)))]
    (fn []
      [rn/view
       {:style {:margin-top top
                :flex       1}}
       [common.top-nav/view]
       [quo/wallet-overview temp/wallet-overview-state]
       [rn/pressable
        {:on-long-press #(rf/dispatch [:show-bottom-sheet {:content temp/wallet-temporary-navigation}])}
        [quo/wallet-graph {:time-frame :empty}]]
       [rn/flat-list
        {:style      style/accounts-list
         :data       account-cards
         :horizontal true
         :separator  [rn/view {:style {:width 12}}]
         :render-fn  quo/account-card}]
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
                         :content-container-style {:padding-horizontal 8}}]
         :collectibles [quo/empty-state
                        {:title           (i18n/label :t/no-collectibles)
                         :description     (i18n/label :t/no-collectibles-description)
                         :placeholder?    true
                         :container-style style/empty-container-style}]
         [quo/empty-state
          {:title           (i18n/label :t/no-activity)
           :description     (i18n/label :t/empty-tab-description)
           :placeholder?    true
           :container-style style/empty-container-style}])])))
