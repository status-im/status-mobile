(ns status-im.contexts.wallet.wallet-connect.modals.common.footer.view
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [quo.theme]
            [react-native.core :as rn]
            [status-im.common.standard-authentication.core :as standard-authentication]
            [status-im.contexts.wallet.sheets.buy-token.view :as buy-token]
            [status-im.contexts.wallet.wallet-connect.modals.common.footer.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- on-auth-success
  [password]
  (rf/dispatch [:hide-bottom-sheet])
  (rf/dispatch [:wallet-connect/respond-current-session password]))

(defn view
  [{:keys [warning-label slide-button-text error-state]} & children]
  (let [{:keys [customization-color]} (rf/sub [:wallet-connect/current-request-account-details])
        offline?                      (rf/sub [:network/offline?])
        theme                         (quo.theme/use-theme)]
    [:<>
     (when (or offline? error-state)
       [quo/alert-banner
        {:action?         (when error-state true)
         :text            (if offline?
                            (i18n/label :t/wallet-connect-no-internet-warning)
                            (i18n/label (condp = error-state
                                          :not-enough-assets-to-pay-gas-fees
                                          :t/not-enough-assets-to-pay-gas-fees

                                          :not-enough-assets
                                          :t/not-enough-assets-for-transaction)))
         :button-text     (i18n/label :t/buy-eth)
         :on-button-press #(rf/dispatch [:show-bottom-sheet
                                         {:content buy-token/view}])}])
     [rn/view {:style style/content-container}
      (into [rn/view
             {:style style/data-items-container}]
            children)
      [rn/view {:style style/auth-container}
       [standard-authentication/slide-button
        {:size                :size-48
         :track-text          slide-button-text
         :disabled?           (or offline? error-state)
         :customization-color customization-color
         :on-auth-success     on-auth-success
         :auth-button-label   (i18n/label :t/confirm)}]]
      [rn/view {:style style/warning-container}
       [quo/text
        {:size   :paragraph-2
         :style  {:color (if (= theme :dark)
                           colors/white-opa-70
                           colors/neutral-80-opa-70)}
         :weight :medium}
        warning-label]]]]))
