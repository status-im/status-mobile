(ns status-im.contexts.wallet.swap.swap-proposal.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.sheets.slippage-settings.view :as slippage-settings]
    [status-im.contexts.wallet.swap.swap-proposal.style :as style]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [max-slippage (rf/sub [:wallet/swap-max-slippage])]
    [rn/view {:style style/container}
     [quo/button
      {:on-press #(rf/dispatch [:show-bottom-sheet
                                {:content slippage-settings/view}])}
      (str "Edit Slippage: " max-slippage "%")]
     [quo/button
      {:on-press #(rf/dispatch [:navigate-to-within-stack
                                [:screen/wallet.swap-confirmation :screen/wallet.swap-propasal]])}
      "Swap confirmation"]
     [quo/button
      {:on-press #(rf/dispatch [:open-modal :screen/wallet.swap-set-spending-cap])}
      "Set spending cap"]
     [quo/button
      {:on-press #(rf/dispatch [:navigate-to-within-stack
                                [:screen/wallet.setup-swap :screen/wallet.swap-propasal]])}
      "Setup swap"]]))
