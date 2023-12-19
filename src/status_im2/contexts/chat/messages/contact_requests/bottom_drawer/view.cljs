(ns status-im2.contexts.chat.messages.contact-requests.bottom-drawer.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im2.constants :as constants]
    [status-im2.contexts.chat.messages.contact-requests.bottom-drawer.style :as style]
    [status-im2.contexts.shell.jump-to.constants :as jump-to.constants]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  [contact-id contact-request-state group-chat]
  (let [customization-color (rf/sub [:profile/customization-color])
        [primary-name _]    (rf/sub [:contacts/contact-two-names-by-identity contact-id])]
    [rn/view {:style style/container}
     [quo/permission-context
      [quo/button
       {:type      :ghost
        :size      24
        :on-press  #(rf/dispatch [:chat.ui/show-profile contact-id])
        :icon-left (if (= contact-request-state constants/contact-request-state-sent)
                     :i/pending-state
                     :i/add-user)}
       (cond
         group-chat
         (i18n/label :t/group-chat-not-member)

         (or (not contact-request-state)
             (= contact-request-state
                constants/contact-request-state-none))
         (i18n/label :t/contact-request-chat-add {:name primary-name})

         (= contact-request-state
            constants/contact-request-state-received)
         (str primary-name " sent you a contact request")

         (= contact-request-state
            constants/contact-request-state-sent)
         (i18n/label :t/contact-request-chat-pending))]]
     [quo/floating-shell-button
      {:jump-to
       {:on-press            (fn []
                               (rf/dispatch [:shell/navigate-to-jump-to]))
        :customization-color customization-color
        :label               (i18n/label :t/jump-to)}}
      {:position :absolute
       :top      (- jump-to.constants/floating-shell-button-height)}]]))
