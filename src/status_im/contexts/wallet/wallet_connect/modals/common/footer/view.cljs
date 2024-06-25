(ns status-im.contexts.wallet.wallet-connect.modals.common.footer.view
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im.common.standard-authentication.core :as standard-authentication]
            [status-im.contexts.wallet.wallet-connect.modals.common.footer.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- on-auth-success
  [password]
  (rf/dispatch [:hide-bottom-sheet])
  (rf/dispatch [:wallet-connect/respond-current-session password]))

(defn view
  [{:keys [warning-label slide-button-text disabed?]} & children]
  (let [{:keys [customization-color]} (rf/sub [:wallet-connect/current-request-account-details])]
    [rn/view {:style style/content-container}
     (into [rn/view
            {:style style/data-items-container}]
           children)
     [rn/view {:style style/auth-container}
      [standard-authentication/slide-button
       {:size                :size-48
        :track-text          slide-button-text
        :disabled?           disabed?
        :customization-color customization-color
        :on-auth-success     on-auth-success
        :auth-button-label   (i18n/label :t/confirm)}]]
     [rn/view {:style style/warning-container}
      [quo/text
       {:size   :paragraph-2
        :style  {:color colors/neutral-80-opa-70}
        :weight :medium}
       warning-label]]]))
