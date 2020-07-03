(ns status-im.ui.screens.notifications-settings.views
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.react-native.resources :as resources]
            [quo.core :as quo]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]))

(defn notifications-settings []
  [react/view {:flex 1 :background-color colors/white
               :align-items :center :padding-bottom 16}
   [react/text {:style {:margin-top 72 :margin-bottom 16
                        :typography :header}}
    (i18n/label :t/private-notifications)]
   [react/text {:style {:color colors/gray :text-align :center :margin-horizontal 24}}
    (i18n/label :t/private-notifications-descr)]
   [react/view {:flex 1 :align-items :center :justify-content :center}
    [react/image {:source (resources/get-image :notifications)
                  :style  {:width  118
                           :height 118}}]]
   [quo/button {:on-press #(do (re-frame/dispatch
                                [:multiaccounts.ui/notifications-switched true])
                               (re-frame/dispatch [:navigate-to :welcome]))
                :accessibility-label :enable-notifications}
    (i18n/label :t/intro-wizard-title6)]
   [quo/button {:type     :secondary :style {:margin-top 8}
                :accessibility-label :maybe-later
                :on-press #(re-frame/dispatch [:navigate-to :welcome])}
    (i18n/label :t/maybe-later)]])
