(ns status-im2.contexts.onboarding.enable-notifications.view
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.safe-area :as safe-area]
    [status-im.notifications.core :as notifications]
    [status-im2.contexts.onboarding.common.background.view :as background]
    [status-im2.contexts.onboarding.enable-notifications.style :as style]
    [status-im2.contexts.shell.animation :as shell.animation]))

(defn navigation-bar
  []
  [quo/page-nav
   (merge {:horizontal-description? false
           :one-icon-align-left?    true
           :align-mid?              false
           :page-nav-color          :transparent
           :left-section            {:icon                  :i/arrow-left
                                     :icon-background-color colors/white-opa-5
                                     :icon-override-theme   :dark
                                     :type                  :shell
                                     :on-press              #()}})])

(defn page-title
  []
  [quo/title
   {:title                        (i18n/label :t/intro-wizard-title6)
    :title-accessibility-label    :notifications-title
    :subtitle                     (i18n/label :t/enable-notifications-sub-title)
    :subtitle-accessibility-label :notifications-sub-title}])

(defn enable-notification-buttons
  [{:keys [insets]}]
  (let [profile-color (:color (rf/sub [:onboarding-2/profile]))]
    [rn/view {:style (style/buttons insets)}
     [quo/button
      {:on-press                  (fn []
                                    (shell.animation/change-selected-stack-id :communities-stack true)
                                  (rf/dispatch [::notifications/switch true platform/ios?])
                                    (rf/dispatch [:init-root :welcome]))
       :type                      :primary
       :before                    :i/notifications
       :accessibility-label       :enable-notifications-button
       :override-background-color (colors/custom-color profile-color 60)}
      (i18n/label :t/intro-wizard-title6)]
     [quo/button
      {:on-press                  (fn []
                                  (shell.animation/change-selected-stack-id :communities-stack true)
                                  (rf/dispatch [:init-root :welcome]))
       :accessibility-label       :enable-notifications-later-button
       :override-background-color colors/white-opa-5
       :style                     {:margin-top 12}}
      (i18n/label :t/maybe-later)]]))

(defn enable-notifications
  []
  [safe-area/consumer
   (fn [insets]
     [rn/view {:style (style/page-container insets)}
      [background/view true]
      [navigation-bar]
      [page-title]
      [rn/view {:style style/page-illustration}
       [quo/text
        "Illustration here"]]
      [enable-notification-buttons {:insets insets}]])])
