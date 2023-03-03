(ns status-im2.contexts.onboarding.notifications.notifications
  (:require
   [quo2.core :as quo]
   [quo2.foundations.colors :as colors]
   [utils.i18n :as i18n]
   [utils.re-frame :as rf]
   [react-native.core :as rn]
   [react-native.blur :as blur]
   [react-native.platform :as platform]
   [status-im.notifications.core :as notifications]
   [status-im2.contexts.onboarding.notifications.style :as style]))

(defn page-title
  []
  [rn/view {:style style/title-container}
   [quo/text
    style/title-text-style
    (i18n/label :t/intro-wizard-title6)]
   [quo/text
    style/subtitle-text-style
    (i18n/label :t/enable-notification-sub-title)]])

(defn navigate-back
  []
  [rn/view {:margin-top  44}
   [quo/page-nav
    (merge {:horizontal-description? false
            :one-icon-align-left?    true
            :align-mid?              false
            :page-nav-color          :transparent
            :mid-section             {:type            :text-with-description
                                      :main-text       nil
                                      :description-img nil}
            :left-section {:icon                  :i/close
                           :icon-background-color (colors/theme-colors
                                                   colors/neutral-50
                                                   colors/neutral-40)
                           :on-press              #(rf/dispatch [:navigate-back])}})]])

(defn enable-notification-buttons
  []
  [rn/view {:style style/enable-notifications-buttons}
   [quo/button
    {:on-press                   #(do (rf/dispatch [::notifications/switch true platform/ios?])
                                      (rf/dispatch [:init-root :welcome]))
     :type                      :primary
     :before                    :main-icon/notifications
     :accessibility-label       :enable-notifications-button
     :override-background-color colors/danger-60}
    (i18n/label :t/intro-wizard-title6)]
   [quo/button
    {:on-press                  #()
     :accessibility-label       :enable-notifications-later-button
     :override-background-color colors/white-opa-5
     :style                     {:margin-top  12}}
    (i18n/label :t/maybe-later)]])

(defn views
  []
  [:<> {:style {:flex     1}}
   [blur/view {:style style/blur-screen-container
               :blur-amount        20
               :blur-type          :x-light
               :overlay-color      :transparent}]
   [rn/view {:style style/notifications-container}
    [navigate-back]
    [page-title]
    [rn/view {:style style/illustration}
     [quo/text
      style/subtitle-text-style
      "Illustration here"]]
    [enable-notification-buttons]]])