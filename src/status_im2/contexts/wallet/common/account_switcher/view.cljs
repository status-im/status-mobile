(ns status-im2.contexts.wallet.common.account-switcher.view
  (:require [quo.core :as quo]
            [status-im2.contexts.wallet.common.sheets.account-options.view :as account-options]
            [utils.re-frame :as rf]))

(defn view
  [{:keys [on-press accessibility-label] :or {accessibility-label :top-bar}}]
  (let [{:keys [color emoji]} (rf/sub [:wallet/current-viewing-account])
        networks              (rf/sub [:wallet/network-details])]
    [quo/page-nav
     {:icon-name           :i/close
      :background          :blur
      :on-press            on-press
      :accessibility-label accessibility-label
      :networks            networks
      :networks-on-press   #(js/alert "Pressed Networks")
      :right-side          :account-switcher
      :account-switcher    {:customization-color color
                            :on-press            #(rf/dispatch [:show-bottom-sheet
                                                                {:content      account-options/view
                                                                 :hide-handle? true}])
                            :emoji               emoji}}]))
