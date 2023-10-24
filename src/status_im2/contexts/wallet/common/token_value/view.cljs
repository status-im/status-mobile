(ns status-im2.contexts.wallet.common.token-value.view
  (:require [quo.core :as quo]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn token-value-drawer
  []
  [:<>
   [quo/action-drawer
    [[{:icon                :i/buy
       :accessibility-label :buy
       :label               (i18n/label :t/buy)
       :on-press            #(js/alert "to be implemented")
       :right-icon          :i/external}
      {:icon                :i/send
       :accessibility-label :send
       :label               (i18n/label :t/send)}
      {:icon                :i/receive
       :accessibility-label :receive
       :label               (i18n/label :t/receive)}
      {:icon                :i/bridge
       :accessibility-label :bridge
       :label               (i18n/label :t/bridge)}
      {:icon                :i/settings
       :accessibility-label :settings
       :label               (i18n/label :t/manage-tokens)
       :add-divider?        true}
      {:icon                :i/hide
       :accessibility-label :hide
       :label               (i18n/label :t/hide)}]]]])

(defn view
  [item]
  [quo/token-value
   (merge item
          {:on-long-press
           #(rf/dispatch
             [:show-bottom-sheet
              {:content       token-value-drawer
               :selected-item (fn [] [quo/token-value item])}])})])
