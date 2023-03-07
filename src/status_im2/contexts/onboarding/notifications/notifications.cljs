(ns status-im2.contexts.onboarding.notifications.notifications
  (:require
   [quo2.core :as quo]
   [quo2.foundations.colors :as colors]
   [utils.i18n :as i18n]
   [utils.re-frame :as rf]
   [react-native.core :as rn]
   [react-native.platform :as platform]
   [status-im.notifications.core :as notifications]
   [status-im2.contexts.onboarding.notifications.style :as style]
   [status-im2.contexts.onboarding.common.background :as background]
   [status-im2.contexts.onboarding.common.page-nav :as page-nav]))

(defn page-title
  []
  [rn/view {:style style/title-container}
   [quo/text (merge {:style {:color colors/white}}
                    style/title-text-style) 
    (i18n/label :t/intro-wizard-title6)]
   [quo/text (merge {:style {:color colors/white}}
                    style/subtitle-text-style) 
    (i18n/label :t/enable-notifications-sub-title)]])


(defn enable-notification-buttons
  []
  [rn/view {:style style/enable-notifications-buttons}
   [quo/button
    {:on-press                  #(do (rf/dispatch [::notifications/switch true platform/ios?])
                                     (rf/dispatch [:init-root :welcome-new]))
     :type                      :primary
     :before                    :main-icon/notifications
     :accessibility-label       :enable-notifications-button
     :override-background-color (colors/custom-color :magenta 60)}
    (i18n/label :t/intro-wizard-title6)]
   [quo/button
    {:on-press                  #(rf/dispatch [:init-root :welcome-new])
     :accessibility-label       :enable-notifications-later-button
     :override-background-color colors/white-opa-5
     :style                     {:margin-top 12}}
    (i18n/label :t/maybe-later)]])

(defn views
  []
  [:<>
   [rn/view {:style style/blur-screen-container}
    [background/view true]]
   [rn/view {:style style/notifications-container}
    [page-nav/navigate-back]
    [page-title]
    [rn/view {:style style/illustration}
     [quo/text
      style/subtitle-text-style
      "[Illustration here]"]]
    [enable-notification-buttons]]])