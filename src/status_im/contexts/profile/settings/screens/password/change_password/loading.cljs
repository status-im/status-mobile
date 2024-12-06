(ns status-im.contexts.profile.settings.screens.password.change-password.loading
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im.contexts.profile.settings.screens.password.change-password.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

;; NOTE: The duration of the password change depends on the device hardware, so to avoid
;; quick changes in UI when mounted, we're waiting for a bit.
(def ^:private minimum-loading-time 5000)

(defn- handle-logout
  []
  (rf/dispatch [:profile.settings/ask-logout])
  (rf/dispatch [:change-password/reset]))

(defn view
  []
  (let [insets                             (safe-area/get-insets)
        [minimum-loading-timeout-done?
         set-minimum-loading-timeout-done] (rn/use-state false)
        loading?                           (rf/sub [:settings/change-password-loading])
        logging-out?                       (rf/sub [:profile/logging-out?])
        done?                              (and (not loading?) minimum-loading-timeout-done?)]
    (rn/use-mount (fn []
                    (js/setTimeout
                     #(set-minimum-loading-timeout-done true)
                     minimum-loading-time)))
    [quo/overlay {:type :shell}
     [rn/view
      {:key   :change-password-loading
       :style (style/loading-container insets)}
      [quo/page-nav]
      [quo/page-top
       {:description      :text
        :title            (if done?
                            (i18n/label :t/change-password-done-header)
                            (i18n/label :t/change-password-loading-header))
        :description-text (if done?
                            (i18n/label :t/change-password-done-description)
                            (i18n/label :t/change-password-loading-description))}]
      [rn/view {:style style/loading-content}
       (when-not done?
         [quo/information-box
          {:type  :error
           :style {:margin-top 12}
           :icon  :i/info}
          (i18n/label :t/change-password-loading-warning)])]
      (when done?
        [quo/logout-button
         {:container-style style/logout-container
          :disabled?       logging-out?
          :on-press        handle-logout}])]]))

