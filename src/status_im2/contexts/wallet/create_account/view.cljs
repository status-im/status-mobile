(ns status-im2.contexts.wallet.create-account.view
  (:require [react-native.core :as rn]
            [quo2.core :as quo]
            [re-frame.core :as rf]))

(defn view
  []
  [rn/view
   {:style {:flex            1
            :align-items     :center
            :justify-content :center}}

   [quo/text {} "CREATE ACCOUNT"]
   [quo/divider-label]
   [quo/button {:on-press #(rf/dispatch [:navigate-back])}
    "NAVIGATE BACK"]])
