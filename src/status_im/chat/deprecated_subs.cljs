(ns status-im.chat.deprecated-subs
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.commands.input :as commands.input]
            [status-im.chat.constants :as chat.constants]
            [status-im.chat.db :as chat.db]
            [status-im.models.transactions :as transactions]
            [status-im.utils.platform :as platform]
            [status-im.chat.subs :as chat.subs]))

(re-frame/reg-sub :chats/current-chat-id :current-chat-id)

(re-frame/reg-sub
 :chats/chat
 :<- [:chats/active-chats]
 (fn [chats [_ chat-id]]
   (get chats chat-id)))

(re-frame/reg-sub
 :chats/current-chat-contact
 :<- [:contacts/contacts]
 :<- [:chats/current-chat-id]
 (fn [[contacts chat-id]]
   (get contacts chat-id)))

(re-frame/reg-sub
 :chats/current-chat-name
 :<- [:chats/current-chat-contact]
 :<- [:chats/current-chat]
 (fn [[contact chat]]
   (chat.db/chat-name chat contact)))

(re-frame/reg-sub
 :chats/chat-name
 :<- [:contacts/contacts]
 :<- [::chat.subs/chats]
 (fn [[contacts chats] [_ chat-id]]
   (chat.db/chat-name (get chats chat-id) (get contacts chat-id))))

(re-frame/reg-sub
 :chats/current-chat
 :<- [:chats/current]
 (fn [current-chat]
   current-chat))

(re-frame/reg-sub
 :chats/current-chat-message
 :<- [:chats/current-chat]
 (fn [{:keys [messages]} [_ message-id]]
   (get messages message-id)))

(re-frame/reg-sub
 :chats/current-chat-messages-stream
 :<- [:chats/current]
 (fn [{:keys [messages]}]
   messages))

(re-frame/reg-sub
 :chats/selected-chat-command
 :<- [:chats/selected-command]
 (fn [selected-command]
   selected-command))

(re-frame/reg-sub
 :chats/unviewed-messages-count
 (fn [[_ chat-id]]
   (re-frame/subscribe [:chats/chat chat-id]))
 (fn [{:keys [unviewed-messages]}]
   (count unviewed-messages)))

(re-frame/reg-sub
 :chats/photo-path
 (fn [[_ chat-id]]
   (re-frame/subscribe [:chats/chat chat-id]))
 (fn [{:keys [photo-path]}]
   photo-path))

(re-frame/reg-sub
 :chats/last-message
 (fn [[_ chat-id]]
   (re-frame/subscribe [:chats/chat chat-id]))
 (fn [{:keys [last-message]}]
   last-message))
