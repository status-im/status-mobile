(ns status-im2.contexts.chat.messages.contact-requests.bottom-drawer
  (:require
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [quo2.core :as quo]
    [quo2.components.drawers.permission-context.view :as permission-context]
    [status-im2.constants :as constants]))

(defn view
  [contact-id contact-request-state group-chat]
  (let [names (rf/sub [:contacts/contact-two-names-by-identity contact-id])]
    [permission-context/view
     [quo/button
      {:type      :ghost
       :on-press  #(rf/dispatch [:chat.ui/show-profile contact-id])
       :icon-left :i/communities}
      (cond
        group-chat
        (i18n/label :t/group-chat-not-member)

        (or (not contact-request-state)
            (= contact-request-state
               constants/contact-request-state-none)
            (= contact-request-state
               constants/contact-request-state-received))
        (i18n/label :t/contact-request-chat-add {:name (first names)})
        (= contact-request-state
           constants/contact-request-state-sent)

        (i18n/label :t/contact-request-chat-pending))]]))
