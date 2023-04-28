(ns status-im2.contexts.chat.actions.view
  (:require [quo2.core :as quo]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn new-chat
  []
  [quo/action-drawer
   [[{:icon                :i/new-message
      :accessibility-label :start-a-new-chat
      :label               (i18n/label :t/new-chat)
      :on-press            (fn []
                             (rf/dispatch [:group-chat/clear-contacts])
                             (rf/dispatch [:open-modal :start-a-new-chat]))}
     {:icon                :i/add-user
      :accessibility-label :add-a-contact
      :label               (i18n/label :t/add-a-contact)
      :sub-label           (i18n/label :t/enter-a-chat-key)
      :add-divider?        true
      :on-press            #(rf/dispatch [:open-modal :new-contact])}]]])
