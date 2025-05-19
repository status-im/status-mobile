(ns status-im.contexts.profile.settings.screens.notifications.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [status-im.common.events-helper :as events-helper]
    [status-im.config :as config]
    [status-im.contexts.profile.settings.screens.notifications.styles :as styles]
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
  [{:keys [notifications-blocked? notifications-enabled?]}]
  (let [on-change (rn/use-callback
                   #(toggle-notifications-enabled notifications-enabled?)
                   [notifications-enabled?])]
    {:blur?        true
     :title        (i18n/label :t/show-notifications)
     :action       :selector
     :action-props {:on-change on-change
                    :checked?  (and (not notifications-blocked?)
                                    notifications-enabled?)}}))

(defn chat-non-contacts-notifications-setting
  [{:keys [notifications-enabled?
           messenger-notifications-enabled?
           non-contact-notifications-enabled?]}]
  (let [disabled? (or (not notifications-enabled?)
                      (not messenger-notifications-enabled?))
        on-change (rn/use-callback
                   #(toggle-non-contact-notifications non-contact-notifications-enabled?)
                   [non-contact-notifications-enabled?])]
    {:blur?             true
     :title             (i18n/label :t/notifications-non-contacts)
     :description       :text
     :description-props {:text (i18n/label :t/notifications-non-contacts-description)}
     :action            :selector
     :action-props      {:on-change (when-not disabled? on-change)
                         :disabled? disabled?
                         :checked?  (if (ff/enabled? ::ff/settings.news-notifications)
                                      non-contact-notifications-enabled?
                                      (and (not disabled?)
                                           non-contact-notifications-enabled?))}}))

(defn chat-community-mentions-notifications-setting
  [{:keys [notifications-enabled?
           messenger-notifications-enabled?
           community-mentions-notifications-enabled?]}]
  (let [disabled? (or (not notifications-enabled?)
                      (not messenger-notifications-enabled?))
        on-change (rn/use-callback
                   #(toggle-community-mentions-notifications community-mentions-notifications-enabled?)
                   [community-mentions-notifications-enabled?])]
    {:blur?             true
     :title             (i18n/label :t/communities)
     :description       :text
     :description-props {:text (i18n/label :t/allow-community-mentions-notifications-description)}
     :action            :selector
     :action-props      {:on-change (when-not disabled? on-change)
                         :disabled? disabled?
                         :checked?  (if (ff/enabled? ::ff/settings.news-notifications)
                                      community-mentions-notifications-enabled?
                                      (and (not disabled?)
                                           community-mentions-notifications-enabled?))}}))

(defn messenger-notifications-setting
  [{:keys [notifications-enabled? messenger-notifications-enabled?]}]
  (let [disabled? (not notifications-enabled?)
        on-change (rn/use-callback
                   #(toggle-messenger-notifications messenger-notifications-enabled?)
                   [messenger-notifications-enabled?])]
    (cond-> {:blur?        true
             :title        (i18n/label :t/allow-messenger-notifications)
             :action       :selector
             :action-props {:on-change (when-not disabled? on-change)
                            :disabled? disabled?
                            :checked?  messenger-notifications-enabled?}}
      platform/android?
      (assoc :title             (i18n/label :t/allow-messages-and-communities-notifications)
             :description       :text
             :description-props {:text (i18n/label
                                        :t/allow-messages-and-communities-notifications-description)}))))

(defn news-notifications-setting
  [{:keys [notifications-enabled? news-notifications-enabled?]}]
  (let [disabled? (not notifications-enabled?)
        on-change (rn/use-callback
                   #(toggle-news-notifications news-notifications-enabled?)
                   [news-notifications-enabled?])]
    (cond-> {:blur?        true
             :title        (i18n/label :t/allow-news-notifications)
             :action       :selector
             :action-props {:on-change (when-not disabled? on-change)
                            :disabled? disabled?
                            :checked?  news-notifications-enabled?}}
      platform/android?
      (assoc :description       :text
             :description-props {:text (i18n/label :t/allow-news-notifications-description)}))))

(defn- settings-group-item
  [item & _rest]
  [quo/category
   {:blur?           true
    :list-type       :settings
    :container-style styles/settings-group-item-container
    :data            [item]}])

(defn- messenger-notifications-settings
  [notifications-settings]
  (let [{:keys [action-props title]} (messenger-notifications-setting notifications-settings)]
    [rn/pressable
     {:on-press (:on-change action-props)
      :style    styles/settings-group-container}
     [rn/view
      {:style styles/settings-group-header}
      [quo/text {:style {:flex 1}} title]
      [quo/selectors
       {:type      :toggle
        :checked?  (:checked? action-props)
        :disabled? (:disabled? action-props)
        :on-change (:on-change action-props)}]]
     [rn/flat-list
      {:data      [(chat-non-contacts-notifications-setting notifications-settings)
                   (chat-community-mentions-notifications-setting notifications-settings)]
       :render-fn settings-group-item
       :separator [rn/view {:style {:height 0}}]}]]))

(defn view
  []
  (let [notifications-settings (rf/sub [:profile/notifications-settings])]
    (rn/use-mount #(rf/dispatch [:notifications/check-notifications-blocked]))
    [quo/overlay {:type :shell :top-inset? true}
     [quo/page-nav
      {:background :blur
       :icon-name  :i/arrow-left
       :on-press   events-helper/navigate-back}]
     [quo/page-top {:title (i18n/label :t/notifications)}]
     (when (:notifications-blocked? notifications-settings)
       [quo/information-box
        {:type            :error
         :style           styles/information-box
         :blur?           false
         :on-button-press #(rf/dispatch [:notifications/open-notifications-settings])
         :button-label    [rn/view {:style styles/information-box-button-label}
                           [quo/text {:size :paragraph-2}
                            (i18n/label :t/enabled-push-notifications)]
                           [quo/icon :i/external {:size 12}]]}
        (i18n/label (if platform/ios?
                      :t/push-notifications-blocked-ios
                      :t/push-notifications-blocked-android))])
     [quo/category
      {:blur?     true
       :list-type :settings
       :data      [(notifications-enabled-setting notifications-settings)]}]
     (if (ff/enabled? ::ff/settings.news-notifications)
       [:<>
        (cond
          platform/ios?
          [messenger-notifications-settings notifications-settings]

          platform/android?
          [quo/category
           {:blur?     true
            :list-type :settings
            :data      [(messenger-notifications-setting notifications-settings)]}]

          :else nil)

        (when-not config/fdroid?
          [quo/category
           {:blur?     true
            :list-type :settings
            :data      [(news-notifications-setting notifications-settings)]}])]

       (when platform/ios?
         [quo/category
          {:blur?     true
           :list-type :settings
           :data      [(chat-non-contacts-notifications-setting notifications-settings)
                       (chat-community-mentions-notifications-setting notifications-settings)]}]))]))
