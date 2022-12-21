(ns status-im.ui.screens.onboarding.notifications.views
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [quo.platform :as platform]
            [re-frame.core :as re-frame]
            [i18n.i18n :as i18n]
            [status-im.notifications.core :as notifications]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.react :as react]))

(defn notifications-onboarding
  []
  [react/view
   {:flex             1
    :background-color colors/white
    :align-items      :center
    :padding-bottom   16}
   [react/text
    {:style {:margin-top    72
             :margin-bottom 16
             :typography    :header}}
    (i18n/label :t/private-notifications)]
   [react/text {:style {:color colors/gray :text-align :center :margin-horizontal 24}}
    (i18n/label :t/private-notifications-descr)]
   [react/view {:flex 1 :align-items :center :justify-content :center}
    [react/image
     {:source (resources/get-image :notifications)
      :style  {:width  118
               :height 118}}]]
   [quo/button
    {:on-press            #(do (re-frame/dispatch [::notifications/switch true platform/ios?])
                               (re-frame/dispatch [:init-root :welcome]))
     :accessibility-label :enable-notifications}
    (i18n/label :t/intro-wizard-title6)]
   [quo/button
    {:type                :secondary
     :style               {:margin-top 8}
     :accessibility-label :maybe-later
     :on-press            #(re-frame/dispatch [:init-root :welcome])}
    (i18n/label :t/maybe-later)]])
