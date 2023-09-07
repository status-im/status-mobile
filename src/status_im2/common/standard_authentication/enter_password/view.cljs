(ns status-im2.common.standard-authentication.enter-password.view
  (:require [utils.i18n :as i18n]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [utils.re-frame :as rf]
            [status-im2.common.standard-authentication.enter-password.style :as style]
            [status-im2.common.standard-authentication.password-input.view :as password-input]
            [status-im.multiaccounts.core :as multiaccounts]))

(defn view
  [{:keys [on-enter-password button-label]}]
  (let [{:keys [key-uid display-name
                customization-color]
         :as   account}                     (rf/sub [:profile/multiaccount])
        {:keys [error processing password]} (rf/sub [:profile/login])
        sign-in-enabled?                    (rf/sub [:sign-in-enabled?])
        profile-picture                     (multiaccounts/displayed-photo account)]
    [:<>
     [rn/view {:style style/enter-password-container}
      [rn/view
       [quo/text
        {:accessibility-label :sync-code-generated
         :weight              :bold
         :size                :heading-1
         :style               {:margin-bottom 4}}
        (i18n/label :t/enter-password)]
       [rn/view
        {:style style/context-tag}
        [quo/context-tag
         {:type                :default
          :blur?               true
          :profile-picture     profile-picture
          :full-name           display-name
          :customization-color customization-color
          :size                24}]]
       [password-input/view
        {:processing       processing
         :error            error
         :default-password password
         :sign-in-enabled? sign-in-enabled?}]
       [rn/view {:style style/enter-password-button}
        [quo/button
         {:size                40
          :type                :primary
          :customization-color (or customization-color :primary)
          :accessibility-label :login-button
          :icon-left           :i/reveal
          :disabled?           (or (not sign-in-enabled?) processing)
          :on-press            (fn []
                                 (rf/dispatch [:set-in [:profile/login :key-uid] key-uid])
                                 (rf/dispatch [:profile.login/verify-database-password password])
                                 (js/setTimeout #(on-enter-password password) 100))}
         button-label]]]]]))
