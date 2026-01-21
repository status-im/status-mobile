(ns status-im.contexts.chat.group.events
  (:require [clojure.string :as string]
            [oops.core :as oops]
            [re-frame.core :as rf]
            [status-im.common.avatar-picture-picker.view :as avatar-picture-picker]
            [status-im.constants :as constants]
            [taoensso.timbre :as log]
            [utils.i18n :as i18n]))

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

(rf/reg-event-fx :group-chat/set-manage-members-error
 (fn [{:keys [db]} [{:keys [chat-id error?]}]]
   {:db (assoc-in db
         [:group-chat/manage-members-error chat-id]
         error?)
    :fx [(when error?
           [:dispatch
            [:toasts/upsert
             {:type :negative
              :text (i18n/label :t/new-group-limit
                                {:max-contacts
                                 constants/max-group-chat-participants})}]])]}))
