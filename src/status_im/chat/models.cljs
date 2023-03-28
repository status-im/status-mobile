(ns status-im.chat.models
  (:require [utils.i18n :as i18n]
            [re-frame.core :as re-frame]
            [utils.re-frame :as rf]
            [taoensso.timbre :as log]
            [status-im.add-new.db :as new-public-chat.db]
            [status-im.data-store.chats :as chats-store]))

;; OLD

(rf/defn handle-public-chat-created
  {:events [::public-chat-created]}
  [{:keys [db]} chat-id response]
  {:db       (-> db
                 (assoc-in [:chats chat-id] (chats-store/<-rpc (first (:chats response))))
                 (update :chats-home-list conj chat-id))
   :dispatch [:chat/navigate-to-chat chat-id]})

(rf/defn create-public-chat-go
  [_ chat-id]
  {:json-rpc/call [{:method     "wakuext_createPublicChat"
                    :params     [{:id chat-id}]
                    :on-success #(re-frame/dispatch [::public-chat-created chat-id %])
                    :on-error   #(log/error "failed to create public chat" chat-id %)}]})

(rf/defn start-public-chat
  "Starts a new public chat"
  {:events [:chat.ui/start-public-chat]}
  [cofx topic]
  (if (new-public-chat.db/valid-topic? topic)
    (create-public-chat-go
     cofx
     topic)
    {:utils/show-popup {:title   (i18n/label :t/cant-open-public-chat)
                        :content (i18n/label :t/invalid-public-chat-topic)}}))
