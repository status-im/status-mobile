(ns status-im.contexts.profile.settings.screens.password.change-password.loading
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

;; NOTE: The duration of the password change depends on the device hardware, so to avoid
;; quick changes in UI when mounted, we're waiting for a bit.
(def ^:private minimum-loading-time 3000)

(defn view
  []
  (let [insets                             (safe-area/get-insets)
        [minimum-loading-timeout-done?
         set-minimum-loading-timeout-done] (rn/use-state false)
        loading?                           (rf/sub [:settings/change-password-loading])
        done?                              (and (not loading?) minimum-loading-timeout-done?)]
    (rn/use-mount (fn []
                    (js/setTimeout
                     #(set-minimum-loading-timeout-done true)
                     minimum-loading-time)))
    [quo/overlay {:type :shell}
     [rn/view
      {:key   :change-password-loading
       :style {:flex            1
               :justify-content :space-between
               :padding-top     (:top insets)
               :padding-bottom  (:bottom insets)}}

      [quo/page-nav]
      [quo/page-top
       (cond-> {:description :text}
         (not done?) (assoc
                      :title            (i18n/label :t/change-password-loading-header)
                      :description-text (i18n/label :t/change-password-loading-description))
         done?       (assoc
                      :title            (i18n/label :t/change-password-done-header)
                      :description-text (i18n/label :t/change-password-done-description)))]
      [rn/view
       {:style {:flex               1
                :padding-horizontal 20}}
       (when-not done?
         [quo/information-box
          {:type  :error
           :style {:margin-top 12}
           :icon  :i/info}
          (i18n/label :t/change-password-loading-warning)])]
      (when done?
        [quo/logout-button
         {:container-style {:margin-horizontal 20
                            :margin-vertical   12}
          :on-press        (fn []
                             (rf/dispatch [:multiaccounts.logout.ui/logout-pressed])

                             (rf/dispatch [:password-settings/reset-change-password]))}])]]))
