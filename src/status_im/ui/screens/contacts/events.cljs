(ns status-im.ui.screens.contacts.events
  (:require [clojure.set :as set]
            [cljs.reader :as reader]
            [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.contacts :as utils.contacts] 
            [status-im.constants :as constants]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.gfycat.core :as gfycat.core]
            [status-im.ui.screens.contacts.navigation]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.ui.screens.group.events :as group.events]
            [status-im.chat.console :as console-chat]
            [status-im.chat.events :as chat.events]
            [status-im.chat.models :as chat.models] 
            [status-im.transport.message.core :as transport]
            [status-im.transport.message.v1.contact :as message.v1.contact]
            [status-im.ui.screens.add-new.new-chat.db :as new-chat.db]))

;;;; Handlers

(defn- update-contact [{:keys [whisper-identity] :as contact} {:keys [db]}]
  (when (get-in db [:contacts/contacts whisper-identity])
    {:db                      (update-in db [:contacts/contacts whisper-identity] merge contact)
     :data-store/save-contact contact}))

(handlers/register-handler-fx
  :load-contacts
  [(re-frame/inject-cofx :data-store/get-all-contacts)]
  (fn [{:keys [db all-contacts]} _]
    (let [contacts-list (map #(vector (:whisper-identity %) %) all-contacts)
          contacts (into {} contacts-list)]
      {:db (update db :contacts/contacts #(merge contacts %))})))

(defn- add-new-contact [{:keys [whisper-identity] :as contact} {:keys [db]}]
  (let [new-contact (assoc contact :pending? false)]
    {:db                      (-> db
                                  (update-in [:contacts/contacts whisper-identity] merge new-contact)
                                  (assoc-in [:contacts/new-identity] ""))
     :data-store/save-contact new-contact}))

(defn- own-info [{:accounts/keys [accounts current-account-id] :as db}]
  (let [{:keys [name photo-path address]} (get accounts current-account-id)
        fcm-token (get-in db [:notifications :fcm-token])]
    {:name          name
     :profile-image photo-path
     :address       address
     :fcm-token     fcm-token}))

(defn send-contact-request [{:keys [whisper-identity pending? dapp?] :as contact} {:keys [db] :as cofx}]
  (when-not dapp?
    (if pending?
      (transport/send (message.v1.contact/map->ContactRequestConfirmed (own-info db)) whisper-identity cofx)
      (transport/send (message.v1.contact/map->ContactRequest (own-info db)) whisper-identity cofx))))

(defn- build-contact [whisper-id {{:keys [chats] :contacts/keys [contacts]} :db}]
  (-> (if-let [contact-info (get-in chats [whisper-id :contact-info])]
        (reader/read-string contact-info)
        (or (get contacts whisper-id)
            (utils.contacts/whisper-id->new-contact whisper-id)))
      (assoc :address (utils.contacts/public-key->address whisper-id))))

(defn add-contact [whisper-id {:keys [db] :as cofx}]
  (let [contact (build-contact whisper-id cofx)]
    (handlers/merge-fx cofx
                       (add-new-contact contact)
                       (send-contact-request contact))))

(defn add-contact-and-open-chat [whisper-id cofx]
  (handlers/merge-fx cofx
                     (navigation/navigate-to-clean :home)
                     (add-contact whisper-id)
                     (chat.events/start-chat whisper-id {})))

(handlers/register-handler-fx
  :add-contact
  [(re-frame/inject-cofx :random-id)]
  (fn [cofx [_ whisper-id]]
    (add-contact whisper-id cofx)))

(handlers/register-handler-fx
  :set-contact-identity-from-qr
  [(re-frame/inject-cofx :random-id)]
  (fn [{{:accounts/keys [accounts current-account-id] :as db} :db :as cofx} [_ _ contact-identity]]
    (let [current-account (get accounts current-account-id)
          fx              {:db (assoc db :contacts/new-identity contact-identity)}]
      (if (new-chat.db/validate-pub-key contact-identity current-account)
        fx
        (handlers/merge-fx cofx
                           fx
                           (add-contact-and-open-chat contact-identity))))))

(handlers/register-handler-fx
  :hide-contact
  (fn [cofx [_ {:keys [whisper-identity] :as contact}]]
    (update-contact {:whisper-identity whisper-identity
                     :pending?         true}
                    cofx)))

;;used only by status-dev-cli
(handlers/register-handler-fx
  :remove-contact
  (fn [{:keys [db]} [_ whisper-identity]]
    (when-let [contact (get-in db [:contacts/contacts whisper-identity])]
      {:db                        (update db :contacts/contacts dissoc whisper-identity)
       :data-store/delete-contact contact})))

(handlers/register-handler-db
  :open-contact-toggle-list
  (fn [db [_ group-type]]
    (-> (assoc db
               :group/group-type group-type
               :group/selected-contacts #{}
               :new-chat-name "")
        (navigation/navigate-to :contact-toggle-list))))

(handlers/register-handler-fx
  :open-chat-with-contact
  [(re-frame/inject-cofx :random-id)]
  (fn [{:keys [db] :as cofx} [_ {:keys [whisper-identity] :as contact}]]
    (handlers/merge-fx cofx
                       (navigation/navigate-to-clean :home)
                       (add-contact whisper-identity)
                       (chat.events/start-chat whisper-identity {}))))

(handlers/register-handler-fx
  :add-contact-handler
  [(re-frame/inject-cofx :random-id)]
  (fn [{{:contacts/keys [new-identity] :as db} :db :as cofx} _]
    (when (seq new-identity)
      (add-contact-and-open-chat new-identity cofx))))
