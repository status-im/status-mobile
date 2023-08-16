(ns status-im2.contexts.wallet.account.view
  (:require [react-native.core :as rn]
            [quo2.core :as quo]
            [utils.re-frame :as rf]))

(defn view
  []
  [rn/view
   {:style {:flex            1
            :align-items     :center
            :justify-content :center}}
   [quo/text {} "ACCOUNTS PAGE"]
   [quo/divider-label]
   [quo/button {:on-press #(rf/dispatch [:navigate-back])}
    "NAVIGATE BACK"]])
