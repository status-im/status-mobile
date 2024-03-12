(ns status-im.contexts.profile.contact.header.view
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [quo.theme]
            [react-native.core :as rn]
            [status-im.common.not-implemented]
            [status-im.common.scalable-avatar.view :as avatar]
            [status-im.constants :as constants]
            [status-im.contexts.profile.contact.contact-request.view :as contact-request]
            [status-im.contexts.profile.contact.contact-review.view :as contact-review]
            [status-im.contexts.profile.contact.header.style :as style]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  [{:keys [scroll-y]}]
  (let [{:keys [public-key customization-color ens-name
                emoji-hash bio contact-request-state]
         :as   profile}     (rf/sub [:contacts/current-contact])
        customization-color (or customization-color :blue)
        full-name           (profile.utils/displayed-name profile)
        profile-picture     (profile.utils/photo profile)
        online?             (rf/sub [:visibility-status-updates/online? public-key])
        theme               (quo.theme/use-theme-value)
        on-contact-request  (rn/use-callback #(rf/dispatch [:show-bottom-sheet
                                                            {:content (fn [] [contact-request/view])}]))
        on-contact-review   (rn/use-callback #(rf/dispatch [:show-bottom-sheet
                                                            {:content (fn [] [contact-review/view])}]))
        on-start-chat       (rn/use-callback #(rf/dispatch [:chat.ui/start-chat
                                                            public-key
                                                            ens-name])
                                             [ens-name public-key])]
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
       :emoji-dash       emoji-hash}]

     (cond
       (or (not contact-request-state)
           (= contact-request-state constants/contact-request-state-none)
           (= contact-request-state constants/contact-request-state-dismissed))
       [quo/button
        {:container-style style/button-wrapper
         :on-press        on-contact-request
         :icon-left       :i/add-user}
        (i18n/label :t/send-contact-request)]

       (= contact-request-state constants/contact-request-state-received)
       [quo/button
        {:container-style style/button-wrapper
         :on-press        on-contact-review
         :icon-left       :i/add-user}
        (i18n/label :t/contact-request-review)]

       (= contact-request-state constants/contact-request-state-mutual)
       [quo/button
        {:container-style style/button-wrapper
         :on-press        on-start-chat
         :icon-left       :i/messages}
        (i18n/label :t/send-message)]

       :else nil)]))
