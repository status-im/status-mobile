(ns status-im.common.standard-authentication.forgot-password-doc.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.common.standard-authentication.forgot-password-doc.style :as style]
    [utils.i18n :as i18n]))

(defn view
  [{:keys [shell?]}]
  [quo/documentation-drawers
   {:title  (i18n/label :t/forgot-your-password-info-title)
    :shell? shell?}
   [rn/view {:style style/container}
    [quo/text {:size :paragraph-2} (i18n/label :t/forgot-your-password-info-description)]

    [rn/view {:style style/step-container}
     [quo/step {:in-blur-view? shell?} 1]
     [rn/view
      {:style style/step-content}
      [quo/text {:size :paragraph-2 :weight :semi-bold}
       (i18n/label :t/forgot-your-password-info-remove-profile)]
      [rn/view {:style style/step-description}
       [quo/text {:size :paragraph-2}
        (i18n/label :t/forgot-your-password-info-remove-profile-description)]]]]

    [rn/view {:style style/step-container}
     [quo/step {:in-blur-view? shell?} 2]
     [rn/view {:style style/step-content}
      [rn/view {:style style/step-title}
       [quo/text {:size :paragraph-2 :weight :semi-bold}
        (i18n/label :t/forgot-your-password-info-recover-profile)]]
      [rn/view {:style style/step-description}
       [quo/text {:size :paragraph-2}
        (i18n/label :t/forgot-your-password-info-recover-profile-description)]]]]

    [rn/view {:style style/step-container}
     [quo/step {:in-blur-view? shell?} 3]
     [rn/view {:style style/step-content}
      [quo/text {:size :paragraph-2 :weight :semi-bold}
       (i18n/label :t/forgot-your-password-info-create-new-password)]
      [rn/view {:style style/step-description}
       [quo/text {:size :paragraph-2}
        (i18n/label :t/forgot-your-password-info-create-new-password-description)]]]]]])
