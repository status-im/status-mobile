(ns status-im.contexts.chat.group-create.events
  (:require [clojure.string :as string]
            [legacy.status-im.data-store.chats :as data-store.chats]
            [oops.core :as oops]
            [re-frame.core :as rf]
            [status-im.common.avatar-picture-picker.view :as avatar-picture-picker]))

(rf/reg-event-fx :group-chat/create
 (fn [{:keys [db]} [group-name color image]]
   (let [selected-contacts (:group/selected-contacts db)]
     {:json-rpc/call [{:method      "wakuext_createGroupChatWithMembers"
                       :params      [nil group-name (into [] selected-contacts)]
                       :js-response true
                       :on-success  (fn [response]
                                      (let [chat-id (-> (oops/oget response :chats)
                                                        first
                                                        (oops/oget :id))]
                                        (rf/dispatch [:chat-updated response])
                                        (rf/dispatch [:group-chat/edit
                                                      {:chat-id    chat-id
                                                       :group-name group-name
                                                       :color      color
                                                       :image      image}])))}]})))

(rf/reg-event-fx :group-chat/edit-success
 (fn [{:keys [db]} [{:keys [chat-id name color image]}]]
   (let [new-chat {:name name :color color :image image}]
     {:db (update-in db [:chats chat-id] #(merge % new-chat))})))

(rf/reg-event-fx :group-chat/edit
 (fn [_ [{:keys [chat-id group-name color image]}]]
   {:json-rpc/call [{:method      "chat_editChat"
                     :params      ["" chat-id group-name (name color)
                                   {:imagePath (when image (string/replace-first image #"file://" ""))
                                    :x         0
                                    :y         0
                                    :width     avatar-picture-picker/crop-size
                                    :height    avatar-picture-picker/crop-size}]
                     :js-response true
                     :on-success  #(rf/dispatch [:group-chat/edit-success
                                                 (data-store.chats/<-rpc-js %)])}]}))
