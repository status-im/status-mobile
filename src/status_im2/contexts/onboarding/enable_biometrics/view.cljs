(ns status-im2.contexts.onboarding.enable-biometrics.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im2.contexts.onboarding.enable-biometrics.style :as style]
            [status-im2.contexts.onboarding.common.navigation-bar.view :as navigation-bar]
            [status-im.multiaccounts.biometric.core :as biometric]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.common.parallax.view :as parallax]))

(defonce motion-permission-granted (reagent/atom false))

(defn page-title
  []
  [quo/title
   {:title                        (i18n/label :t/enable-biometrics)
    :title-accessibility-label    :enable-biometrics-title
    :subtitle                     (i18n/label :t/use-biometrics)
    :subtitle-accessibility-label :enable-biometrics-sub-title}])

(defn enable-biometrics-buttons
  [{:keys [insets]}]
  (let [supported-biometric (rf/sub [:supported-biometric-auth])
        bio-type-label      (biometric/get-label supported-biometric)
        profile-color       (:color (rf/sub [:onboarding-2/profile]))]
    [rn/view {:style (style/buttons insets)}
     [quo/button
      {:accessibility-label       :enable-biometrics-button
       :on-press                  #(rf/dispatch [:onboarding-2/enable-biometrics])
       :before                    :i/face-id
       :override-background-color (colors/custom-color profile-color 50)}
      (i18n/label :t/biometric-enable-button {:bio-type-label bio-type-label})]
     [quo/button
      {:accessibility-label       :maybe-later-button
       :on-press                  #(rf/dispatch [:onboarding-2/create-account-and-login])
       :override-background-color colors/white-opa-5
       :style                     {:margin-top 12}}
      (i18n/label :t/maybe-later)]]))

(defn enable-biometrics
  []
  (let [insets                    (safe-area/get-insets)
        request-motion-permission (fn []
                                    (rf/dispatch
                                     [:request-permissions
                                      {:permissions [:motion]
                                       :on-allowed  #(reset! motion-permission-granted true)
                                       :on-denied   #(rf/dispatch
                                                      [:toasts/upsert
                                                       {:icon           :i/info
                                                        :icon-color     colors/danger-50
                                                        :override-theme :light
                                                        :text           "motion denied"}])}]))]
    [rn/view {:style (style/page-container insets)}
     [navigation-bar/navigation-bar {:disable-back-button? true}]
     [page-title]
     [rn/view {:style style/page-illustration}
      (if @motion-permission-granted
        [parallax/video
         {:layers (resources/get-parallax-video :biometrics1)}]
        [quo/button
         {:before              :i/camera
          :type                :primary
          :size                32
          :accessibility-label :request-motion-permission
          :override-theme      :dark
          :on-press            request-motion-permission}
         "test motion"])]
     [enable-biometrics-buttons {:insets insets}]]))
