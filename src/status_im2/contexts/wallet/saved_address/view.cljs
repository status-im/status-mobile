(ns status-im2.contexts.wallet.saved-address.view
  (:require
    [quo2.core :as quo]
    [re-frame.core :as rf]
    [react-native.core :as rn]))

(defn view
  []
  [rn/view
   {:style {:flex            1
            :align-items     :center
            :justify-content :center}}

   [quo/text {} "SAVED ADDRESS"]
   [quo/button {:on-press #(rf/dispatch [:navigate-back])}
    "NAVIGATE BACK"]])
