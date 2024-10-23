(ns status-im.contexts.chat.group.update.view
  (:require
    [status-im.contexts.chat.group.common.group-edit :as group-edit]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [close]}]
  (let [chat-id                         (rf/sub [:get-screen-params :screen/group-update])
        {:keys [chat-name color image]} (rf/sub [:chats/chat-by-id chat-id])
        contacts                        (rf/sub [:contacts/contacts-by-chat chat-id])]
    [group-edit/view
     {:default-group-name  chat-name
      :default-group-color color
      :default-group-image image
      :contacts            contacts
      :submit-button-label (i18n/label :t/update-group-chat)
      :submit-event        :group-chat/edit
      :back-button-icon    :i/close
      :chat-id             chat-id
      :close               close
      :on-success          close
      :editing-group?      true}]))
