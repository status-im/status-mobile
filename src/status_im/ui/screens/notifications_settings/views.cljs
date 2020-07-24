(ns status-im.ui.screens.notifications-settings.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [status-im.notifications.core :as notifications]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]))

(defn notifications-settings []
  (let [{:keys [remote-push-notifications-enabled
                push-notifications-from-contacts-only]}
        @(re-frame/subscribe [:multiaccount])]
    [react/view {:flex 1}
     [topbar/topbar {:title :t/notification-settings}]
     [react/scroll-view {:style                   {:flex 1}
                         :content-container-style {:padding-vertical 8}}
      [quo/list-item
       {:size                :small
        :title               (i18n/label :t/notifications)
        :accessibility-label :notifications-button
        :active              remote-push-notifications-enabled
        :on-press            #(re-frame/dispatch [::notifications/switch (not remote-push-notifications-enabled)])
        :accessory           :switch}]
      [react/view {:height           1
                   :background-color (:ui-02 @colors/theme)
                   :margin-vertical  8}]
      [quo/list-header (i18n/label :t/notifications-preferences)]
      [quo/list-item
       {:size                :small
        :disabled            (not remote-push-notifications-enabled)
        :title               (i18n/label :t/notifications-non-contacts)
        :accessibility-label :notifications-button
        :active              (and remote-push-notifications-enabled
                                  (not push-notifications-from-contacts-only))
        :on-press            #(re-frame/dispatch
                               [::notifications/switch-non-contacts (not push-notifications-from-contacts-only)])
        :accessory           :switch}]]]))
