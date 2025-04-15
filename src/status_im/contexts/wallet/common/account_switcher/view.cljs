(ns status-im.contexts.wallet.common.account-switcher.view
  (:require
    [quo.core :as quo]
    [status-im.contexts.wallet.sheets.account-options.view :as account-options]
    [status-im.contexts.wallet.sheets.network-filter.view :as network-filter]
    [status-im.contexts.wallet.sheets.select-account.view :as select-account]
    [status-im.feature-flags :as ff]
    [utils.re-frame :as rf]))

(defn get-bottom-sheet-args
  [switcher-type params]
  (case switcher-type
    :account-options {:content      account-options/view
                      :hide-handle? true}
    :select-account  {:content (fn []
                                 [select-account/view params])}
    nil))

(defn- on-switcher-press
  [switcher-type params]
  (rf/dispatch [:show-bottom-sheet (get-bottom-sheet-args switcher-type params)]))

(defn view
  [{:keys [type on-press accessibility-label icon-name switcher-type margin-top params
           show-dapps-button?]
    :or   {icon-name           :i/close
           accessibility-label :top-bar
           switcher-type       :account-options
           show-dapps-button?  false
           type                :no-title}}]
  (let [{:keys [color emoji watch-only?]} (rf/sub [:wallet/current-viewing-account])
        networks                          (rf/sub [:wallet/filtered-networks])
        sending-collectible?              (rf/sub [:wallet/sending-collectible?])
        show-new-chain-indicator?         (rf/sub [:wallet/show-new-chain-indicator?])
        networks-filtered?                (rf/sub [:wallet/network-filter?])
        on-press-networks                 (fn []
                                            (if networks-filtered?
                                              (rf/dispatch [:wallet/reset-network-balances-filter])
                                              (do (rf/dispatch [:wallet/mark-new-networks-as-seen])
                                                  (rf/dispatch [:show-bottom-sheet
                                                                {:content network-filter/view}]))))]
    [quo/page-nav
     {:type                      type
      :icon-name                 icon-name
      :margin-top                margin-top
      :background                :blur
      :on-press                  on-press
      :accessibility-label       accessibility-label
      :networks                  networks
      :networks-filtered?        networks-filtered?
      :align-center?             true
      :networks-on-press         on-press-networks
      :show-new-chain-indicator? show-new-chain-indicator?
      :right-side                [(when (and (ff/enabled? ::ff/wallet.wallet-connect)
                                             (not watch-only?)
                                             show-dapps-button?)
                                    {:icon-name :i/dapps
                                     :on-press  #(rf/dispatch [:navigate-to
                                                               :screen/wallet.connected-dapps])})
                                  (when-not sending-collectible?
                                    {:content-type        :account-switcher
                                     :customization-color color
                                     :on-press            #(on-switcher-press switcher-type params)
                                     :emoji               emoji
                                     :type                (when watch-only? :watch-only)})]}]))
