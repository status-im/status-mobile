(ns status-im.contexts.wallet.account.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.account.style :as style]
    [status-im.contexts.wallet.account.tabs.view :as tabs]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.sheets.buy-token.view :as buy-token]
    [status-im.feature-flags :as ff]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def first-tab-id :assets)

(defn tabs-data
  [watch-only?]
  (cond-> [{:id :assets :label (i18n/label :t/assets) :accessibility-label :assets-tab}
           {:id :collectibles :label (i18n/label :t/collectibles) :accessibility-label :collectibles-tab}
           {:id :activity :label (i18n/label :t/activity) :accessibility-label :activity-tab}]
    (not watch-only?) (conj {:id :dapps :label (i18n/label :t/dapps) :accessibility-label :dapps})
    true              (conj {:id :about :label (i18n/label :t/about) :accessibility-label :about})))

(defn- change-tab [id] (rf/dispatch [:wallet/select-account-tab id]))

(defn view
  []
  (let [selected-tab          (or (rf/sub [:wallet/account-tab]) first-tab-id)
        {:keys [name color formatted-balance
                watch-only?]} (rf/sub [:wallet/current-viewing-account])
        customization-color   (rf/sub [:profile/customization-color])]
    (rn/use-unmount #(rf/dispatch [:wallet/clean-send-data]))
    (rn/use-mount
     #(rf/dispatch [:wallet/fetch-activities-for-current-account]))
    [rn/view {:style {:flex 1}}
     [account-switcher/view
      {:type     :wallet-networks
       :on-press (fn []
                   (rf/dispatch [:wallet/close-account-page]))}]
     [quo/account-overview
      {:container-style     style/account-overview
       :current-value       formatted-balance
       :account-name        name
       :account             (if watch-only? :watched-address :default)
       :customization-color color}]
     (when (ff/enabled? ::ff/wallet.graph) [quo/wallet-graph {:time-frame :empty}])
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
                            (rf/dispatch [:wallet/wizard-navigate-forward
                                          {:start-flow? true
                                           :flow-id     :wallet-bridge-flow}]))
         :swap-action     (when (ff/enabled? ::ff/wallet.swap)
                            #(rf/dispatch [:wallet.swap/start]))}])
     [quo/tabs
      {:style            style/tabs
       :size             32
       :active-tab-id    selected-tab
       :data             (tabs-data watch-only?)
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
