(ns status-im.contexts.wallet.wallet-connect.sign-message.view
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im.common.raw-data-block.view :as raw-data-block]
            [status-im.common.standard-authentication.core :as standard-authentication]
            [status-im.contexts.wallet.wallet-connect.sign-message.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- close-sheet
  []
  (rf/dispatch [:navigate-back]))

(defn- on-auth-success
  [password]
  (rf/dispatch [:hide-bottom-sheet])
  (rf/dispatch [:wallet-connect/respond-current-session password]))

;; NOTE: this will be a reusable component
(defn- header
  [{:keys [emoji customization-color name]}]
  [rn/view
   {:style {:padding-vertical 12}}
   [quo/text
    {:size   :heading-1
     :weight :semi-bold}
    [rn/view {:style {:margin-top -4}}
     [quo/summary-tag
      {:label "dapp placeholder"}]]
    (i18n/label :t/wallet-connect-sign-header)
    [rn/view {:style {:padding-top 4}}
     [quo/summary-tag
      {:type                :account
       :emoji               emoji
       :label               name
       :customization-color customization-color}]]]])

(defn data-block
  []
  (let [display-data (rf/sub [:wallet-connect/current-request-display-data])]
    [raw-data-block/view {:data display-data}]))

(defn view
  []
  (let [bottom                                   (safe-area/get-bottom)
        {:keys [name emoji customization-color]} (rf/sub
                                                  [:wallet-connect/current-request-account-details])]
    [rn/view {:style (style/container bottom)}
     [quo/gradient-cover {:customization-color customization-color}]
     [quo/page-nav
      {:icon-name           :i/close
       :background          :blur
       :on-press            close-sheet
       :accessibility-label :wallet-connect-sign-message-close}]
     [rn/view {:style style/content-container}
      [header
       {:emoji               emoji
        :customization-color customization-color
        :name                name}]
      [data-block]
      [quo/data-item
       {:size            :small
        :status          :default
        :card?           false
        :container-style style/fees-container
        :title           (i18n/label :t/max-fees)
        :subtitle        (i18n/label :t/no-fees)}]
      [rn/view {:style style/auth-container}
       [standard-authentication/slide-button
        {:size                :size-48
         :track-text          (i18n/label :t/slide-to-sign)
         :customization-color customization-color
         :on-auth-success     on-auth-success
         :auth-button-label   (i18n/label :t/confirm)}]]
      [rn/view {:style style/warning-container}
       [quo/text
        {:size   :paragraph-2
         :style  {:color colors/neutral-80-opa-70}
         :weight :medium}
        (i18n/label :t/wallet-connect-sign-warning)]]]]))
