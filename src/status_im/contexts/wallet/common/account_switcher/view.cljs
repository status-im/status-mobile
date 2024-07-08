(ns status-im.contexts.wallet.common.account-switcher.view
  (:require
    [quo.core :as quo]
    [status-im.contexts.wallet.sheets.account-options.view :as account-options]
    [status-im.contexts.wallet.sheets.network-filter.view :as network-filter]
    [status-im.contexts.wallet.sheets.select-account.view :as select-account]
    [status-im.feature-flags :as ff]
    [utils.re-frame :as rf]))

(defn get-bottom-sheet-args
  [switcher-type]
  (case switcher-type
    :account-options {:content      account-options/view
                      :hide-handle? true}
    :select-account  {:content select-account/view}
    nil))

(defn- on-dapps-press
  [switcher-type]
  (rf/dispatch [:show-bottom-sheet (get-bottom-sheet-args switcher-type)]))

(defn view
  [{:keys [type on-press accessibility-label icon-name switcher-type margin-top]
    :or   {icon-name           :i/close
           accessibility-label :top-bar
           switcher-type       :account-options
           type                :no-title}}]
  (let [{:keys [color emoji watch-only?]} (rf/sub [:wallet/current-viewing-account])
        networks                          (rf/sub [:wallet/selected-network-details])]
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
      :right-side          [(when (and (ff/enabled? ::wallet.wallet-connect)
                                       (not watch-only?))
                              {:icon-name :i/dapps
                               :on-press  #(rf/dispatch [:navigate-to :screen/wallet.connected-dapps])})

                            {:content-type        :account-switcher
                             :customization-color color
                             :on-press            #(on-dapps-press switcher-type)
                             :emoji               emoji
                             :type                (when watch-only? :watch-only)}]}]))
