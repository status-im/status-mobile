(ns status-im.contexts.profile.settings.screens.password.change-password.old-password-form
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.constants :as constant]
    [status-im.contexts.profile.settings.screens.password.change-password.events]
    [status-im.contexts.profile.settings.screens.password.change-password.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]
    [utils.string :as utils.string]))

(defn view
  [{:keys [customization-color]}]
  (let [error                   (rf/sub [:settings/change-password-error])
        [password set-password] (rn/use-state "")
        on-change-password      (fn [new-value]
                                  (when error
                                    (rf/dispatch [:change-password/reset-error]))
                                  (set-password new-value))
        meet-requirements?      (and (seq password)
                                     (utils.string/at-least-n-chars? password
                                                                     constant/min-password-length))
        on-submit               (fn []
                                  (when meet-requirements?
                                    (rf/dispatch
                                     [:change-password/verify-old-password
                                      (security/mask-data password)])))]
    [:<>
     [quo/input
      {:placeholder    (i18n/label :t/change-password-old-password-placeholder)
       :label          (i18n/label :t/change-password-old-password-label)
       :on-change-text on-change-password
       :auto-focus     true
       :type           :password
       :blur?          true}]
     [rn/view
      {:style style/error-container}
      (when error
        [quo/info-message
         {:type :error
          :size :default
          :icon :i/info}
         (i18n/label :t/oops-wrong-password)])]
     [rn/view {:style style/bottom-part}
      [quo/information-box
       {:type  :error
        :style style/warning-container
        :icon  :i/info}
       (i18n/label :t/change-password-confirm-warning)]
      [rn/view {:style style/button-container}
       [quo/button
        {:disabled?           (not meet-requirements?)
         :customization-color customization-color
         :on-press            on-submit}
        (i18n/label :t/continue)]]]]))
