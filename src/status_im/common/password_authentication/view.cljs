(ns status-im.common.password-authentication.view
  (:require
    [native-module.core :as native-module]
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.profile.utils :as profile.utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [entered-password (reagent/atom "")]
    (fn []
      (let [profile                (rf/sub [:profile/profile-with-image])
            {:keys [error button]} (rf/sub [:password-authentication])]
        [rn/view {:padding-horizontal 20}
         [quo/text {:size :heading-1 :weight :semi-bold}
          (i18n/label :t/enter-password)]
         [rn/view {:style {:margin-top 8 :margin-bottom 20}}
          [quo/context-tag
           {:size            24
            :full-name       (profile.utils/displayed-name profile)
            :profile-picture (profile.utils/photo profile)}]]
         [quo/input
          {:type           :password
           :label          (i18n/label :t/profile-password)
           :placeholder    (i18n/label :t/type-your-password)
           :error?         (when (not-empty error) error)
           :auto-focus     true
           :on-change-text #(reset! entered-password %)}]
         (when (not-empty error)
           [quo/info-message
            {:status          :error
             :size            :default
             :icon            :i/info
             :container-style {:margin-top 8}}
            (i18n/label :t/oops-wrong-password)])
         [quo/button
          {:container-style {:margin-bottom 12 :margin-top 40}
           :on-press        #((:on-press button) (native-module/sha3 @entered-password))}
          (:label button)]]))))
