(ns status-im.contexts.profile.settings.list-items
  (:require [status-im.common.not-implemented :as not-implemented]
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
     :on-press    not-implemented/alert
     :image-props :i/password
     :image       :icon
     :blur?       true
     :action      :arrow}]
   [{:title       (i18n/label :t/messages)
     :on-press    not-implemented/alert
     :image-props :i/messages
     :image       :icon
     :blur?       true
     :action      :arrow}
    {:title       (i18n/label :t/wallet)
     :on-press    not-implemented/alert
     :image-props :i/wallet
     :image       :icon
     :blur?       true
     :action      :arrow}
    {:title       (i18n/label :t/dapps)
     :on-press    not-implemented/alert
     :image-props :i/placeholder
     :image       :icon
     :blur?       true
     :action      :arrow}
    {:title       (i18n/label :t/browser)
     :on-press    not-implemented/alert
     :image-props :i/browser
     :image       :icon
     :blur?       true
     :action      :arrow}
    {:title       (i18n/label :t/keycard)
     :on-press    not-implemented/alert
     :image-props :i/keycard
     :image       :icon
     :blur?       true
     :action      :arrow}]
   [{:title       (i18n/label :t/syncing)
     :on-press    #(rf/dispatch [:navigate-to :settings-syncing])
     :image-props :i/syncing
     :image       :icon
     :blur?       true
     :action      :arrow}
    {:title       (i18n/label :t/notifications)
     :on-press    #(rf/dispatch [:navigate-to :notifications])
     :image-props :i/activity-center
     :image       :icon
     :blur?       true
     :action      :arrow}
    {:title       (i18n/label :t/appearance)
     :on-press    #(rf/dispatch [:navigate-to :appearance])
     :image-props :i/light
     :image       :icon
     :blur?       true
     :action      :arrow}
    {:title       (i18n/label :t/language-and-currency)
     :on-press    not-implemented/alert
     :image-props :i/globe
     :image       :icon
     :blur?       true
     :action      :arrow}]
   [{:title       (i18n/label :t/data-usage)
     :on-press    not-implemented/alert
     :image-props :i/mobile
     :image       :icon
     :blur?       true
     :action      :arrow}
    {:title       (i18n/label :t/advanced)
     :on-press    #(rf/dispatch [:navigate-to :advanced-settings])
     :image-props :i/settings
     :image       :icon
     :blur?       true
     :action      :arrow}]
   ;; temporary link to legacy settings
   [{:title       "Legacy settings"
     :on-press    #(rf/dispatch [:navigate-to :my-profile])
     :action      :arrow
     :image       :icon
     :blur?       true
     :image-props :i/toggle}
    {:title       "Quo preview"
     :on-press    #(rf/dispatch [:navigate-to :quo-preview])
     :action      :arrow
     :image       :icon
     :blur?       true
     :image-props :i/light}]
   [{:title    (i18n/label :t/about)
     :on-press not-implemented/alert
     :action   :arrow}
    {:title    (i18n/label :t/status-help)
     :on-press not-implemented/alert
     :action   :arrow}]])
