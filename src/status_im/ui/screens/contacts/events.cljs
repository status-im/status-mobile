(ns status-im.ui.screens.contacts.events
  (:require [cljs.reader :as reader]
            [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.contacts :as utils.contacts]
            [status-im.utils.random :as random]
            [status-im.utils.js-resources :as js-res]
            [status-im.i18n :refer [label]]
            [taoensso.timbre :as log]
            [status-im.utils.js-resources :as js-res]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.gfycat.core :as gfycat.core]
            [status-im.ui.screens.contacts.navigation]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.chat.console :as console-chat]
            [status-im.chat.events :as chat.events]
            [status-im.chat.models :as chat.models]
            [status-im.commands.events.loading :as loading-events]
            [status-im.js-dependencies :as js-dependencies]
            [status-im.transport.message.core :as transport]
            [status-im.transport.message.v1.contact :as message.v1.contact]
            [status-im.ui.screens.add-new.new-chat.db :as new-chat.db]))

;;;; COFX

(re-frame/reg-cofx
  ::get-default-contacts-and-groups
  (fn [coeffects _]
    (assoc coeffects
           :default-contacts js-res/default-contacts
           :default-groups js-res/default-contact-groups)))

;;;; Handlers

(defn- update-contact [{:keys [whisper-identity] :as contact} {:keys [db]}]
  (when (get-in db [:contacts/contacts whisper-identity])
    {:db                      (update-in db [:contacts/contacts whisper-identity] merge contact)
     :data-store/save-contact contact}))

(defn- update-pending-status [old-contacts {:keys [whisper-identity pending?] :as contact}]
  (let [{old-pending :pending?
         :as         old-contact} (get old-contacts whisper-identity)
        pending?' (if old-contact (and old-pending pending?) pending?)]
    (assoc contact :pending? (boolean pending?'))))

(defn- public-key->address [public-key]
  (let [length (count public-key)
        normalized-key (case length
                         132 (subs public-key 4)
                         130 (subs public-key 2)
                         128 public-key
                         nil)]
    (when normalized-key
      (subs (.sha3 js-dependencies/Web3.prototype normalized-key #js {:encoding "hex"}) 26))))

(defn- prepare-default-groups-events [groups default-groups]
  [[:add-contact-groups
    (for [[id {:keys [name contacts]}] default-groups
          :let [id' (clojure.core/name id)]
          :when (not (get groups id'))]
      {:group-id  id'
       :name      (:en name)
       :order     0
       :timestamp (datetime/timestamp)
       :contacts  (mapv #(hash-map :identity %) contacts)})]])

;; NOTE(oskarth): We now overwrite default contacts upon upgrade with default_contacts.json data.
(defn- prepare-default-contacts-events [contacts default-contacts]
  (let [default-contacts
        (for [[id {:keys [name photo-path public-key add-chat? pending? description
                          dapp? dapp-url dapp-hash bot-url unremovable? hide-contact?]}] default-contacts
              :let [id' (clojure.core/name id)]]
          {:whisper-identity id'
           :address          (public-key->address id')
           :name             (:en name)
           :photo-path       photo-path
           :public-key       public-key
           :unremovable?     (boolean unremovable?)
           :hide-contact?    (boolean hide-contact?)
           :pending?         pending?
           :dapp?            dapp?
           :dapp-url         (:en dapp-url)
           :bot-url          bot-url
           :description      description
           :dapp-hash        dapp-hash})
        all-default-contacts (conj default-contacts console-chat/contact)]
    [[:add-contacts all-default-contacts]]))

(defn- prepare-add-chat-events [contacts default-contacts]
  (for [[id {:keys [name add-chat?]}] default-contacts
        :let [id' (clojure.core/name id)]
        :when (and (not (get contacts id')) add-chat?)]
    [:add-chat id' {:name (:en name)}]))

(defn- prepare-add-contacts-to-groups-events [contacts default-contacts]
  (let [groups (for [[id {:keys [groups]}] default-contacts
                     :let [id' (clojure.core/name id)]
                     :when (and (not (get contacts id')) groups)]
                 (for [group groups]
                   {:group-id group :whisper-identity id'}))
        groups' (vals (group-by :group-id (flatten groups)))]
    (for [contacts groups']
      [:add-contacts-to-group
       (:group-id (first contacts))
       (mapv :whisper-identity contacts)])))

(handlers/register-handler-fx
  :load-default-contacts!
  [(re-frame/inject-cofx ::get-default-contacts-and-groups)]
  (fn [{:keys [db default-contacts default-groups]} _]
    (let [{:contacts/keys [contacts] :group/keys [contact-groups]} db]
      {:dispatch-n (concat
                    (prepare-default-groups-events contact-groups default-groups)
                    (prepare-default-contacts-events contacts default-contacts)
                    (prepare-add-chat-events contacts default-contacts)
                    (prepare-add-contacts-to-groups-events contacts default-contacts))})))

(handlers/register-handler-fx
  :load-contacts
  [(re-frame/inject-cofx :data-store/get-all-contacts)]
  (fn [{:keys [db all-contacts]} _]
    (let [contacts-list (map #(vector (:whisper-identity %) %) all-contacts)
          contacts (into {} contacts-list)]
      {:db (update db :contacts/contacts #(merge contacts %))})))

(handlers/register-handler-fx
  :add-contacts
  [(re-frame/inject-cofx :data-store/get-local-storage-data)]
  (fn [{:keys [db] :as cofx} [_ new-contacts]]
    (let [{:contacts/keys [contacts]} db
          new-contacts' (->> new-contacts
                             (map #(update-pending-status contacts %))
                             ;; NOTE(oskarth): Overwriting default contacts here
                             ;;(remove #(identities (:whisper-identity %)))
                             (map #(vector (:whisper-identity %) %))
                             (into {}))
          fx            {:db                       (update db :contacts/contacts merge new-contacts')
                         :data-store/save-contacts (vals new-contacts')}]
      (transduce (map second)
                 (completing (partial loading-events/load-commands (assoc cofx :db (:db fx))))
                 fx
                 new-contacts'))))

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
      (assoc :address (public-key->address whisper-id))))

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
  [(re-frame/inject-cofx :random-id) (re-frame/inject-cofx :data-store/get-chat)]
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
  [(re-frame/inject-cofx :random-id) (re-frame/inject-cofx :data-store/get-chat)]
  (fn [{{:contacts/keys [new-identity] :as db} :db :as cofx} _]
    (when (seq new-identity)
      (add-contact-and-open-chat new-identity cofx))))
