(ns status-im2.contexts.wallet.common.account-switcher.view
  (:require [quo.core :as quo]
            [status-im2.contexts.wallet.common.sheets.account-options.view :as account-options]
            [status-im2.contexts.wallet.common.sheets.select-account.view :as select-account]
            [utils.re-frame :as rf]))

(defn get-bottom-sheet-args
  [switcher-type]
  (case switcher-type
    :account-options {:content      account-options/view
                      :hide-handle? true}
    :select-account  {:content select-account/view}
    nil))

(defn view
  [{:keys [on-press accessibility-label icon-name switcher-type]
    :or   {icon-name           :i/close
           accessibility-label :top-bar
           switcher-type       :account-options}}]
  (let [{:keys [color emoji watch-only?]} (rf/sub [:wallet/current-viewing-account])
        networks                          (rf/sub [:wallet/network-details])]
    [quo/page-nav
     {:icon-name           icon-name
      :background          :blur
      :on-press            on-press
      :accessibility-label accessibility-label
      :networks            networks
      :networks-on-press   #(js/alert "Pressed Networks")
      :right-side          :account-switcher
      :account-switcher    {:customization-color color
                            :on-press            #(rf/dispatch [:show-bottom-sheet
                                                                (get-bottom-sheet-args switcher-type)])
                            :emoji               emoji
                            :type                (when watch-only? :watch-only)}}]))
