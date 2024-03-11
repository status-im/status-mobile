(ns status-im.contexts.profile.contact.contact-request.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.contexts.profile.contact.contact-request.style :as style]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [{:keys [public-key customization-color]
         :as   profile}       (rf/sub [:contacts/current-contact])
        ;; TODO: remove :blue when #18733 merged.
        customization-color   (or customization-color :blue)
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
     [rn/text {:style style/message-prompt-wrapper}
      (i18n/label :t/contact-request-message-prompt)]
     [rn/view {:style style/message-input-wrapper}
      [quo/input
       {:type           :text
        :multiline?     true
        :char-limit     280
        :label          (i18n/label :t/message)
        :on-change-text on-message-change}]]
     [quo/bottom-actions
      {:actions          :one-action
       :button-one-props {:disabled? (string/blank? message)
                          :on-press  on-message-submit}
       :button-one-label (i18n/label :t/send-contact-request)}]]))

