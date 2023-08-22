(ns status-im2.contexts.chat.messages.contact-requests.bottom-drawer
  (:require
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [quo2.core :as quo]
    [status-im2.contexts.shell.jump-to.constants :as jump-to.constants]
    [quo2.components.drawers.permission-context.view :as permission-context]
    [status-im2.constants :as constants]
    [react-native.core :as rn]
    [status-im2.config :as config]))

(defn view
  [contact-id contact-request-state group-chat]
  (let [customization-color (rf/sub [:profile/customization-color])
        names               (rf/sub [:contacts/contact-two-names-by-identity contact-id])]
    [rn/view
     [permission-context/view
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
         (i18n/label :t/contact-request-chat-add {:name (first names)})

         (= contact-request-state
            constants/contact-request-state-received)
         (str (first names) " sent you a contact request")

         (= contact-request-state
            constants/contact-request-state-sent)
         (i18n/label :t/contact-request-chat-pending))]]
     [quo/floating-shell-button
      {:jump-to
       {:on-press            (fn []
                               (when config/shell-navigation-disabled?
                                 (rf/dispatch [:chat/close true]))
                               (rf/dispatch [:shell/navigate-to-jump-to]))
        :customization-color customization-color
        :label               (i18n/label :t/jump-to)}}
      {:position :absolute
       :top      (- jump-to.constants/floating-shell-button-height)}]]))
