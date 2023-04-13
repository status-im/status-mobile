(ns status-im2.contexts.onboarding.enable-biometrics.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im2.contexts.onboarding.common.background.view :as background]
            [status-im2.contexts.onboarding.enable-biometrics.style :as style]
            [status-im2.contexts.onboarding.common.navigation-bar.view :as navigation-bar]
            [status-im.multiaccounts.biometric.core :as biometric]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn page
  [{:keys [navigation-bar-top]}]
  (let [supported-biometric (rf/sub [:supported-biometric-auth])
        bio-type-label (biometric/get-label supported-biometric)
        profile-color (:color (rf/sub [:onboarding-2/profile]))]
    [rn/view {:style style/page-container}
     [navigation-bar/navigation-bar {:top                  navigation-bar-top
                                     :disable-back-button? true}]
     [rn/view
      {:style {:padding-horizontal 20
               :flex               1}}
      [quo/text
       {:size   :heading-1
        :weight :semi-bold
        :style  {:color colors/white}} (i18n/label :t/enable-biometrics)]
      [quo/text
       {:size  :paragraph-1
        :style {:color      colors/white
                :margin-top 8}}
       (i18n/label :t/use-biometrics)]
      ;; TODO(@briansztamfater): Replace view with image view with the real illustration,
      ;; https://github.com/status-im/status-mobile/issues/15445
      [rn/view {:style style/image-container}
       [quo/text {:size :paragraph-1}
        "Illustration here"]]
      [rn/view {:style {:margin-bottom 55}}
       [quo/button
        {:on-press                  #(rf/dispatch [:onboarding-2/enable-biometrics])
         :before                    :i/face-id
         :override-background-color (colors/custom-color profile-color 50)}
        (i18n/label :t/biometric-enable-button {:bio-type-label bio-type-label})]
       [quo/button
        {:on-press                  #(rf/dispatch [:onboarding-2/create-account-and-login])
         :override-background-color colors/white-opa-5
         :style                     {:margin-top 12}}
        (i18n/label :t/maybe-later)]]]]))

(defn enable-biometrics []
  (fn []
    [safe-area/consumer
     (fn [{:keys [top]}]
       [rn/view {:style {:flex 1}}
        [background/view true]
        [page {:navigation-bar-top top}]])]))
