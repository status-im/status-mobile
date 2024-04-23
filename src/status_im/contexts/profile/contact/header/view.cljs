(ns status-im.contexts.profile.contact.header.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
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

(defn on-contact-request
  []
  (rf/dispatch [:show-bottom-sheet
                {:content (fn [] [contact-request/view])}]))

(defn on-contact-review
  []
  (rf/dispatch [:show-bottom-sheet
                {:content (fn [] [contact-review/view])}]))

(defn view
  [{:keys [scroll-y]}]
  (let [{:keys [public-key customization-color ens-name nickname secondary-name
                emoji-hash bio blocked? contact-request-state]
         :as   contact}     (rf/sub [:contacts/current-contact])
        customization-color (or customization-color constants/profile-default-color)
        full-name           (profile.utils/displayed-name contact)
        profile-picture     (profile.utils/photo contact)
        online?             (rf/sub [:visibility-status-updates/online? public-key])
        theme               (quo.theme/use-theme)
        contact-status      (rn/use-memo (fn []
                                           (cond
                                             (= contact-request-state
                                                constants/contact-request-state-mutual) :contact
                                             blocked?                                   :blocked
                                             :else                                      nil))
                                         [blocked? contact-request-state])
        on-start-chat       (rn/use-callback #(rf/dispatch [:chat.ui/start-chat
                                                            public-key
                                                            ens-name])
                                             [ens-name public-key])
        on-unblock-press    (rn/use-callback (fn []
                                               (rf/dispatch [:contact.ui/unblock-contact-pressed
                                                             public-key])
                                               (rf/dispatch [:toasts/upsert
                                                             {:id   :user-unblocked
                                                              :type :positive
                                                              :text (i18n/label :t/user-unblocked
                                                                                {:username
                                                                                 full-name})}]))
                                             [public-key full-name])]
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
     [rn/view {:style style/username-wrapper}
      [quo/username
       {:name-type           (if-not (string/blank? nickname) :nickname :default)
        :accessibility-label :contact-name
        :username            full-name
        :status              contact-status
        :name                secondary-name}]]
     [quo/page-top
      {:description      :text
       :description-text (when-not blocked? bio)
       :emoji-dash       emoji-hash}]

     (when blocked?
       [quo/button
        {:container-style     style/button-wrapper
         :on-press            on-unblock-press
         :customization-color customization-color
         :icon-left           :i/block}
        (i18n/label :t/unblock)])

     (cond
       (and (not blocked?)
            (or
             (not contact-request-state)
             (= contact-request-state constants/contact-request-state-none)
             (= contact-request-state constants/contact-request-state-dismissed)))

       [quo/button
        {:container-style     style/button-wrapper
         :on-press            on-contact-request
         :customization-color customization-color
         :icon-left           :i/add-user}
        (i18n/label :t/send-contact-request)]

       (= contact-request-state constants/contact-request-state-received)
       [quo/button
        {:container-style     style/button-wrapper
         :on-press            on-contact-review
         :customization-color customization-color
         :icon-left           :i/add-user}
        (i18n/label :t/contact-request-review)]

       (= contact-request-state constants/contact-request-state-mutual)
       [quo/button
        {:container-style     style/button-wrapper
         :on-press            on-start-chat
         :customization-color customization-color
         :icon-left           :i/messages}
        (i18n/label :t/send-message)]

       :else nil)]))
