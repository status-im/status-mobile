(ns status-im.contexts.profile.settings.screens.notifications.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [status-im.common.events-helper :as events-helper]
    [status-im.config :as config]
    [status-im.feature-flags :as ff]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn toggle-notifications-enabled
  [notifications-enabled?]
  (rf/dispatch [:push-notifications/switch (not notifications-enabled?)]))

(defn toggle-messenger-notifications
  [messenger-notifications-enabled?]
  (rf/dispatch [:notifications/messenger-notifications-switch (not messenger-notifications-enabled?)]))

(defn toggle-news-notifications
  [news-notifications-enabled?]
  (rf/dispatch [:notifications/news-notifications-switch (not news-notifications-enabled?)]))

(defn toggle-non-contact-notifications
  [non-contact-notifications-enabled?]
  (rf/dispatch [:push-notifications/switch-non-contacts non-contact-notifications-enabled?]))

(defn toggle-community-mentions-notifications
  [community-mentions-notifications-enabled?]
  (rf/dispatch [:push-notifications/switch-block-mentions community-mentions-notifications-enabled?]))

(defn notifications-enabled-setting
  [{:keys [notifications-enabled?]}]
  (let [on-change (rn/use-callback
                   #(toggle-notifications-enabled notifications-enabled?)
                   [notifications-enabled?])]
    {:blur?        true
     :title        (i18n/label :t/show-notifications)
     :action       :selector
     :action-props {:on-change on-change
                    :checked?  notifications-enabled?}}))

(defn chat-non-contacts-notifications-setting
  [{:keys [notifications-enabled?
           messenger-notifications-enabled?
           non-contact-notifications-enabled?]}]
  (let [disabled? (or (not notifications-enabled?)
                      (not messenger-notifications-enabled?))
        on-change (rn/use-callback
                   #(toggle-non-contact-notifications non-contact-notifications-enabled?)
                   [non-contact-notifications-enabled?])]
    {:blur?        true
     :title        (i18n/label :t/notifications-non-contacts)
     :action       :selector
     :action-props {:on-change (when-not disabled? on-change)
                    :disabled? disabled?
                    :checked?  (and (not disabled?)
                                    non-contact-notifications-enabled?)}}))

(defn chat-community-mentions-notifications-setting
  [{:keys [notifications-enabled?
           messenger-notifications-enabled?
           community-mentions-notifications-enabled?]}]
  (let [disabled? (or (not notifications-enabled?)
                      (not messenger-notifications-enabled?))
        on-change (rn/use-callback
                   #(toggle-community-mentions-notifications community-mentions-notifications-enabled?)
                   [community-mentions-notifications-enabled?])]
    {:blur?        true
     :title        (i18n/label :t/allow-mention-notifications)
     :action       :selector
     :action-props {:on-change (when-not disabled? on-change)
                    :disabled? disabled?
                    :checked?  (and (not disabled?)
                                    community-mentions-notifications-enabled?)}}))

(defn messenger-notifications-setting
  [{:keys [notifications-enabled? messenger-notifications-enabled?]}]
  (let [disabled? (not notifications-enabled?)
        on-change (rn/use-callback
                   #(toggle-messenger-notifications messenger-notifications-enabled?)
                   [messenger-notifications-enabled?])]
    {:blur?        true
     :title        (i18n/label :t/allow-messenger-notifications)
     :action       :selector
     :action-props {:on-change (when-not disabled? on-change)
                    :disabled? disabled?
                    :checked?  messenger-notifications-enabled?}}))

(defn news-notifications-setting
  [{:keys [notifications-enabled? news-notifications-enabled?]}]
  (let [disabled? (not notifications-enabled?)
        on-change (rn/use-callback
                   #(toggle-news-notifications news-notifications-enabled?)
                   [news-notifications-enabled?])]
    {:blur?        true
     :title        (i18n/label :t/allow-news-notifications)
     :action       :selector
     :action-props {:on-change (when-not disabled? on-change)
                    :disabled? disabled?
                    :checked?  news-notifications-enabled?}}))

(defn view
  []
  (let [notifications-settings (rf/sub [:profile/notifications-settings])]
    [quo/overlay {:type :shell :top-inset? true}
     [quo/page-nav
      {:background :blur
       :icon-name  :i/arrow-left
       :on-press   events-helper/navigate-back}]
     [quo/page-top {:title (i18n/label :t/notifications)}]
     [quo/category
      {:blur?     true
       :list-type :settings
       :data      [(notifications-enabled-setting notifications-settings)]}]
     ;; NOTE(@seanstrom): temporarily hide the messenger notification
     ;; toggle until we update the design system
     (if (and (not config/fdroid?)
              (ff/enabled? ::ff/settings.news-notifications))
       [:<>
        (cond
          platform/ios?
          [quo/category
           {:blur?     true
            :list-type :settings
            :data      [(messenger-notifications-setting notifications-settings)
                        (chat-non-contacts-notifications-setting notifications-settings)
                        (chat-community-mentions-notifications-setting notifications-settings)]}]
          platform/android?
          [quo/category
           {:blur?     true
            :list-type :settings
            :data      [(messenger-notifications-setting notifications-settings)]}]
          :else nil)
        [quo/category
         {:blur?     true
          :list-type :settings
          :data      [(news-notifications-setting notifications-settings)]}]]
       (when platform/ios?
         [quo/category
          {:blur?     true
           :list-type :settings
           :data      [(chat-non-contacts-notifications-setting notifications-settings)
                       (chat-community-mentions-notifications-setting notifications-settings)]}]))]))
