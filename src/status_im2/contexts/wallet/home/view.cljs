(ns status-im2.contexts.wallet.home.view
  (:require [react-native.core :as rn]
            [quo2.core :as quo]
            [react-native.safe-area :as safe-area]
            [utils.re-frame :as rf]))

(defn wallet-temporary-navigation
  []
  [rn/view
   {:style {:flex            1
            :align-items     :center
            :justify-content :center}}
   [quo/text {} "TEMPORARY NAVIGATION"]
   [quo/button {:on-press #(rf/dispatch [:navigate-to :wallet-accounts])}
    "Navigate to Account"]
   [quo/button {:on-press #(rf/dispatch [:navigate-to :wallet-create-account])}
    "Create Account"]
   [quo/button {:on-press #(rf/dispatch [:navigate-to :wallet-saved-addresses])}
    "Saved Addresses"]
   [quo/button {:on-press #(rf/dispatch [:navigate-to :wallet-collectibles])}
    "Collectibles"]])

(defn view
  []
  (let [top (safe-area/get-top)]
    [rn/view
     {:style {:margin-top      top
              :flex            1
              :align-items     :center
              :justify-content :center}}
     [quo/button
      {:icon-only?      true
       :type            :grey
       :on-press        (fn [] (rf/dispatch [:show-bottom-sheet {:content wallet-temporary-navigation}]))
       :container-style {:position :absolute
                         :top      20
                         :right    20}} :i/options]
     [quo/text {} "New Wallet Home"]]))
