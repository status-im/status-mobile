(ns status-im.ui.screens.notifications-settings.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [status-im.notifications.core :as notifications]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]))

(defonce server (reagent/atom ""))

(defn notifications-settings []
  (let [{:keys [remote-push-notifications-enabled?
                send-push-notifications?
                push-notifications-server-enabled?
                push-notifications-from-contacts-only?]}
        @(re-frame/subscribe [:multiaccount])]
    [react/view {:flex 1}
     [topbar/topbar {:title :t/notification-settings}]
     [react/scroll-view {:style                   {:flex 1}
                         :content-container-style {:padding-vertical 8}}
      [quo/list-item
       {:size                :small
        :title               (i18n/label :t/notifications)
        :accessibility-label :notifications-button
        :active              remote-push-notifications-enabled?
        :on-press            #(re-frame/dispatch [::notifications/switch (not remote-push-notifications-enabled?)])
        :accessory           :switch}]
      [react/view {:height           1
                   :background-color (:ui-02 @colors/theme)
                   :margin-vertical  8}]
      [quo/list-header (i18n/label :t/notifications-preferences)]
      [quo/list-item
       {:size                :small
        :disabled            (not remote-push-notifications-enabled?)
        :title               (i18n/label :t/notifications-non-contacts)
        :accessibility-label :notifications-button
        :active              (and remote-push-notifications-enabled?
                                  (not push-notifications-from-contacts-only?))
        :on-press            #(re-frame/dispatch
                               [::notifications/switch-non-contacts (not push-notifications-from-contacts-only?)])
        :accessory           :switch}]
      [quo/list-item
       {:size                :small
        :disabled            (not remote-push-notifications-enabled?)
        :title               (i18n/label :t/send-push-notifications)
        :accessibility-label :send-push-notifications-button
        :active              (and remote-push-notifications-enabled?
                                  send-push-notifications?)
        :on-press            #(re-frame/dispatch
                               [::notifications/switch-send-push-notifications (not send-push-notifications?)])
        :accessory           :switch}]
      [quo/list-item
       {:size                :small
        :disabled            (not remote-push-notifications-enabled?)
        :title               (i18n/label :t/push-notifications-server-enabled)
        :accessibility-label :send-push-notifications-button
        :active              (and remote-push-notifications-enabled?
                                  push-notifications-server-enabled?)
        :on-press            #(re-frame/dispatch
                               [::notifications/switch-push-notifications-server-enabled (not push-notifications-server-enabled?)])
        :accessory           :switch}]
      [quo/text-input
       {:label          (i18n/label :t/server)
        :placeholder    (i18n/label :t/specify-server-public-key)
        :value          @server
        :on-change-text #(reset! server %)
        :auto-focus     true}]
      [quo/button {:type     :secondary
                   :after    :main-icon/next
                   :disabled (or (not remote-push-notifications-enabled?)
                                 (empty? @server))
                   :on-press #(do
                                (re-frame/dispatch [::notifications/add-server @server])
                                (reset! server ""))}
       (i18n/label :t/save)]]]))
