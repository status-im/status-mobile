(ns status-im2.common.standard-authentication.enter-password.view
  (:require [utils.i18n :as i18n]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [utils.re-frame :as rf]
            [status-im2.common.standard-authentication.auth-utils :as auth-utils]
            [utils.security.core :as security]
            [status-im2.common.standard-authentication.enter-password.style :as style]
            [quo2.theme :as quo.theme]))



(defn- view-internal
  [{:keys [theme on-enter-password button-label]}]
  (let [entered-password                    (atom "")
        {:keys [key-uid display-name
                customization-color]}       (rf/sub [:profile/multiaccount])
        {:keys [error processing password]} (rf/sub [:profile/login])
        sign-in-enabled?                    (rf/sub [:sign-in-enabled?])
        profile-picture                     (rf/sub [:profile/login-profiles-picture key-uid])
        error                               (auth-utils/get-error-message error)]
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
        [quo/context-tag {} profile-picture display-name]]
       [rn/view
        [quo/input
         {:type                :password
          :blur?               true
          :placeholder         (i18n/label :t/type-your-password)
          :auto-focus          true
          :show-cancel         false
          :error?              (seq error)
          :accessibility-label :password-input
          :label               (i18n/label :t/profile-password)
          :on-change-text      (fn [password]
                                 (rf/dispatch [:set-in [:profile/login :password]
                                               (security/mask-data password)])
                                 (reset! entered-password password)
                                 (rf/dispatch [:set-in [:profile/login :error] ""]))}]]
       (when (seq error)
         [rn/view {:style style/error-message}
          [quo/info-message
           {:type :error
            :size :default
            :icon :i/info}
           error]
          [rn/pressable
           {:active-opacity 1
            :on-press       #(reset! entered-password %)}
           [quo/text
            {:style                 {:text-decoration-line :underline
                                     :color                (colors/theme-colors
                                                            (colors/custom-color :danger 50)
                                                            (colors/custom-color :danger 60)
                                                            theme)}
             :size                  :paragraph-2
             :suppress-highlighting true}
            (i18n/label :t/forgot-password)]]])
       [rn/view {:style style/enter-password-button}
        [quo/button
         {:size                40
          :type                :primary
          :customization-color (or customization-color :primary)
          :accessibility-label :login-button
          :icon-left           :i/reveal
          :disabled?           (or (not sign-in-enabled?) processing)
          :on-press            #(on-enter-password password)}
         button-label]]]]]))

(def view (quo.theme/with-theme view-internal))
