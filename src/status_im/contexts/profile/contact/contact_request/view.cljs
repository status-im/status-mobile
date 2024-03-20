(ns status-im.contexts.profile.contact.contact-request.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.constants :as constants]
            [status-im.contexts.profile.contact.contact-request.style :as style]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [{:keys [public-key customization-color]
         :as   profile}       (rf/sub [:contacts/current-contact])
        ;; TODO: remove default color when #18733 merged.
        customization-color   (or customization-color constants/profile-default-color)
        full-name             (profile.utils/displayed-name profile)
        profile-picture       (profile.utils/photo profile)
        [message set-message] (rn/use-state "")
        on-message-change     (rn/use-callback #(set-message %))
        on-message-submit     (rn/use-callback (fn []
                                                 (rf/dispatch [:hide-bottom-sheet])
                                                 (rf/dispatch [:contact.ui/send-contact-request
                                                               public-key message])
                                                 (rf/dispatch [:toasts/upsert
                                                               {:id   :send-contact-request
                                                                :type :positive
                                                                :text (i18n/label
                                                                       :t/contact-request-was-sent)}]))
                                               [public-key message])]
    [:<>
     [quo/drawer-top
      {:type                :context-tag
       :context-tag-type    :default
       :title               (i18n/label :t/send-contact-request)
       :full-name           full-name
       :profile-picture     profile-picture
       :customization-color customization-color}]
     [quo/text {:style style/message-prompt-wrapper}
      (i18n/label :t/contact-request-message-prompt)]
     [rn/view {:style style/message-input-wrapper}
      [quo/input
       {:type                :text
        :multiline?          true
        :char-limit          constants/contact-request-message-max-length
        :max-length          constants/contact-request-message-max-length
        :placeholder         (i18n/label :t/type-something)
        :auto-focus          true
        :accessibility-label :contact-request-message
        :label               (i18n/label :t/message)
        :on-change-text      on-message-change}]]
     [quo/bottom-actions
      {:actions          :one-action
       :button-one-props {:disabled?           (string/blank? message)
                          :accessibility-label :send-contact-request
                          :customization-color customization-color
                          :on-press            on-message-submit}
       :button-one-label (i18n/label :t/send-contact-request)}]]))

