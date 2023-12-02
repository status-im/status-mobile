(ns status-im2.contexts.profile.settings.list-items
  (:require [status-im2.common.not-implemented :as not-implemented]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(def items
  [[{:title       (i18n/label :t/edit-profile)
     :on-press    not-implemented/alert
     :image-props :i/edit
     :image       :icon
     :action      :arrow}
    {:title       (i18n/label :t/password)
     :on-press    not-implemented/alert
     :image-props :i/password
     :image       :icon
     :action      :arrow}]
   [{:title       (i18n/label :t/messages)
     :on-press    not-implemented/alert
     :image-props :i/messages
     :image       :icon
     :action      :arrow}
    {:title       (i18n/label :t/wallet)
     :on-press    not-implemented/alert
     :image-props :i/wallet
     :image       :icon
     :action      :arrow}
    {:title       (i18n/label :t/dapps)
     :on-press    not-implemented/alert
     :image-props :i/placeholder
     :image       :icon
     :action      :arrow}
    {:title       "Browser"
     :on-press    not-implemented/alert
     :image-props :i/browser
     :image       :icon
     :action      :arrow}
    {:title       (i18n/label :t/keycard)
     :on-press    not-implemented/alert
     :image-props :i/keycard
     :image       :icon
     :action      :arrow}]
   [{:title       (i18n/label :t/syncing)
     :on-press    not-implemented/alert
     :image-props :i/syncing
     :image       :icon
     :action      :arrow}
    {:title       (i18n/label :t/notifications)
     :on-press    not-implemented/alert
     :image-props :i/notifications
     :image       :icon
     :action      :arrow}
    {:title       (i18n/label :t/appearance)
     :on-press    not-implemented/alert
     :image-props :i/light
     :image       :icon
     :action      :arrow}
    {:title       (i18n/label :t/language-and-currency)
     :on-press    not-implemented/alert
     :image-props :i/globe
     :image       :icon
     :action      :arrow}]
   [{:title       (i18n/label :t/data-usage)
     :on-press    not-implemented/alert
     :image-props :i/mobile
     :image       :icon
     :action      :arrow}
    {:title       (i18n/label :t/advanced)
     :on-press    not-implemented/alert
     :image-props :i/settings
     :image       :icon
     :action      :arrow}]
   ;; temporary link to legacy settings
   [{:title       "Legacy settings"
     :on-press    #(rf/dispatch [:navigate-to :my-profile])
     :action      :arrow
     :image       :icon
     :image-props :i/toggle}]
   [{:title    (i18n/label :t/about)
     :on-press not-implemented/alert
     :action   :arrow}
    {:title    (i18n/label :t/status-help)
     :on-press not-implemented/alert
     :action   :arrow}]])
