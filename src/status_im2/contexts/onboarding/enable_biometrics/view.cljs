(ns status-im2.contexts.onboarding.enable-biometrics.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im2.contexts.onboarding.enable-biometrics.style :as style]
            [status-im2.contexts.onboarding.common.navigation-bar.view :as navigation-bar]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [status-im2.common.resources :as resources]
            [status-im2.common.parallax.view :as parallax]
            [status-im2.common.parallax.whitelist :as whitelist]
            [status-im2.common.biometric.events :as biometric]))

(defn page-title
  []
  [quo/title
   {:container-style              {:margin-top 12}
    :title                        (i18n/label :t/enable-biometrics)
    :title-accessibility-label    :enable-biometrics-title
    :subtitle                     (i18n/label :t/use-biometrics)
    :subtitle-accessibility-label :enable-biometrics-sub-title}])

(defn enable-biometrics-buttons
  [insets]
  (let [supported-biometric-type (rf/sub [:biometric/supported-type])
        bio-type-label           (biometric/get-label-by-type supported-biometric-type)
        profile-color            (:color (rf/sub [:onboarding-2/profile]))]
    [rn/view {:style (style/buttons insets)}
     [quo/button
      {:accessibility-label :enable-biometrics-button
       :on-press            #(rf/dispatch [:onboarding-2/enable-biometrics])
       :before              :i/face-id
       :customization-color profile-color}
      (i18n/label :t/biometric-enable-button {:bio-type-label bio-type-label})]
     [quo/button
      {:accessibility-label :maybe-later-button
       :background          :blur
       :type                :grey
       :on-press            #(rf/dispatch [:onboarding-2/create-account-and-login])
       :style               {:margin-top 12}}
      (i18n/label :t/maybe-later)]]))

(defn enable-biometrics-parallax
  []
  (let [stretch (if rn/small-screen? 25 40)]
    [:<>
     [parallax/video
      {:layers  (:biometrics resources/parallax-video)
       :stretch stretch}]
     [rn/view
      [navigation-bar/navigation-bar {:disable-back-button? true}]
      [page-title]]]))

(defn enable-biometrics-simple
  []
  (let [width (:width (rn/get-window))]
    [:<>
     [rn/view {:flex 1}
      [navigation-bar/navigation-bar {:disable-back-button? true}]
      [page-title]
      [rn/view {:style {:flex 1}}
       [rn/image
        {:resize-mode :contain
         :style       (style/page-illustration width)
         :source      (resources/get-image :biometrics)}]]]]))

(defn enable-biometrics
  []
  (let [insets (safe-area/get-insets)]
    [rn/view {:style (style/page-container insets)}
     (if whitelist/whitelisted?
       [enable-biometrics-parallax]
       [enable-biometrics-simple])
     [enable-biometrics-buttons insets]]))
