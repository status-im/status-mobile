(ns status-im2.common.password-authentication.view
  (:require
    [native-module.core :as native-module]
    [quo2.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.multiaccounts.core :as multiaccounts]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [entered-password (reagent/atom "")]
    (fn []
      (let [account                (rf/sub [:profile/multiaccount])
            {:keys [error button]} (rf/sub [:password-authentication])]
        [rn/view {:padding-horizontal 20}
         [quo/text {:size :heading-1 :weight :semi-bold}
          (i18n/label :t/enter-password)]
         [rn/view {:style {:margin-top 8 :margin-bottom 20}}
          [quo/context-tag
           {:size            24
            :profile-picture (multiaccounts/displayed-photo account)
            :full-name       (multiaccounts/displayed-name account)}]]
         [quo/input
          {:type           :password
           :label          (i18n/label :t/profile-password)
           :placeholder    (i18n/label :t/type-your-password)
           :error?         (when (not-empty error) error)
           :auto-focus     true
           :on-change-text #(reset! entered-password %)}]
         (when (not-empty error)
           [quo/info-message
            {:type  :error
             :size  :default
             :icon  :i/info
             :style {:margin-top 8}}
            (i18n/label :t/oops-wrong-password)])
         [quo/button
          {:container-style {:margin-bottom 12 :margin-top 40}
           :on-press        #((:on-press button) (native-module/sha3 @entered-password))}
          (:label button)]]))))
