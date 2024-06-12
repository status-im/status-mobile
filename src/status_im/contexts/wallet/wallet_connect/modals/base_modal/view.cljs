(ns status-im.contexts.wallet.wallet-connect.modals.base-modal.view
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im.common.raw-data-block.view :as raw-data-block]
            [status-im.common.standard-authentication.core :as standard-authentication]
            [status-im.contexts.wallet.wallet-connect.modals.base-modal.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- close-sheet
  []
  (rf/dispatch [:navigate-back]))

(defn- on-auth-success
  [password]
  (rf/dispatch [:hide-bottom-sheet])
  (rf/dispatch [:wallet-connect/respond-current-session password]))

(defn data-block
  []
  (let [display-data (rf/sub [:wallet-connect/current-request-display-data])]
    [raw-data-block/view
     {:data           display-data
      :bottom-margin? false}]))

(defn header
  [{:keys [label dapp account]}]
  [rn/view
   {:style style/header-container}
   [quo/text
    {:size   :heading-1
     :weight :semi-bold}
    (let [{:keys [name icons]} (:peerMetadata dapp)]
      [rn/view {:style style/header-dapp-name}
       [quo/summary-tag
        {:type         :dapp
         :label        name
         :image-source (first icons)}]])
    (str " " label " ")
    (let [{:keys [emoji customization-color name]} account]
      [rn/view {:style {:padding-top 4}}
       [quo/summary-tag
        {:type                :account
         :emoji               emoji
         :label               name
         :customization-color customization-color}]])]])

(defn view
  [{:keys [header-label warning-label show-network? slide-button-text]}]
  (let [bottom                (safe-area/get-bottom)
        {:keys [customization-color]
         :as   account}       (rf/sub [:wallet-connect/current-request-account-details])
        dapp                  (rf/sub [:wallet-connect/current-request-dapp])
        network               (rf/sub [:wallet-connect/current-request-network])
        {:keys [max-fees-fiat-formatted
                error-state]} (rf/sub [:wallet-connect/current-request-transaction-information])]
    [rn/view {:style (style/container bottom)}
     [quo/gradient-cover {:customization-color customization-color}]
     [quo/page-nav
      {:icon-name           :i/close
       :background          :blur
       :on-press            close-sheet
       :accessibility-label :wallet-connect-sign-message-close}]
     [rn/view {:flex 1}
      [rn/view {:style style/data-content-container}
       [header
        {:label   header-label
         :dapp    dapp
         :account account}]
       [data-block]]
      (when error-state
        [quo/alert-banner
         {:action? false
          :text    (i18n/label (condp = error-state
                                 :not-enough-assets-to-pay-gas-fees
                                 :t/not-enough-assets-to-pay-gas-fees

                                 :not-enough-assets
                                 :t/not-enough-assets))}])
      [rn/view {:style style/content-container}
       [rn/view
        {:style style/data-items-container}
        (when show-network?
          [quo/data-item
           {:status          :default
            :card?           false
            :container-style style/data-item
            :title           (i18n/label :t/network)
            :subtitle-type   :network
            :network-image   (:source network)
            :subtitle        (:full-name network)}])
        [quo/data-item
         {:size            :small
          :status          :default
          :card?           false
          :container-style style/data-item
          :title           (i18n/label :t/max-fees)
          :subtitle        (or max-fees-fiat-formatted (i18n/label :t/no-fees))}]]
       [rn/view {:style style/auth-container}
        [standard-authentication/slide-button
         {:size                :size-48
          :track-text          slide-button-text
          :disabled?           error-state
          :customization-color customization-color
          :on-auth-success     on-auth-success
          :auth-button-label   (i18n/label :t/confirm)}]]
       [rn/view {:style style/warning-container}
        [quo/text
         {:size   :paragraph-2
          :style  {:color colors/neutral-80-opa-70}
          :weight :medium}
         warning-label]]]]]))
