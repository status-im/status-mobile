(ns status-im.ui.screens.sync-settings.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]))

(defn- list-data [mailserver-id use-mobile-data?]
  (let [mobile-data-usage-state (if use-mobile-data?
                                  :t/mobile-network-use-mobile
                                  :t/mobile-network-use-wifi)]
    [{:container-margin-top 8
      :type                 :section-header
      :title                :t/message-syncing}
     {:type                :small
      :title               :t/mobile-network-settings
      :accessibility-label :notifications-button
      :on-press
      #(re-frame/dispatch [:navigate-to :mobile-network-settings])
      :accessories         [mobile-data-usage-state :chevron]}
     {:type                    :small
      :title                   :t/offline-messaging
      :accessibility-label     :offline-messages-settings-button
      :on-press
      #(re-frame/dispatch [:navigate-to :offline-messaging-settings])
      :accessories             [mailserver-id :chevron]
      :container-margin-bottom 8}
     list-item/divider
     {:container-margin-top 8
      :type                 :section-header
      :title                :t/device-syncing}
     {:type                :small
      :title               :t/devices
      :accessibility-label :pairing-settings-button
      :on-press            #(re-frame/dispatch [:navigate-to :installations])
      :accessories         [:chevron]}]))

(views/defview sync-settings []
  (views/letsubs [{:keys [syncing-on-mobile-network?]} [:multiaccount]
                  mailserver-id                        [:mailserver/current-id]]
    [react/view {:flex 1 :background-color colors/white}
     [status-bar/status-bar]
     [toolbar/simple-toolbar
      (i18n/label :t/sync-settings)]
     [list/flat-list
      {:data      (list-data mailserver-id syncing-on-mobile-network?)
       :key-fn    (fn [_ i] (str i))
       :render-fn list/flat-list-generic-render-fn}]]))
