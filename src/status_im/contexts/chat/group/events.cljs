(ns status-im.contexts.chat.group.events
  (:require [clojure.string :as string]
            [oops.core :as oops]
            [re-frame.core :as rf]
            [status-im.common.avatar-picture-picker.view :as avatar-picture-picker]
            [taoensso.timbre :as log]))

(rf/reg-event-fx :group-chat/create
 (fn [{:keys [db]} [{:keys [group-name group-color group-image]}]]
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
                                                      {:chat-id     chat-id
                                                       :group-name  group-name
                                                       :group-color group-color
                                                       :group-image group-image}])))}]})))

(rf/reg-event-fx :group-chat/edit
 (fn [_ [{:keys [chat-id group-name group-color group-image on-success]}]]
   {:json-rpc/call [{:method      "chat_editChat"
                     :params      ["" chat-id group-name (name group-color)
                                   {:imagePath (when group-image
                                                 (string/replace-first group-image #"file://" ""))
                                    :x         0
                                    :y         0
                                    :width     avatar-picture-picker/crop-size
                                    :height    avatar-picture-picker/crop-size}]
                     :js-response true
                     :on-error    #(log/error "failed to edit group" {:error %})
                     :on-success  (fn [response]
                                    (rf/dispatch [:chat-updated response true])
                                    (when on-success (on-success)))}]}))
