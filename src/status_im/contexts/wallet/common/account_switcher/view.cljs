(ns status-im.contexts.wallet.common.account-switcher.view
  (:require [quo.core :as quo]
            [status-im.contexts.wallet.common.sheets.account-options.view :as account-options]
            [status-im.contexts.wallet.common.sheets.network-filter.view :as network-filter]
            [status-im.contexts.wallet.common.sheets.select-account.view :as select-account]
            [status-im.feature-flags :as ff]
            [utils.re-frame :as rf]))

(defn get-bottom-sheet-args
  [switcher-type]
  (case switcher-type
    :account-options {:content      account-options/view
                      :hide-handle? true}
    :select-account  {:content select-account/view}
    nil))

(defn view
  [{:keys [type on-press accessibility-label icon-name switcher-type margin-top]
    :or   {icon-name           :i/close
           accessibility-label :top-bar
           switcher-type       :account-options}}]
  (let [{:keys [color emoji watch-only?]} (rf/sub [:wallet/current-viewing-account])
        networks                          (rf/sub [:wallet/network-details])]
    [quo/page-nav
     {:type                (or type :no-title)
      :icon-name           icon-name
      :margin-top          margin-top
      :background          :blur
      :on-press            on-press
      :accessibility-label accessibility-label
      :networks            networks
      :networks-on-press   #(ff/alert ::ff/wallet.network-filter
                                      (fn []
                                        (rf/dispatch [:show-bottom-sheet
                                                      {:content network-filter/view}])))
      :right-side          :account-switcher
      :account-switcher    {:customization-color color
                            :on-press            #(rf/dispatch [:show-bottom-sheet
                                                                (get-bottom-sheet-args switcher-type)])
                            :emoji               emoji
                            :type                (when watch-only? :watch-only)}}]))
