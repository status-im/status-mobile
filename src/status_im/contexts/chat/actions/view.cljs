(ns status-im.contexts.chat.actions.view
  (:require
    [quo.core :as quo]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn fetch-messages
  [chat-id]
  {:icon                :i/download
   :right-icon          :i/chevron-right
   :accessibility-label :chat-fetch-messages
   :on-press            (fn []
                          (rf/dispatch [:hide-bottom-sheet])
                          (rf/dispatch [:chat/fetch-messages chat-id]))
   :label               (i18n/label :t/fetch-messages)})

(defn new-chat
  []
  [quo/action-drawer
   [[{:icon                :i/new-message
      :accessibility-label :start-a-new-chat
      :label               (i18n/label :t/new-chat)
      :on-press            (fn []
                             (rf/dispatch [:group-chat/clear-contacts])
                             (debounce/throttle-and-dispatch
                              [:open-modal :screen/start-a-new-chat]
                              1000))}
     {:icon                :i/add-user
      :accessibility-label :add-a-contact
      :label               (i18n/label :t/add-a-contact)
      :sub-label           (i18n/label :t/enter-chat-key)
      :add-divider?        true
      :on-press            #(debounce/throttle-and-dispatch
                             [:open-modal :screen/new-contact]
                             1000)}]]])
