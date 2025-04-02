(ns status-im.contexts.wallet.wallet-connect.modals.common.footer.view
  (:require [quo.context]
            [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im.common.standard-authentication.core :as standard-authentication]
            [status-im.contexts.wallet.sheets.buy-token.view :as buy-token]
            [status-im.contexts.wallet.wallet-connect.modals.common.footer.style :as style]
            [utils.hex :as hex]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- on-auth-success
  [signatures]
  (let [signature (-> signatures
                      first
                      :signature
                      hex/prefix-hex)]
    (rf/dispatch [:hide-bottom-sheet])
    (rf/dispatch [:wallet-connect/respond signature])))

(defn- on-auth-fail
  [error]
  (rf/dispatch [:wallet-connect/on-sign-error error]))

(defn- slide-button
  [{:keys [disabled? slide-button-text]}]
  (let [{:keys [customization-color]} (rf/sub [:wallet-connect/current-request-account-details])
        address                       (rf/sub [:wallet-connect/current-request-address])
        prepared-hash                 (rf/sub [:wallet-connect/prepared-hash])]
    [rn/view {:style style/auth-container}
     [standard-authentication/slide-sign
      {:sign-payload        [{:address address
                              :message prepared-hash}]
       :size                :size-48
       :track-text          slide-button-text
       :disabled?           (and (not prepared-hash) disabled?)
       :customization-color customization-color
       :on-success          on-auth-success
       :on-fail             on-auth-fail
       :auth-button-label   (i18n/label :t/confirm)}]]))

(defn view
  [{:keys [warning-label slide-button-text error-state]} & children]
  (let [offline? (rf/sub [:network/offline?])
        theme    (quo.context/use-theme)]
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
         :button-text     (i18n/label :t/add-eth)
         :on-button-press (rn/use-callback (fn []
                                             (rf/dispatch [:centralized-metrics/track
                                                           :metric/dapp-buy-eth])
                                             (rf/dispatch [:show-bottom-sheet
                                                           {:content buy-token/view}])))}])
     [rn/view {:style style/content-container}
      (into [rn/view
             {:style style/data-items-container}]
            children)
      [slide-button
       {:disabled?         (or offline? error-state)
        :slide-button-text slide-button-text}]
      [rn/view {:style style/warning-container}
       [quo/text
        {:size   :paragraph-2
         :style  {:color (if (= theme :dark)
                           colors/white-opa-70
                           colors/neutral-80-opa-70)}
         :weight :medium}
        warning-label]]]]))
