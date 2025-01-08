(ns status-im.contexts.profile.contact.contact-review.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.constants :as constants]
            [status-im.contexts.profile.contact.contact-review.style :as style]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [{:keys [public-key customization-color]
         :as   profile}                (rf/sub [:contacts/current-contact])
        {contact-request-id :id
         :keys              [message]} (rf/sub [:activity-center/pending-contact-request-from-contact-id
                                                public-key])
        ;; TODO(@seanstrom): https://github.com/status-im/status-mobile/issues/18733
        customization-color            (or customization-color constants/profile-default-color)
        full-name                      (profile.utils/displayed-name profile)
        profile-picture                (profile.utils/photo profile)
        on-contact-accept              (rn/use-callback
                                        (fn []
                                          (rf/dispatch [:hide-bottom-sheet])
                                          (rf/dispatch [:activity-center.contact-requests/accept
                                                        contact-request-id])
                                          (rf/dispatch [:toasts/upsert
                                                        {:id   :accept-contact-request
                                                         :type :positive
                                                         :text (i18n/label
                                                                :t/contact-request-was-accepted)}]))
                                        [contact-request-id])
        on-contact-ignore              (rn/use-callback
                                        (fn []
                                          (rf/dispatch [:hide-bottom-sheet])
                                          (rf/dispatch [:activity-center.contact-requests/decline
                                                        contact-request-id])
                                          (rf/dispatch [:toasts/upsert
                                                        {:id   :ignore-contact-request
                                                         :type :positive
                                                         :text (i18n/label
                                                                :t/contact-request-was-ignored)}]))
                                        [contact-request-id])]
    [:<>
     [quo/drawer-top
      {:type                :context-tag
       :context-tag-type    :default
       :title               (i18n/label :t/contact-request-review)
       :full-name           full-name
       :profile-picture     profile-picture
       :customization-color customization-color}]
     [rn/view {:style style/message-input-wrapper}
      [quo/locked-input {}
       (-> message :content :text)]]
     [rn/view {:style style/bottom-actions-wrapper}
      [quo/bottom-actions
       {:actions          :two-actions
        :button-one-label (i18n/label :t/accept)
        :button-one-props {:type     :positive
                           :on-press on-contact-accept}
        :button-two-props {:type     :danger
                           :on-press on-contact-ignore}
        :button-two-label (i18n/label :t/ignore)}]]]))
