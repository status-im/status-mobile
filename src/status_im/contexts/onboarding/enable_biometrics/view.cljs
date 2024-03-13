(ns status-im.contexts.onboarding.enable-biometrics.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.biometric.utils :as biometric]
    [status-im.common.parallax.blacklist :as blacklist]
    [status-im.common.parallax.view :as parallax]
    [status-im.common.resources :as resources]
    [status-im.contexts.onboarding.enable-biometrics.style :as style]
    [status-im.navigation.state :as state]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))


(defn page-title
  []
  [quo/text-combinations
   {:container-style                 style/title-container
    :title                           (i18n/label :t/enable-biometrics)
    :title-accessibility-label       :enable-biometrics-title
    :description                     (i18n/label :t/use-biometrics)
    :description-accessibility-label :enable-biometrics-sub-title}])

(defn enable-biometrics-buttons
  [insets]
  (let [supported-biometric-type (rf/sub [:biometrics/supported-type])
        bio-type-label           (biometric/get-label-by-type supported-biometric-type)
        profile-color            (or (:color (rf/sub [:onboarding/profile]))
                                     (rf/sub [:profile/customization-color]))
        syncing-results?         (= :syncing-results @state/root-id)]
    [rn/view {:style (style/buttons insets)}
     [quo/button
      {:size                40
       :accessibility-label :enable-biometrics-button
       :icon-left           :i/face-id
       :customization-color profile-color
       :on-press            #(rf/dispatch [:onboarding/enable-biometrics])}
      (i18n/label :t/biometric-enable-button {:bio-type-label bio-type-label})]
     [quo/button
      {:accessibility-label :maybe-later-button
       :background          :blur
       :type                :grey
       :on-press            #(rf/dispatch (if syncing-results?
                                            [:navigate-to-within-stack
                                             [:enable-notifications :enable-biometrics]]
                                            [:onboarding/create-account-and-login]))
       :container-style     {:margin-top 12}}
      (i18n/label :t/maybe-later)]]))

(defn enable-biometrics-parallax
  []
  (let [stretch (if rn/small-screen? 25 40)]
    [rn/view
     {:position :absolute
      :top      12}
     [parallax/video
      {:layers  (:biometrics resources/parallax-video)
       :stretch stretch}]]))

(defn enable-biometrics-simple
  []
  (let [width (:width (rn/get-window))]
    [rn/image
     {:resize-mode :contain
      :style       (style/page-illustration width)
      :source      (resources/get-image :biometrics)}]))

(defn f-enable-biometrics
  []
  (let [insets (safe-area/get-insets)]
    [rn/view {:style (style/page-container insets)}
     [page-title]
     (if blacklist/blacklisted?
       [enable-biometrics-simple]
       [enable-biometrics-parallax])
     [enable-biometrics-buttons insets]]))

(defn view
  []
  [:f> f-enable-biometrics])

