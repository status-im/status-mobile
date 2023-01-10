(ns status-im2.contexts.chat.messages.contact-requests.bottom-drawer
  (:require
   [i18n.i18n :as i18n]
   [utils.re-frame :as rf]
   [quo2.core :as quo]
   [quo2.components.drawers.permission-context.view :as permission-context]
   [status-im2.common.constants :as constants]))

(defn view
  [contact-id contact-request-state]
  (let [names (rf/sub [:contacts/contact-two-names-by-identity contact-id])]
    [permission-context/view
     [quo/button
      {:type     :ghost
       :on-press #(rf/dispatch [:chat.ui/show-profile contact-id])
       :before   :i/communities}
      (cond
        (or (= contact-request-state
               constants/contact-request-state-none)
            (= contact-request-state
               constants/contact-request-state-received))
        (i18n/label :t/contact-request-chat-add {:name (first names)})
        (= contact-request-state
           constants/contact-request-state-sent)

        (i18n/label :t/contact-request-chat-pending))]]))
