(ns status-im.contexts.profile.contact.header.view
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [quo.theme]
            [react-native.core :as rn]
            [status-im.common.scalable-avatar.view :as avatar]
            [status-im.constants :as constants]
            [status-im.contexts.profile.contact.header.style :as style]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  [{:keys [scroll-y]}]
  (let [{:keys [public-key customization-color
                emoji-hash bio contact-request-state]
         :as   profile}     (rf/sub [:contacts/current-contact])
        customization-color (or customization-color :blue)
        full-name           (profile.utils/displayed-name profile)
        profile-picture     (profile.utils/photo profile)
        online?             (rf/sub [:visibility-status-updates/online? public-key])
        theme               (quo.theme/use-theme-value)]
    [rn/view {:style style/header-container}
     [rn/view {:style style/header-top-wrapper}
      [rn/view {:style style/avatar-wrapper}
       [avatar/view
        {:scroll-y            scroll-y
         :full-name           full-name
         :online?             online?
         :profile-picture     profile-picture
         :border-color        (colors/theme-colors colors/white colors/neutral-95 theme)
         :customization-color customization-color}]]
      (when (= contact-request-state constants/contact-request-state-sent)
        [rn/view {:style style/status-tag-wrapper}
         [quo/status-tag
          {:label  (i18n/label :t/contact-profile-request-pending)
           :status {:type :pending}
           :size   :large}]])]
     [quo/page-top
      {:title            full-name
       :description      :text
       :description-text bio
       :emoji-dash       emoji-hash}]]))
