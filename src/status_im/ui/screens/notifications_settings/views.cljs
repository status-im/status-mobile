(ns status-im.ui.screens.notifications-settings.views
  (:require [quo.core :as quo]
            [quo.design-system.colors :as quo-colors]
            [quo.platform :as platform]
            [utils.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [utils.re-frame :as rf]))

(defn local-notifications
  []
  (let [{:keys [enabled?]}               (rf/sub [:push-notifications/wallet-transactions])
        {:keys [notifications-enabled?]} (rf/sub [:profile/profile])]
    [:<>
     [quo/separator
      {:color (:ui-02 @quo-colors/theme)
       :style {:margin-vertical 8}}]
     [quo/list-header (i18n/label :t/local-notifications)]
     [quo/list-item
      {:size                :small
       :title               (i18n/label :t/notifications-transactions)
       :accessibility-label :notifications-button
       :active              (and notifications-enabled? enabled?)
       :on-press            #(rf/dispatch [:push-notifications.wallet/switch-transactions
                                           (not enabled?)])
       :accessory           :switch}]]))

(defn notifications-settings-ios
  []
  (let [{:keys [notifications-enabled?
                push-notifications-block-mentions?
                push-notifications-from-contacts-only?]}
        (rf/sub [:profile/profile])]
    [:<>
     [quo/list-item
      {:size                :small
       :title               (i18n/label :t/show-notifications)
       :accessibility-label :notifications-button
       :active              notifications-enabled?
       :on-press            #(rf/dispatch [:push-notifications/switch (not notifications-enabled?)])
       :accessory           :switch}]
     [quo/separator
      {:color (:ui-02 @quo-colors/theme)
       :style {:margin-vertical 8}}]
     [quo/list-header (i18n/label :t/notifications-preferences)]
     [quo/list-item
      {:size                :small
       :title               (i18n/label :t/notifications-non-contacts)
       :accessibility-label :notifications-button
       :active              (and notifications-enabled?
                                 (not push-notifications-from-contacts-only?))
       :on-press            #(rf/dispatch
                              [:push-notifications/switch-non-contacts
                               (not push-notifications-from-contacts-only?)])
       :accessory           :switch}]
     [quo/list-item
      {:size                :small
       :title               (i18n/label :t/allow-mention-notifications)
       :accessibility-label :notifications-button
       :active              (and notifications-enabled?
                                 (not push-notifications-block-mentions?))
       :on-press            #(rf/dispatch
                              [:push-notifications/switch-block-mentions
                               (not push-notifications-block-mentions?)])
       :accessory           :switch}]
     [local-notifications]]))

(defn notifications-settings-android
  []
  (let [{:keys [notifications-enabled?]} (rf/sub [:profile/profile])]
    [:<>
     [quo/list-item
      {:title               (i18n/label :t/local-notifications)
       :accessibility-label :local-notifications-settings-button
       :subtitle            (i18n/label :t/local-notifications-subtitle)
       :active              notifications-enabled?
       :on-press            #(rf/dispatch [:push-notifications/switch (not notifications-enabled?)])
       :accessory           :switch}]
     [local-notifications]]))

(defn notifications-settings
  []
  [react/scroll-view
   {:style                   {:flex 1}
    :content-container-style {:padding-vertical 8}}
   (if platform/ios?
     [notifications-settings-ios]
     [notifications-settings-android])])
