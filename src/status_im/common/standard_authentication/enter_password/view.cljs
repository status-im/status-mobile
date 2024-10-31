(ns status-im.common.standard-authentication.enter-password.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.common.standard-authentication.core :as standard-authentication]
    [status-im.common.standard-authentication.enter-password.style :as style]
    [status-im.contexts.profile.utils :as profile.utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [on-enter-password on-press-biometrics button-label button-icon-left]}]
  (let [{:keys [key-uid customization-color] :as profile} (rf/sub [:profile/profile-with-image])
        {:keys [error processing password]}               (rf/sub [:profile/login])
        sign-in-enabled?                                  (rf/sub [:sign-in-enabled?])]
    [:<>
     [rn/view {:style style/enter-password-container}
      [rn/view
       [quo/text
        {:accessibility-label :sync-code-generated
         :weight              :semi-bold
         :size                :heading-2
         :style               {:margin-bottom 4}}
        (i18n/label :t/enter-password)]
       [rn/view
        {:style style/context-tag}
        [quo/context-tag
         {:type                :default
          :blur?               true
          :profile-picture     (profile.utils/photo profile)
          :full-name           (profile.utils/displayed-name profile)
          :customization-color customization-color
          :size                24}]]
       [standard-authentication/password-input
        {:on-press-biometrics on-press-biometrics
         :blur?               true
         :processing          processing
         :error               {:error?        (not (string/blank? error))
                               :error-message error}
         :default-password    password
         :sign-in-enabled?    sign-in-enabled?}]
       [quo/button
        {:size                40
         :container-style     style/enter-password-button
         :type                :primary
         :customization-color (or customization-color :primary)
         :accessibility-label :login-button
         :icon-left           button-icon-left
         :disabled?           (or (not sign-in-enabled?) processing)
         :on-press            (fn []
                                (rf/dispatch [:set-in [:profile/login :key-uid] key-uid])
                                (rf/dispatch [:profile.login/verify-database-password password
                                              #(on-enter-password password)]))}
        button-label]]]]))
