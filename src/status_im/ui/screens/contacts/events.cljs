(ns status-im.ui.screens.contacts.events
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.contacts :as utils.contacts]
            [status-im.chat.events :as chat.events]
            [status-im.transport.message.core :as transport]
            [status-im.transport.message.v1.contact :as message.v1.contact]
            [status-im.ui.screens.add-new.new-chat.db :as new-chat.db]
            [status-im.ui.screens.contacts.default-dapps :as default-dapps]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.data-store.contacts :as contacts-store]
            [status-im.utils.js-resources :as js-res]))

(re-frame/reg-cofx
 :get-default-contacts
 (fn [coeffects _]
   (assoc coeffects :default-contacts js-res/default-contacts)))

(re-frame/reg-cofx
 :get-default-dapps
 (fn [coeffects _]
   (assoc coeffects :default-dapps default-dapps/all)))

;;;; Handlers

(defn- update-contact [{:keys [whisper-identity] :as contact} {:keys [db]}]
  (when (get-in db [:contacts/contacts whisper-identity])
    {:db            (update-in db [:contacts/contacts whisper-identity] merge contact)
     :data-store/tx [(contacts-store/save-contact-tx contact)]}))

(handlers/register-handler-fx
 :load-contacts
 [(re-frame/inject-cofx :data-store/get-all-contacts)]
 (fn [{:keys [db all-contacts]} _]
   (let [contacts-list (map #(vector (:whisper-identity %) %) all-contacts)
         contacts (into {} contacts-list)]
     {:db (update db :contacts/contacts #(merge contacts %))})))

(defn- add-new-contact [{:keys [whisper-identity] :as contact} {:keys [db]}]
  (let [new-contact (assoc contact
                           :pending? false
                           :public-key whisper-identity)]
    {:db            (-> db
                        (update-in [:contacts/contacts whisper-identity]
                                   merge new-contact)
                        (assoc-in [:contacts/new-identity] ""))
     :data-store/tx [(contacts-store/save-contact-tx new-contact)]}))

(defn- own-info [db]
  (let [{:keys [name photo-path address]} (:account/account db)
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
  (assoc (or (get contacts whisper-id)
             (utils.contacts/whisper-id->new-contact whisper-id))
         :address (utils.contacts/public-key->address whisper-id)))

(defn add-contact [whisper-id {:keys [db] :as cofx}]
  (let [contact (build-contact whisper-id cofx)]
    (handlers-macro/merge-fx cofx
                             (add-new-contact contact)
                             (send-contact-request contact))))

(defn add-contact-and-open-chat [whisper-id cofx]
  (handlers-macro/merge-fx cofx
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
 (fn [{:keys [db] :as cofx} [_ _ contact-identity]]
   (let [current-account (:account/account db)
         fx              {:db (assoc db :contacts/new-identity contact-identity)}]
     (if (new-chat.db/validate-pub-key contact-identity current-account)
       fx
       (handlers-macro/merge-fx cofx
                                fx
                                (add-contact-and-open-chat contact-identity))))))

(handlers/register-handler-db
 :open-contact-toggle-list
 (fn [db _]
   (-> (assoc db
              :group/selected-contacts #{}
              :new-chat-name "")
       (navigation/navigate-to :contact-toggle-list))))

(handlers/register-handler-fx
 :open-chat-with-contact
 [(re-frame/inject-cofx :random-id)]
 (fn [cofx [_ {:keys [whisper-identity]}]]
   (handlers-macro/merge-fx cofx
                            (navigation/navigate-to-clean :home)
                            (add-contact whisper-identity)
                            (chat.events/start-chat whisper-identity {}))))

(handlers/register-handler-fx
 :add-contact-handler
 [(re-frame/inject-cofx :random-id)]
 (fn [{{:contacts/keys [new-identity]} :db :as cofx} _]
   (when (seq new-identity)
     (add-contact-and-open-chat new-identity cofx))))
