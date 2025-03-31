(ns status-im.contexts.wallet.common.transaction-settings.nonce.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.controlled-input.utils :as controlled-input]
    [status-im.contexts.wallet.common.transaction-settings.view :as transaction-settings]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [current                       (rf/sub [:wallet/tx-settings-nonce])
        suggested-nonce               (rf/sub [:wallet/tx-settings-suggested-nonce])
        last-tx-nonce                 (dec suggested-nonce)
        [input-state set-input-state] (rn/use-state (controlled-input/set-value-numeric
                                                     controlled-input/init-state
                                                     current))
        input-value                   (controlled-input/input-value input-state)
        warning?                      (> (controlled-input/value-numeric input-state)
                                         suggested-nonce)]
    [rn/view
     {:style {:flex 1}}
     [quo/page-nav
      {:type       :title
       :title      (i18n/label :t/nonce)
       :text-align :center
       :right-side [{:icon-name :i/info
                     :on-press  #(rf/dispatch [:show-bottom-sheet
                                               {:content (fn [] [transaction-settings/info-sheet
                                                                 (i18n/label :t/nonce)
                                                                 (i18n/label :t/about-nonce)])}])}]
       :icon-name  :i/arrow-left
       :on-press   (fn []
                     (rf/dispatch [:navigate-back])
                     (rf/dispatch [:show-bottom-sheet
                                   {:content transaction-settings/custom-settings-sheet}]))}]
     [rn/view
      {:style {:padding-horizontal 20
               :gap                8}}
      [quo/input
       {:type          :text
        :label         (i18n/label :t/type-nonce)
        :label-right   (i18n/label :t/last-transaction-is
                                   {:number (if (< last-tx-nonce 0)
                                              "-"
                                              last-tx-nonce)})
        :editable      false
        :default-value input-value
        :clearable?    true
        :error?        warning?
        :on-clear      (fn []
                         (set-input-state controlled-input/delete-all))}]
      (when warning?
        [quo/text
         {:style  {:color colors/warning-50}
          :weight :regular
          :size   :paragraph-2}
         (i18n/label :t/nonce-higher {:number suggested-nonce})])]
     [rn/view {:style {:flex 1}}]
     [quo/bottom-actions
      {:actions          :one-action
       :button-one-label (i18n/label :t/save-changes)
       :button-one-props {:disabled? (controlled-input/empty-value? input-state)
                          :on-press  (fn []
                                       (rf/dispatch [:wallet/set-nonce
                                                     (controlled-input/value-numeric input-state)])
                                       (rf/dispatch [:navigate-back])
                                       (rf/dispatch [:show-bottom-sheet
                                                     {:content
                                                      transaction-settings/custom-settings-sheet}]))}}]
     [quo/numbered-keyboard
      {:container-style      {:padding-bottom (safe-area/get-bottom)}
       :left-action          :none
       :delete-key?          true
       :on-press             (fn [c]
                               (set-input-state #(controlled-input/add-character % c)))
       :on-delete            (fn []
                               (set-input-state controlled-input/delete-last))
       :on-long-press-delete (fn []
                               (set-input-state controlled-input/delete-all))}]]))
