(ns status-im.contexts.wallet.send.select-asset.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.common.asset-list.view :as asset-list]
    [status-im.contexts.wallet.common.collectibles-tab.view :as collectibles-tab]
    [status-im.contexts.wallet.send.select-asset.style :as style]
    [status-im.setup.hot-reload :as hot-reload]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def tabs-data
  [{:id :tab/assets :label (i18n/label :t/assets) :accessibility-label :assets-tab}
   {:id :tab/collectibles :label (i18n/label :t/collectibles) :accessibility-label :collectibles-tab}])

(defn- search-input
  [search-text on-change-text]
  [rn/view {:style style/search-input-container}
   [quo/input
    {:small?         true
     :placeholder    (i18n/label :t/search-assets)
     :icon-name      :i/search
     :value          search-text
     :on-change-text on-change-text}]])

(defn collectibles-grid
  [search-text]
  (let [collectibles      (rf/sub [:wallet/current-viewing-account-collectibles-filtered search-text])
        search-performed? (not (string/blank? search-text))]
    [collectibles-tab/view
     {:collectibles            collectibles
      :current-account-address (rf/sub [:wallet/current-viewing-account-address])
      :filtered?               search-performed?
      :on-end-reached          #(rf/dispatch [:wallet/request-collectibles-for-current-viewing-account])
      :on-collectible-press    (fn [{:keys [collectible]}]
                                 (rf/dispatch [:wallet/set-collectible-to-send
                                               {:collectible    collectible
                                                :current-screen :screen/wallet.select-asset
                                                :entry-point    :account-send-button}]))}]))

(defn- tab-view
  [search-text selected-tab on-change-text]
  (let [unfiltered-collectibles (rf/sub [:wallet/current-viewing-account-collectibles])
        show-search-input?      (or (= selected-tab :tab/assets)
                                    (and (= selected-tab :tab/collectibles)
                                         (seq unfiltered-collectibles)))
        on-token-press          (fn [token]
                                  (rf/dispatch [:wallet/set-token-to-send
                                                {:token    token
                                                 :stack-id :screen/wallet.select-asset}]))]
    [:<>
     (when show-search-input?
       [search-input search-text on-change-text])
     (case selected-tab
       :tab/assets       [asset-list/view
                          {:search-text    search-text
                           :on-token-press on-token-press}]
       :tab/collectibles [collectibles-grid search-text])]))

(defn view
  []
  (let [[selected-tab set-selected-tab] (rn/use-state (:id (first tabs-data)))
        [search-text set-search-text]   (rn/use-state "")
        on-change-text                  #(set-search-text %)
        on-change-tab                   #(set-selected-tab %)
        on-close                        (fn []
                                          (rf/dispatch [:wallet/clean-selected-token])
                                          (rf/dispatch [:wallet/clean-selected-collectible])
                                          (rf/dispatch [:navigate-back]))]
    (hot-reload/use-safe-unmount (fn []
                                   (rf/dispatch [:wallet/clean-selected-token])
                                   (rf/dispatch [:wallet/clean-selected-collectible])))
    [rn/safe-area-view {:style style/container}
     [account-switcher/view
      {:icon-name     :i/arrow-left
       :on-press      on-close
       :switcher-type :select-account}]
     [quo/page-top
      {:title                     (i18n/label :t/select-token)
       :title-accessibility-label :title-label}]
     [quo/segmented-control
      {:size            32
       :blur?           false
       :symbol          false
       :default-active  :tab/assets
       :container-style {:margin-horizontal 20
                         :margin-vertical   8}
       :data            tabs-data
       :on-change       on-change-tab}]
     [tab-view search-text selected-tab on-change-text]]))
