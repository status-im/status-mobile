(ns status-im2.contexts.wallet.account.view
  (:require [react-native.core :as rn]
            [quo2.core :as quo]
            [react-native.safe-area :as safe-area]
            [status-im2.contexts.wallet.account.temp :as temp]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [top          (safe-area/get-top)]
  [rn/view
   {:style {:flex            1
            :margin-top top}}
   [quo/page-nav
    {:align-mid?            true
                  :mid-section           {:type :text-only :main-text ""}
                  :left-section          {:type     :grey
                                          :icon     :i/close
                                          :on-press #(rf/dispatch [:navigate-back])}
                  :right-section-buttons [{:type     :grey
                                           :label    "[WIP]"
                                           :on-press #(rf/dispatch [:open-modal :how-to-pair])}]}]
   [quo/account-overview temp/account-overview-state]
   [quo/wallet-graph {:time-frame :empty}]]))
