(ns status-im.common.standard-authentication.enter-password.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.common.standard-authentication.enter-password.style :as style]
    [status-im.common.standard-authentication.password-input.view :as password-input]
    [status-im.contexts.profile.utils :as profile.utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(defn view
  []
  (let [password-value (reagent/atom nil)]
    (fn [{:keys [on-enter-password on-press-biometrics button-label button-icon-left]}]
      (let [{:keys [key-uid customization-color]
             :as   profile}                     (rf/sub [:profile/profile-with-image])
            {:keys [error processing password]} (rf/sub [:profile/login])
            sign-in-enabled?                    (rf/sub [:sign-in-enabled?])
            on-change-password                  (fn [value]
                                                  (reset! password-value value)
                                                  (when (not= value (security/safe-unmask-data password))
                                                    (rf/dispatch [:set-in [:profile/login :password]
                                                                  (security/mask-data value)])
                                                    (rf/dispatch [:set-in [:profile/login :error] ""])))]
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
              :profile-picture     (profile.utils/photo profile)
              :full-name           (profile.utils/displayed-name profile)
              :customization-color customization-color
              :size                24}]]
           [password-input/view
            {:on-press-biometrics on-press-biometrics
             :processing          processing
             :error               error
             :password            @password-value
             :on-change-password  on-change-password
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
            button-label]]]]))))
