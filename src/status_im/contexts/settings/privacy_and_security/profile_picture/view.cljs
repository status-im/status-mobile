(ns status-im.contexts.settings.privacy-and-security.profile-picture.view
  (:require
    [quo.context]
    [quo.core :as quo]
    [status-im.constants :as constants]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def pictures-visibility->label
  {constants/profile-pictures-visibility-everyone      (i18n/label :t/everyone)
   constants/profile-pictures-visibility-contacts-only (i18n/label :t/contacts)
   constants/profile-pictures-visibility-none          (i18n/label :t/no-one)})

(def pictures-show-to->label
  {constants/profile-pictures-show-to-everyone      (i18n/label :t/everyone)
   constants/profile-pictures-show-to-contacts-only (i18n/label :t/contacts)
   constants/profile-pictures-show-to-none          (i18n/label :t/no-one)})

(defn- update-profile
  [k v]
  (rf/dispatch [:profile.settings/profile-update k v])
  (rf/dispatch [:hide-bottom-sheet]))

(defn- update-pictures-visibility-everyone
  []
  (update-profile :profile-pictures-visibility constants/profile-pictures-visibility-everyone))

(defn- update-pictures-visibility-contacts-only
  []
  (update-profile :profile-pictures-visibility constants/profile-pictures-visibility-contacts-only))

(defn- update-pictures-visibility-nobody
  []
  (update-profile :profile-pictures-visibility constants/profile-pictures-visibility-none))

(defn- update-pictures-show-to-everyone
  []
  (update-profile :profile-pictures-show-to constants/profile-pictures-show-to-everyone))

(defn- update-pictures-show-to-contacts-only
  []
  (update-profile :profile-pictures-show-to constants/profile-pictures-show-to-contacts-only))

(defn- update-pictures-show-to-nobody
  []
  (update-profile :profile-pictures-show-to constants/profile-pictures-show-to-none))

(defn options-for-profile-pictures-visibility
  [setting]
  (let [customization-color (rf/sub [:profile/customization-color])]
    [:<>
     [quo/drawer-top {:title (i18n/label :t/show-profile-pictures)}]
     [quo/action-drawer
      [[{:icon                :i/globe
         :accessibility-label :see-profile-pictures-from-everyone
         :label               (i18n/label :t/everyone)
         :on-press            update-pictures-visibility-everyone
         :state               (when (= constants/profile-pictures-visibility-everyone setting)
                                :selected)}
        {:icon                :i/contact
         :icon-color          customization-color
         :accessibility-label :see-profile-pictures-from-contacts
         :label               (i18n/label :t/contacts)
         :on-press            update-pictures-visibility-contacts-only
         :state               (when (= constants/profile-pictures-visibility-contacts-only setting)
                                :selected)}
        {:icon                :i/hide
         :accessibility-label :see-profile-pictures-from-nobody
         :label               (i18n/label :t/no-one)
         :add-divider?        true
         :on-press            update-pictures-visibility-nobody
         :state               (when (= constants/profile-pictures-visibility-none setting)
                                :selected)}]]]]))

(defn options-for-profile-pictures-show-to
  [setting]
  (let [customization-color (rf/sub [:profile/customization-color])]
    [:<>
     [quo/drawer-top {:title (i18n/label :t/show-profile-pictures-to)}]
     [quo/action-drawer
      [[{:icon                :i/globe
         :accessibility-label :show-profile-pictures-to-everyone
         :label               (i18n/label :t/everyone)
         :on-press            update-pictures-show-to-everyone
         :state               (when (= constants/profile-pictures-show-to-everyone setting)
                                :selected)}
        {:icon                :i/contact
         :icon-color          customization-color
         :accessibility-label :show-profile-pictures-to-contacts
         :label               (i18n/label :t/contacts)
         :on-press            update-pictures-show-to-contacts-only
         :state               (when (= constants/profile-pictures-show-to-contacts-only setting)
                                :selected)}
        {:icon                :i/hide
         :accessibility-label :show-profile-pictures-to-nobody
         :label               (i18n/label :t/no-one)
         :on-press            update-pictures-show-to-nobody
         :state               (when (= constants/profile-pictures-show-to-none setting)
                                :selected)
         :add-divider?        true}]]]]))

(defn setting-profile-pictures-visibility
  [pictures-visibility on-press]
  {:title             (i18n/label :t/show-profile-pictures)
   :description       :text
   :description-props {:text (pictures-visibility->label pictures-visibility)}
   :blur?             true
   :action            :arrow
   :on-press          on-press})

(defn setting-profile-pictures-show-to
  [pictures-show-to on-press]
  {:title             (i18n/label :t/show-profile-pictures-to)
   :description       :text
   :description-props {:text (pictures-show-to->label pictures-show-to)}
   :blur?             true
   :action            :arrow
   :on-press          on-press})
