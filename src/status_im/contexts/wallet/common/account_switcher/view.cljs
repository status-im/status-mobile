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
        networks                          (rf/sub [:wallet/selected-network-details])
        sending-collectible?              (rf/sub [:wallet/sending-collectible?])]
    [quo/page-nav
     {:type                type
      :icon-name           icon-name
      :margin-top          margin-top
      :background          :blur
      :on-press            on-press
      :accessibility-label accessibility-label
      :networks            networks
      :align-center?       true
      :networks-on-press   #(rf/dispatch [:show-bottom-sheet {:content network-filter/view}])
      :right-side          [(when (and (ff/enabled? ::ff/wallet.wallet-connect)
                                       (not watch-only?)
                                       show-dapps-button?)
                              {:icon-name :i/dapps
                               :on-press  #(rf/dispatch [:navigate-to :screen/wallet.connected-dapps])})
                            (when-not sending-collectible?
                              {:content-type        :account-switcher
                               :customization-color color
                               :on-press            #(on-switcher-press switcher-type params)
                               :emoji               emoji
                               :type                (when watch-only? :watch-only)})]}]))
