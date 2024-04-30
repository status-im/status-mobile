(ns legacy.status-im.ui.screens.notifications-settings.views
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.core :as components]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.react :as react]
    [quo.core :as quo]
    [react-native.platform :as platform]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn notifications-settings-ios
  []
  (let [{:keys [notifications-enabled?
                push-notifications-block-mentions?
                push-notifications-from-contacts-only?]}
        (rf/sub [:profile/profile])]
    [:<>
     [list.item/list-item
      {:size                :small
       :title               (i18n/label :t/show-notifications)
       :accessibility-label :notifications-button
       :active              notifications-enabled?
       :on-press            #(rf/dispatch [:push-notifications/switch (not notifications-enabled?)])
       :accessory           :switch}]
     [components/separator
      {:color (:ui-02 @colors/theme)
       :style {:margin-vertical 8}}]
     [components/list-header (i18n/label :t/notifications-preferences)]
     [list.item/list-item
      {:size                :small
       :title               (i18n/label :t/notifications-non-contacts)
       :accessibility-label :notifications-button
       :active              (and notifications-enabled?
                                 (not push-notifications-from-contacts-only?))
       :on-press            #(rf/dispatch
                              [:push-notifications/switch-non-contacts
                               (not push-notifications-from-contacts-only?)])
       :accessory           :switch}]
     [list.item/list-item
      {:size                :small
       :title               (i18n/label :t/allow-mention-notifications)
       :accessibility-label :notifications-button
       :active              (and notifications-enabled?
                                 (not push-notifications-block-mentions?))
       :on-press            #(rf/dispatch
                              [:push-notifications/switch-block-mentions
                               (not push-notifications-block-mentions?)])
       :accessory           :switch}]]))

(defn notifications-settings-android
  []
  (let [{:keys [notifications-enabled?]} (rf/sub [:profile/profile])]
    [:<>
     [list.item/list-item
      {:title               (i18n/label :t/local-notifications)
       :accessibility-label :local-notifications-settings-button
       :subtitle            (i18n/label :t/local-notifications-subtitle)
       :active              notifications-enabled?
       :on-press            #(rf/dispatch [:push-notifications/switch (not notifications-enabled?)])
       :accessory           :switch}]]))

(defn notifications-settings
  []
  [:<>
   [quo/page-nav
    {:type       :title
     :title      (i18n/label :t/notification-settings)
     :background :blur
     :icon-name  :i/close
     :on-press   #(rf/dispatch [:navigate-back])}]
   [react/scroll-view
    {:style                   {:flex 1}
     :content-container-style {:padding-vertical 8}}
    (if platform/ios?
      [notifications-settings-ios]
      [notifications-settings-android])]])
