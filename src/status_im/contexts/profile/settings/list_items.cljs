(ns status-im.contexts.profile.settings.list-items
  (:require [status-im.common.not-implemented :as not-implemented]
            [status-im.config :as config]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(def items
  [[{:title       (i18n/label :t/edit-profile)
     :on-press    #(rf/dispatch [:open-modal :edit-profile])
     :image-props :i/edit
     :image       :icon
     :blur?       true
     :action      :arrow}
    {:title       (i18n/label :t/password)
     :on-press    #(rf/dispatch [:open-modal :settings-password])
     :image-props :i/password
     :image       :icon
     :blur?       true
     :action      :arrow}]
   [{:title       (i18n/label :t/messages)
     :on-press    #(rf/dispatch [:open-modal :screen/settings-messages])
     :image-props :i/messages
     :image       :icon
     :blur?       true
     :action      :arrow}
    {:title       (i18n/label :t/wallet)
     :on-press    #(rf/dispatch [:open-modal :screen/settings.wallet])
     :image-props :i/wallet
     :image       :icon
     :blur?       true
     :action      :arrow}
    (when config/show-not-implemented-features?
      {:title       (i18n/label :t/dapps)
       :on-press    not-implemented/alert
       :image-props :i/dapps
       :image       :icon
       :blur?       true
       :action      :arrow})
    (when config/show-not-implemented-features?
      {:title       (i18n/label :t/browser)
       :on-press    not-implemented/alert
       :image-props :i/browser
       :image       :icon
       :blur?       true
       :action      :arrow})
    (when config/show-not-implemented-features?
      {:title       (i18n/label :t/keycard)
       :on-press    not-implemented/alert
       :image-props :i/keycard
       :image       :icon
       :blur?       true
       :action      :arrow})]
   [{:title       (i18n/label :t/syncing)
     :on-press    #(rf/dispatch [:open-modal :settings-syncing])
     :image-props :i/syncing
     :image       :icon
     :blur?       true
     :action      :arrow}
    {:title       (i18n/label :t/notifications)
     :on-press    #(rf/dispatch [:open-modal :notifications])
     :image-props :i/activity-center
     :image       :icon
     :blur?       true
     :action      :arrow}
    {:title       (i18n/label :t/appearance)
     :on-press    #(rf/dispatch [:open-modal :appearance])
     :image-props :i/light
     :image       :icon
     :blur?       true
     :action      :arrow}
    (when config/show-not-implemented-features?
      {:title       (i18n/label :t/language-and-currency)
       :on-press    not-implemented/alert
       :image-props :i/globe
       :image       :icon
       :blur?       true
       :action      :arrow})]
   [(when config/show-not-implemented-features?
      {:title       (i18n/label :t/data-usage)
       :on-press    not-implemented/alert
       :image-props :i/mobile
       :image       :icon
       :blur?       true
       :action      :arrow})
    {:title       (i18n/label :t/advanced)
     :on-press    #(rf/dispatch [:open-modal :advanced-settings])
     :image-props :i/settings
     :image       :icon
     :blur?       true
     :action      :arrow}]
   ;; temporary link to legacy settings
   [{:title       "Legacy settings"
     :on-press    #(rf/dispatch [:open-modal :my-profile])
     :action      :arrow
     :image       :icon
     :blur?       true
     :image-props :i/toggle}
    (when config/quo-preview-enabled?
      {:title       "Quo preview"
       :on-press    #(rf/dispatch [:open-modal :quo-preview])
       :action      :arrow
       :image       :icon
       :blur?       true
       :image-props :i/light})
    (when config/quo-preview-enabled?
      {:title       "Feature Flags"
       :on-press    #(rf/dispatch [:open-modal :feature-flags])
       :action      :arrow
       :image       :icon
       :blur?       true
       :image-props :i/light})]
   [{:title    (i18n/label :t/about)
     :on-press #(rf/dispatch [:open-modal :about-app])
     :action   :arrow}
    {:title    (i18n/label :t/status-help)
     :on-press #(rf/dispatch [:open-modal :help-center])
     :action   :arrow}]])
