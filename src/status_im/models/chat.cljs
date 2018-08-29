(ns status-im.models.chat
  (:require [clojure.set :as set]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.models.message :as models.message]
            [status-im.data-store.contacts :as contacts-store]
            [status-im.data-store.user-statuses :as user-statuses-store]
            [status-im.utils.contacts :as utils.contacts]
            [status-im.utils.handlers-macro :as handlers-macro]))

(def index-messages (partial into {} (map (juxt :message-id identity))))

(defn- add-default-contacts
  [{:keys [db default-contacts] :as cofx}]
  (let [new-contacts      (-> {}
                              (into (map (fn [[id props]]
                                           (let [contact-id (name id)]
                                             [contact-id {:whisper-identity contact-id
                                                          :address          (utils.contacts/public-key->address contact-id)
                                                          :name             (-> props :name :en)
                                                          :photo-path       (:photo-path props)
                                                          :public-key       (:public-key props)
                                                          :unremovable?     (-> props :unremovable? boolean)
                                                          :hide-contact?    (-> props :hide-contact? boolean)
                                                          :pending?         (-> props :pending? boolean)
                                                          :dapp?            (:dapp? props)
                                                          :dapp-url         (-> props :dapp-url :en)
                                                          :bot-url          (:bot-url props)
                                                          :description      (:description props)}])))
                                    default-contacts))
        existing-contacts (:contacts/contacts db)
        contacts-to-add   (select-keys new-contacts (set/difference (set (keys new-contacts))
                                                                    (set (keys existing-contacts))))]
    {:db            (update db :contacts/contacts merge contacts-to-add)
     :data-store/tx [(contacts-store/save-contacts-tx (vals contacts-to-add))]}))

(defn- group-chat-messages
  [{:keys [db]}]
  (reduce-kv (fn [fx chat-id {:keys [messages]}]
               (models.message/group-messages chat-id (vals messages) fx))
             {:db db}
             (:chats db)))

(defn initialize-chats [{:keys [db
                                default-dapps
                                all-stored-chats
                                stored-unanswered-requests
                                get-stored-messages
                                get-stored-user-statuses
                                stored-unviewed-messages
                                stored-message-ids] :as cofx}]
  (let [chat->message-id->request (reduce (fn [acc {:keys [chat-id message-id] :as request}]
                                            (assoc-in acc [chat-id message-id] request))
                                          {}
                                          stored-unanswered-requests)
        chats (reduce (fn [acc {:keys [chat-id] :as chat}]
                        (let [chat-messages (index-messages (get-stored-messages chat-id))
                              message-ids   (keys chat-messages)
                              unviewed-ids  (get stored-unviewed-messages chat-id)]
                          (assoc acc chat-id
                                 (assoc chat
                                        :unviewed-messages unviewed-ids
                                        :requests (get chat->message-id->request chat-id)
                                        :messages chat-messages
                                        :message-statuses (get-stored-user-statuses chat-id message-ids)
                                        :not-loaded-message-ids (set/difference (get stored-message-ids chat-id)
                                                                                (set message-ids))))))
                      {}
                      all-stored-chats)]
    (handlers-macro/merge-fx cofx
                             {:db (assoc db
                                         :chats          chats
                                         :contacts/dapps default-dapps)}
                             (group-chat-messages)
                             (add-default-contacts)
                             (commands/index-commands commands/register))))

(defn process-pending-messages
  "Change status of own messages which are still in `sending` status to `not-sent`
  (If signal from status-go has not been received)"
  [{:keys [db]}]
  (let [me               (:current-public-key db)
        pending-statuses (->> (vals (:chats db))
                              (mapcat :message-statuses)
                              (mapcat (fn [[_ user-id->status]]
                                        (filter (comp (partial = :sending) :status)
                                                (get user-id->status me)))))
        updated-statuses (map #(assoc % :status :not-sent) pending-statuses)]
    {:data-store/tx [(user-statuses-store/save-statuses-tx updated-statuses)]
     :db            (reduce
                     (fn [acc {:keys [chat-id message-id status whisper-identity]}]
                       (assoc-in acc
                                 [:chats chat-id :message-status message-id
                                  whisper-identity :status]
                                 status))
                     db
                     updated-statuses)}))
