(ns status-im2.contexts.onboarding.enable-biometrics.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im2.common.biometric.events :as biometric]
            [status-im2.common.parallax.view :as parallax]
            [status-im2.common.parallax.whitelist :as whitelist]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.onboarding.enable-biometrics.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn page-title
  []
  [quo/text-combinations
   {:container-style                 {:margin-top 12 :margin-horizontal 20}
    :title                           (i18n/label :t/enable-biometrics)
    :title-accessibility-label       :enable-biometrics-title
    :description                     (i18n/label :t/use-biometrics)
    :description-accessibility-label :enable-biometrics-sub-title}])

(defn enable-biometrics-buttons
  [insets]
  (let [supported-biometric-type (rf/sub [:biometric/supported-type])
        bio-type-label           (biometric/get-label-by-type supported-biometric-type)
        profile-color            (:color (rf/sub [:onboarding-2/profile]))]
    [rn/view {:style (style/buttons insets)}
     [quo/button
      {:accessibility-label :enable-biometrics-button
       :on-press            #(rf/dispatch [:onboarding-2/enable-biometrics])
       :icon-left           :i/face-id
       :customization-color profile-color}
      (i18n/label :t/biometric-enable-button {:bio-type-label bio-type-label})]
     [quo/button
      {:accessibility-label :maybe-later-button
       :background          :blur
       :type                :grey
       :on-press            #(rf/dispatch [:onboarding-2/create-account-and-login])
       :container-style     {:margin-top 12}}
      (i18n/label :t/maybe-later)]]))

(defn enable-biometrics-parallax
  []
  (let [stretch (if rn/small-screen? 25 40)]
    [parallax/video
     {:layers  (:biometrics resources/parallax-video)
      :stretch stretch}]))

(defn enable-biometrics-simple
  []
  (let [width (:width (rn/get-window))]
    [rn/image
     {:resize-mode :contain
      :style       (style/page-illustration width)
      :source      (resources/get-image :biometrics)}]))

(defn enable-biometrics
  []
  (let [insets (safe-area/get-insets)]
    [rn/view {:style (style/page-container insets)}
     [page-title]
     (if whitelist/whitelisted?
       [enable-biometrics-parallax]
       [enable-biometrics-simple])
     [enable-biometrics-buttons insets]]))
