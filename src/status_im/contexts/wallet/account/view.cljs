(ns status-im.contexts.wallet.account.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.account.style :as style]
    [status-im.contexts.wallet.account.tabs.view :as tabs]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.sheets.buy-token.view :as buy-token]
    [status-im.feature-flags :as ff]
    [status-im.setup.hot-reload :as hot-reload]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def first-tab-id :assets)

(def tabs-data
  [{:id :assets :label (i18n/label :t/assets) :accessibility-label :assets-tab}
   {:id :collectibles :label (i18n/label :t/collectibles) :accessibility-label :collectibles-tab}
   {:id :activity :label (i18n/label :t/activity) :accessibility-label :activity-tab}
   {:id :about :label (i18n/label :t/about) :accessibility-label :about}])

(defn- change-tab [id] (rf/dispatch [:wallet/select-account-tab id]))

(defn view
  []
  (let [selected-tab          (or (rf/sub [:wallet/account-tab]) first-tab-id)
        {:keys [name color formatted-balance
                watch-only?]} (rf/sub [:wallet/current-viewing-account])
        customization-color   (rf/sub [:profile/customization-color])]
    (hot-reload/use-safe-unmount (fn []
                                   (rf/dispatch [:wallet/close-account-page])
                                   (rf/dispatch [:wallet/clean-current-viewing-account])))
    (rn/use-mount
     #(rf/dispatch [:wallet/fetch-activities-for-current-account]))
    [rn/view {:style {:flex 1}}
     [account-switcher/view
      {:type     :wallet-networks
       :on-press #(rf/dispatch [:pop-to-root :shell-stack])}]
     [quo/account-overview
      {:container-style     style/account-overview
       :current-value       formatted-balance
       :account-name        name
       :account             (if watch-only? :watched-address :default)
       :customization-color color}]
     (when (ff/enabled? ::ff/wallet.graph)
       [quo/wallet-graph {:time-frame :empty}])
     (when (not watch-only?)
       [quo/wallet-ctas
        {:container-style style/cta-buttons
         :send-action     (fn []
                            (rf/dispatch [:wallet/clean-send-data])
                            (rf/dispatch [:wallet/wizard-navigate-forward
                                          {:start-flow? true
                                           :flow-id     :wallet-send-flow}]))
         :receive-action  #(rf/dispatch [:open-modal :screen/wallet.share-address
                                         {:status :receive}])
         :buy-action      #(rf/dispatch [:show-bottom-sheet
                                         {:content buy-token/view}])
         :bridge-action   (fn []
                            (rf/dispatch [:wallet/clean-send-data])
                            (rf/dispatch [:wallet/start-bridge]))
         :swap-action     (when (ff/enabled? ::ff/wallet.swap)
                            #(rf/dispatch [:open-modal :screen/wallet.swap-select-asset-to-pay]))}])
     [quo/tabs
      {:style            style/tabs
       :size             32
       :active-tab-id    selected-tab
       :data             tabs-data
       :on-change        change-tab
       :scrollable?      true
       :scroll-on-press? true}]
     [tabs/view {:selected-tab selected-tab}]
     (when (ff/enabled? ::ff/shell.jump-to)
       [quo/floating-shell-button
        {:jump-to
         {:on-press            #(rf/dispatch [:shell/navigate-to-jump-to])
          :customization-color customization-color
          :label               (i18n/label :t/jump-to)}}
        style/shell-button])]))
