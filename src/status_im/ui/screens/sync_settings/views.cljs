(ns status-im.ui.screens.sync-settings.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [quo.core :as quo]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]))

(views/defview sync-settings []
  (views/letsubs [{:keys [syncing-on-mobile-network?
                          use-mailservers?]}          [:multiaccount]
                  current-mailserver-name             [:mailserver/current-name]]
    [react/view {:style {:flex 1 :background-color colors/white}}
     [topbar/topbar {:title (i18n/label :t/sync-settings)
                     :navigation {:on-press #(re-frame/dispatch [:navigate-to :profile-stack {:screen :my-profile}])}}]
     [react/scroll-view
      [quo/list-header (i18n/label :t/data-syncing)]
      [quo/list-item {:size                :small
                      :title               (i18n/label :t/mobile-network-settings)
                      :accessibility-label :notifications-button
                      :on-press            #(re-frame/dispatch [:navigate-to :mobile-network-settings])
                      :chevron             true
                      :accessory           :text
                      :accessory-text      (if syncing-on-mobile-network?
                                             (i18n/label :t/mobile-network-use-mobile)
                                             (i18n/label :t/mobile-network-use-wifi))}]
      [quo/list-item {:size                :small
                      :accessibility-label :offline-messages-settings-button
                      :title               (i18n/label :t/history-nodes)
                      :on-press            #(re-frame/dispatch [:navigate-to :offline-messaging-settings])
                      :accessory           :text
                      :accessory-text      (when use-mailservers? current-mailserver-name)
                      :chevron             true}]
      ;; TODO(Ferossgp): Devider componemt
      [react/view {:height           1
                   :background-color colors/gray-lighter
                   :margin-top       8}]
      [quo/list-header (i18n/label :t/device-syncing)]
      [quo/list-item {:size                :small
                      :title               (i18n/label :t/devices)
                      :accessibility-label :pairing-settings-button
                      :on-press            #(re-frame/dispatch [:navigate-to :installations])
                      :chevron             true}]]]))
