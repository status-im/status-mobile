(ns status-im2.contexts.onboarding.enable-notifications.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [react-native.safe-area :as safe-area]
            [status-im.notifications.core :as notifications]
            [status-im2.contexts.onboarding.enable-notifications.style :as style]
            [status-im2.contexts.shell.jump-to.utils :as shell.utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn page-title
  []
  [quo/text-combinations
   {:container-style                 {:margin-top 12 :margin-horizontal 20}
    :title                           (i18n/label :t/intro-wizard-title6)
    :title-accessibility-label       :notifications-title
    :description                     (i18n/label :t/enable-notifications-sub-title)
    :description-accessibility-label :notifications-sub-title}])

(defn enable-notification-buttons
  [{:keys [insets]}]
  (let [profile-color (rf/sub [:onboarding-2/customization-color])]
    [rn/view {:style (style/buttons insets)}
     [quo/button
      {:on-press            (fn []
                              (shell.utils/change-selected-stack-id :communities-stack true nil)
                              (rf/dispatch [::notifications/switch true platform/ios?])
                              (rf/dispatch [:navigate-to-within-stack
                                            [:welcome :enable-notifications]]))
       :type                :primary
       :before              :i/notifications
       :accessibility-label :enable-notifications-button
       :customization-color profile-color}
      (i18n/label :t/intro-wizard-title6)]
     [quo/button
      {:on-press            (fn []
                              (shell.utils/change-selected-stack-id :communities-stack true nil)
                              (rf/dispatch [:navigate-to-within-stack
                                            [:welcome :enable-notifications]]))
       :accessibility-label :enable-notifications-later-button
       :type                :grey
       :background          :blur
       :container-style     {:margin-top 12}}
      (i18n/label :t/maybe-later)]]))

(defn enable-notifications
  []
  (let [insets (safe-area/get-insets)]
    [rn/view {:style (style/page-container insets)}
     [quo/page-nav
      {:background :blur
       :icon-name  :i/arrow-left
       :on-press   #(rf/dispatch [:navigate-back-within-stack :new-to-status])}]
     [page-title]
     [rn/view {:style style/page-illustration}
      [quo/text
       "Illustration here"]]
     [enable-notification-buttons {:insets insets}]]))
