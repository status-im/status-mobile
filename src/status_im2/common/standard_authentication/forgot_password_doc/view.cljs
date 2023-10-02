(ns status-im2.common.standard-authentication.forgot-password-doc.view
  (:require [status-im2.common.standard-authentication.forgot-password-doc.style :as style]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [utils.i18n :as i18n]))

(defn view
  [{:keys [shell?]}]
  [quo/documentation-drawers
   {:title  (i18n/label :t/forgot-your-password-info-title)
    :shell? shell?}
   [rn/view
    {:style style/container}
    [quo/text {:size :paragraph-2} (i18n/label :t/forgot-your-password-info-description)]

    [rn/view {:style style/step-container}
     [quo/step {:in-blur-view? shell?} 1]
     [rn/view
      {:style style/step-content}
      [quo/text {:size :paragraph-2 :weight :semi-bold}
       (i18n/label :t/forgot-your-password-info-remove-app)]
      [quo/text {:size :paragraph-2} (i18n/label :t/forgot-your-password-info-remove-app-description)]]]

    [rn/view {:style style/step-container}
     [quo/step {:in-blur-view? shell?} 2]
     [rn/view
      {:style style/step-content}
      [quo/text {:size :paragraph-2 :weight :semi-bold}
       (i18n/label :t/forgot-your-password-info-reinstall-app)]
      [quo/text {:size :paragraph-2}
       (i18n/label :t/forgot-your-password-info-reinstall-app-description)]]]

    [rn/view {:style style/step-container}
     [quo/step {:in-blur-view? shell?} 3]
     [rn/view
      {:style style/step-content}
      [rn/view
       {:style style/step-title}
       [quo/text {:size :paragraph-2} (str (i18n/label :t/sign-up) " ")]
       [quo/text {:size :paragraph-2 :weight :semi-bold}
        (i18n/label :t/forgot-your-password-info-signup-with-key)]]
      [quo/text {:size :paragraph-2}
       (i18n/label :t/forgot-your-password-info-signup-with-key-description)]]]

    [rn/view {:style style/step-container}
     [quo/step {:in-blur-view? shell?} 4]
     [rn/view
      {:style style/step-content}
      [quo/text {:size :paragraph-2 :weight :semi-bold}
       (i18n/label :t/forgot-your-password-info-create-new-password)]
      [quo/text {:size :paragraph-2}
       (i18n/label :t/forgot-your-password-info-create-new-password-description)]]]]])
