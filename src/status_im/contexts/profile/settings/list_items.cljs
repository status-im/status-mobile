(ns status-im.contexts.profile.settings.list-items
  (:require [status-im.common.not-implemented :as not-implemented]
            [status-im.config :as config]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn items
  [mnemonic on-backup-seed-press]
  [(when mnemonic
     [{:title            (i18n/label :t/back-up-seed-phrase)
       :on-press         (on-backup-seed-press mnemonic)
       :image-props      :i/seed
       :image            :icon
       :label            :icon
       :label-props      :i/danger
       :label-icon-props {:no-color true}
       :blur?            true
       :action           :arrow}])
   [{:title       (i18n/label :t/edit-profile)
     :on-press    #(rf/dispatch [:open-modal :screen/edit-profile])
     :image-props :i/edit
     :image       :icon
     :blur?       true
     :action      :arrow}
    {:title       (i18n/label :t/password)
     :on-press    #(rf/dispatch [:open-modal :screen/settings-password])
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
    {:title       (i18n/label :t/keycard)
     :on-press    #(rf/dispatch [:open-modal :screen/settings.keycard])
     :image-props :i/keycard
     :image       :icon
     :blur?       true
     :action      :arrow}]
   [{:title       (i18n/label :t/network-settings)
     :blur?       true
     :image       :icon
     :image-props :i/networks
     :on-press    #(rf/dispatch [:open-modal :screen/settings.network-settings])
     :action      :arrow}
    {:title       (i18n/label :t/privacy-and-security)
     :on-press    #(rf/dispatch [:open-modal :screen/settings-privacy-and-security])
     :image-props :i/privacy
     :image       :icon
     :blur?       true
     :action      :arrow}
    {:title       (i18n/label :t/syncing)
     :on-press    #(rf/dispatch [:open-modal :screen/settings.syncing])
     :image-props :i/syncing
     :image       :icon
     :blur?       true
     :action      :arrow}
    {:title       (i18n/label :t/notifications)
     :on-press    #(rf/dispatch [:open-modal :screen/settings.notifications])
     :image-props :i/activity-center
     :image       :icon
     :blur?       true
     :action      :arrow}
    {:title       (i18n/label :t/appearance)
     :on-press    #(rf/dispatch [:open-modal :screen/legacy-appearance])
     :image-props :i/light
     :image       :icon
     :blur?       true
     :action      :arrow}
    {:title       (i18n/label :t/language-and-currency)
     :on-press    #(rf/dispatch [:open-modal :screen/settings.language-and-currency])
     :image-props :i/globe
     :image       :icon
     :blur?       true
     :action      :arrow}]
   [(when config/show-not-implemented-features?
      {:title       (i18n/label :t/data-usage)
       :on-press    not-implemented/alert
       :image-props :i/mobile
       :image       :icon
       :blur?       true
       :action      :arrow})
    {:title       (i18n/label :t/advanced)
     :on-press    #(rf/dispatch [:open-modal :screen/legacy-advanced-settings])
     :image-props :i/settings
     :image       :icon
     :blur?       true
     :action      :arrow}]
   ;; temporary link to legacy settings
   [{:title       "Legacy settings"
     :on-press    #(rf/dispatch [:open-modal :screen/legacy-settings])
     :action      :arrow
     :image       :icon
     :blur?       true
     :image-props :i/toggle}
    (when config/debug-or-pr-build?
      {:title       "Quo preview"
       :on-press    #(rf/dispatch [:open-modal :screen/quo-preview])
       :action      :arrow
       :image       :icon
       :blur?       true
       :image-props :i/light})
    (when config/debug-or-pr-build?
      {:title       (i18n/label :t/feature-flags)
       :on-press    #(rf/dispatch [:open-modal :screen/feature-flags])
       :action      :arrow
       :image       :icon
       :blur?       true
       :image-props :i/light})]
   [{:title    (i18n/label :t/about)
     :on-press #(rf/dispatch [:open-modal :screen/settings.about])
     :action   :arrow
     :blur?    true}
    {:title    (i18n/label :t/status-help)
     :on-press #(rf/dispatch [:open-modal :screen/help-center])
     :action   :arrow
     :blur?    true}]])
