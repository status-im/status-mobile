(ns status-im2.contexts.onboarding.enable-notifications.view
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [react-native.core :as rn]
    [react-native.platform :as platform]
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
  [rn/view {:style style/title-container}
   [quo/text
    {:accessibility-label :notifications-screen-title
     :weight              :semi-bold
     :size                :heading-1
     :style               {:color colors/white}}
    (i18n/label :t/intro-wizard-title6)]
   [quo/text
    {:accessibility-label :notifications-screen-sub-title
     :weight              :regular
     :size                :paragraph-1
     :style               {:color colors/white}}
    (i18n/label :t/enable-notifications-sub-title)]])

(defn enable-notification-buttons
  []
  (let [{profile-color :color} (rf/sub [:onboarding-2/profile])]
    [rn/view {:style style/enable-notifications-buttons}
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
  [rn/view {:style style/enable-notifications}
   [background/view true]
   [navigation-bar]
   [page-title]
   [rn/view {:style style/page-illustration}
    [quo/text
     "[Illustration here]"]]
   [enable-notification-buttons]])
