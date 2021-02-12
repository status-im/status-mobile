(ns status-im.ui.screens.notifications-settings.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n.i18n :as i18n]
            [quo.core :as quo]
            [quo.platform :as platform]
            [quo.design-system.colors :as quo-colors]
            [status-im.notifications.core :as notifications]
            [status-im.ui.components.colors :as colors]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]))

(defonce server (reagent/atom ""))

(defn local-notifications []
  [:<>
   (let [{:keys [enabled]} @(re-frame/subscribe [:notifications/wallet-transactions])]
     [quo/separator {:color (:ui-02 @quo-colors/theme)
                     :style {:margin-vertical 8}}]
     [quo/list-header (i18n/label :t/local-notifications)]
     [quo/list-item
      {:size                :small
       :title               (i18n/label :t/notifications-transactions)
       :accessibility-label :notifications-button
       :active              enabled
       :on-press            #(re-frame/dispatch
                              [::notifications/switch-transaction-notifications enabled])
       :accessory           :switch}])])

(defn notifications-settings-ios []
  (let [{:keys [remote-push-notifications-enabled?
                push-notifications-block-mentions?
                push-notifications-from-contacts-only?]}
        @(re-frame/subscribe [:multiaccount])]
    [:<>
     [quo/list-item
      {:size                :small
       :title               (i18n/label :t/show-notifications)
       :accessibility-label :notifications-button
       :active              remote-push-notifications-enabled?
       :on-press            #(re-frame/dispatch [::notifications/switch (not remote-push-notifications-enabled?)])
       :accessory           :switch}]
     [quo/separator {:color (:ui-02 @quo-colors/theme)
                     :style {:margin-vertical 8}}]
     [quo/list-header (i18n/label :t/notifications-preferences)]
     [quo/list-item
      {:size                :small
       :title               (i18n/label :t/notifications-non-contacts)
       :accessibility-label :notifications-button
       :active              (and remote-push-notifications-enabled?
                                 (not push-notifications-from-contacts-only?))
       :on-press            #(re-frame/dispatch
                              [::notifications/switch-non-contacts (not push-notifications-from-contacts-only?)])
       :accessory           :switch}]
     [quo/list-item
      {:size                :small
       :title               (i18n/label :t/allow-mention-notifications)
       :accessibility-label :notifications-button
       :active              (and remote-push-notifications-enabled?
                                 (not push-notifications-block-mentions?))
       :on-press            #(re-frame/dispatch
                              [::notifications/switch-block-mentions (not push-notifications-block-mentions?)])
       :accessory           :switch}]
     [local-notifications]]))

(defn notifications-settings-android []
  (let [{:keys [notifications-enabled?]}
        @(re-frame/subscribe [:multiaccount])]
    [:<>
     [quo/list-item
      {:icon                :main-icons/notification
       :title               (i18n/label :t/notifications)
       :accessibility-label :notifications-settings-button
       :active              notifications-enabled?
       :on-press            #(re-frame/dispatch
                              [::notifications/switch (not notifications-enabled?)])
       :accessory           :switch}]
     [local-notifications]]))

(defn notifications-settings []
  [react/view {:flex 1}
   [topbar/topbar {:title (i18n/label :t/notification-settings)}]
   [react/scroll-view {:style                   {:flex 1}
                       :content-container-style {:padding-vertical 8}}
    (if platform/ios?
      [notifications-settings-ios]
      [notifications-settings-android])]])

(defn notifications-advanced-settings []
  (let [{:keys [remote-push-notifications-enabled?
                send-push-notifications?
                push-notifications-server-enabled?]}
        @(re-frame/subscribe [:multiaccount])]
    [react/view {:flex 1}
     [topbar/topbar {:title (i18n/label :t/notification-settings)}]
     [react/scroll-view {:style                   {:flex 1}
                         :content-container-style {:padding-vertical 8}}
      [quo/list-item
       {:size                :small
        :title               (i18n/label :t/send-push-notifications)
        :accessibility-label :send-push-notifications-button
        :active              send-push-notifications?
        :on-press            #(re-frame/dispatch
                               [::notifications/switch-send-push-notifications (not send-push-notifications?)])
        :accessory           :switch}]
      [quo/list-footer
       (i18n/label :t/send-push-notifications-description)]
      [quo/separator {:style {:margin-vertical  8}}]
      [quo/list-item
       {:size                :small
        :title               (i18n/label :t/push-notifications-server-enabled)
        :accessibility-label :send-push-notifications-button
        :active              (and remote-push-notifications-enabled?
                                  push-notifications-server-enabled?)
        :on-press            #(re-frame/dispatch
                               [::notifications/switch-push-notifications-server-enabled (not push-notifications-server-enabled?)])
        :accessory           :switch}]
      [quo/list-item
       {:size                :small
        :title               (i18n/label :t/push-notifications-servers)
        :accessibility-label :send-push-notifications-button
        :chevron             true
        :on-press            #(re-frame/dispatch
                               [:navigate-to :notifications-servers])}]]]))

(defn notifications-onboarding []
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
                                [::notifications/switch true])
                               (re-frame/dispatch [:navigate-to :welcome]))
                :accessibility-label :enable-notifications}
    (i18n/label :t/intro-wizard-title6)]
   [quo/button {:type     :secondary :style {:margin-top 8}
                :accessibility-label :maybe-later
                :on-press #(re-frame/dispatch [:navigate-to :welcome])}
    (i18n/label :t/maybe-later)]])

(defn server-view [{:keys [public-key type registered]}]
  [quo/list-item
   {:size                :small
    :title               (str (subs public-key 0 8)
                              " "
                              (if (= type notifications/server-type-custom)
                                (i18n/label :t/custom)
                                (i18n/label :t/default))
                              " "
                              (if registered
                                (i18n/label :t/registered)
                                (i18n/label :t/not-registered)))}])

(defview notifications-servers []
  (letsubs [servers [:push-notifications/servers]]
    {:component-did-mount #(re-frame/dispatch [::notifications/fetch-servers])}
    [react/view {:flex 1}
     [topbar/topbar {:title :t/notification-servers}]
     [react/scroll-view {:style                   {:flex 1}
                         :content-container-style {:padding-vertical 8}}
      (map server-view servers)
      [react/keyboard-avoiding-view {}
       [react/view {:style {:padding-horizontal 20}}
        [quo/text-input
         {:label          (i18n/label :t/server)
          :placeholder    (i18n/label :t/specify-server-public-key)
          :value          @server
          :on-change-text #(reset! server %)
          :auto-focus     true}]]
       [quo/button {:type     :secondary
                    :after    :main-icon/next
                    :disabled (empty? @server)
                    :on-press #(do
                                 (re-frame/dispatch [::notifications/add-server @server])
                                 (reset! server ""))}
        (i18n/label :t/save)]]]]))
