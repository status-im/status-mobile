(ns status-im.contexts.profile.settings.screens.password.change-password.loading
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [insets   (safe-area/get-insets)
        loading? (rf/sub [:settings/change-password-loading])]
    [quo/overlay {:type :shell}
     [rn/view
      {:style {:flex            1
               :justify-content :space-between
               :padding-top     (:top insets)
               :padding-bottom  (:bottom insets)}}

      [quo/page-nav]
      [quo/page-top
       (cond-> {:description :text}
         loading?       (assoc
                         :title            (i18n/label :t/change-password-loading-header)
                         :description-text (i18n/label :t/change-password-loading-description))
         (not loading?) (assoc
                         :title            (i18n/label :t/change-password-done-header)
                         :description-text (i18n/label :t/change-password-done-description)))]
      [rn/view
       {:style {:flex               1
                :padding-horizontal 20}}
       (when loading?
         [quo/information-box
          {:type  :error
           :style {:margin-top 12}
           :icon  :i/info}
          (i18n/label :t/change-password-loading-warning)])]
      (when-not loading?
        [quo/logout-button
         {:container-style {:margin-horizontal 20
                            :margin-vertical   12}
          :on-press        (fn []
                             (rf/dispatch [:multiaccounts.logout.ui/logout-pressed])

                             (rf/dispatch [:password-settings/reset-change-password]))}])]]))
