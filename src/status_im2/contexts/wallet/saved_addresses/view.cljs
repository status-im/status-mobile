(ns status-im2.contexts.wallet.saved-addresses.view
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

   [quo/text {} "SAVED ADDRESSES"]
   [quo/button {:on-press #(rf/dispatch [:navigate-to :wallet-saved-address])}
    "NAVIGATE TO SAVED ADDRESS"]
   [quo/divider-label]
   [quo/button {:on-press #(rf/dispatch [:navigate-back])}
    "NAVIGATE BACK"]])
