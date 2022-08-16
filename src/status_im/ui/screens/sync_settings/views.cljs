(ns status-im.ui.screens.sync-settings.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [quo.core :as quo]
            [status-im.i18n.i18n :as i18n]
            [status-im.constants :as constants]
            [quo.design-system.colors :as colors]
            [status-im.ui.components.react :as react]))

(views/defview sync-settings []
  (views/letsubs [{:keys [syncing-on-mobile-network?
                          backup-enabled?
                          default-sync-period
                          use-mailservers?]}          [:multiaccount]
                  current-mailserver-name             [:mailserver/current-name]]
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
                     :title               (i18n/label :t/backup-settings)
                     :accessibility-label :backup-settings-button
                     :on-press            #(re-frame/dispatch [:navigate-to :backup-settings])
                     :chevron             true
                     :accessory           :text
                     :accessory-text      (if backup-enabled?
                                            (i18n/label :t/backup-enabled)
                                            (i18n/label :t/backup-disabled))}]
     [quo/list-item {:size                :small
                     :title               (i18n/label :t/default-sync-period)
                     :accessibility-label :default-sync-period-button
                     :on-press            #(re-frame/dispatch [:navigate-to :default-sync-period-settings])
                     :chevron             true
                     :accessory           :text
                     :accessory-text      (cond
                                            (= default-sync-period constants/two-mins)
                                            (i18n/label :t/two-minutes)
                                            (or
                                             (nil? default-sync-period)
                                             (= default-sync-period constants/one-day))
                                            (i18n/label :t/one-day)
                                            (= default-sync-period constants/three-days)
                                            (i18n/label :t/three-days)
                                            (= default-sync-period constants/one-week)
                                            (i18n/label :t/one-week)
                                            (= default-sync-period constants/one-month)
                                            (i18n/label :t/one-month))}]
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
                     :chevron             true}]]))
