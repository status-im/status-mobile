(ns status-im.contexts.wallet.common.token-value.view
  (:require [quo.core :as quo]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn token-value-drawer
  [token]
  (let [token-data (first (rf/sub [:wallet/tokens-filtered (:token token)]))]
    [:<>
     [quo/action-drawer
      [[{:icon                :i/buy
         :accessibility-label :buy
         :label               (i18n/label :t/buy)
         :on-press            #(js/alert "to be implemented")
         :right-icon          :i/external}
        {:icon                :i/send
         :accessibility-label :send
         :label               (i18n/label :t/send)
         :on-press            (fn []
                                (rf/dispatch [:hide-bottom-sheet])
                                (rf/dispatch [:wallet/clean-send-data])
                                (rf/dispatch [:wallet/send-select-token {:token token-data
                                                                                :is-first? true}]))}
        {:icon                :i/receive
         :accessibility-label :receive
         :label               (i18n/label :t/receive)
         :on-press            #(js/alert "to be implemented")}
        {:icon                :i/bridge
         :accessibility-label :bridge
         :label               (i18n/label :t/bridge)
         :on-press            #(js/alert "to be implemented")}
        {:icon                :i/settings
         :accessibility-label :settings
         :label               (i18n/label :t/manage-tokens)
         :on-press            #(js/alert "to be implemented")
         :add-divider?        true}
        {:icon                :i/hide
         :accessibility-label :hide
         :label               (i18n/label :t/hide)
         :on-press            #(js/alert "to be implemented")}]]]]))

(defn view
  [item]
  (let [{:keys [watch-only?]} (rf/sub [:wallet/current-viewing-account])]
    [quo/token-value
     (cond-> item
       (not watch-only?)
       (assoc :on-long-press
              #(rf/dispatch
                [:show-bottom-sheet
                 {:content       (fn [] [token-value-drawer item])
                  :selected-item (fn [] [quo/token-value item])}])))]))
