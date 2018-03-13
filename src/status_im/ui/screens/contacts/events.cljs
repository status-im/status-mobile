(ns status-im.ui.screens.contacts.events
  (:require [re-frame.core :refer [dispatch trim-v reg-fx reg-cofx inject-cofx]]
            [status-im.utils.handlers :refer [register-handler-db register-handler-fx]]
            [status-im.data-store.contacts :as contacts]
            [clojure.string :as s]
            [status-im.utils.utils :refer [http-post]]
            [status-im.utils.random :as random]
            [taoensso.timbre :as log]
            [cljs.reader :refer [read-string]]
            [status-im.utils.js-resources :as js-res]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.js-dependencies :as dependencies]
            [status-im.utils.identicon :refer [identicon]]
            [status-im.utils.gfycat.core :refer [generate-gfy]]
            [status-im.i18n :refer [label]]
            [status-im.ui.screens.contacts.navigation]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.ui.screens.discover.events :as discover-events]
            [status-im.chat.console :as console-chat]
            [status-im.commands.events.loading :as loading-events]
            [status-im.transport.message.core :as transport]
            [status-im.transport.message.v1.contact :as transport-contact]
            [cljs.spec.alpha :as spec]
            [status-im.ui.screens.add-new.new-chat.db :as new-chat.db]
            [clojure.string :as string]))
;;;; COFX

(reg-cofx
  :get-all-contacts
  (fn [coeffects _]
    (assoc coeffects :all-contacts (contacts/get-all))))

(reg-cofx
  ::get-default-contacts-and-groups
  (fn [coeffects _]
    (assoc coeffects
           :default-contacts js-res/default-contacts
           :default-groups js-res/default-contact-groups)))

;;;; FX



(reg-fx
  :save-contact
  (fn [contact]
    (contacts/save contact)))

(reg-fx
  :save-all-contacts
  (fn [new-contacts]
    (contacts/save-all new-contacts)))

(reg-fx
  ::delete-contact
  (fn [contact]
    (contacts/delete contact)))

(defn- contact-name [contact]
  (->> contact
       ((juxt :givenName :middleName :familyName))
       (remove s/blank?)
       (s/join " ")))

;;;; Handlers

(register-handler-fx
  :update-contact!
  (fn [{:keys [db]} [_ {:keys [whisper-identity] :as contact}]]
    (when (get-in db [:contacts/contacts whisper-identity])
      {:db           (update-in db [:contacts/contacts whisper-identity] merge contact)
       :save-contact contact})))

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
      (subs (.sha3 dependencies/Web3.prototype normalized-key #js {:encoding "hex"}) 26))))

(defn- prepare-default-groups-events [groups default-groups]
  [[:add-contact-groups
    (for [[id {:keys [name contacts]}] default-groups
          :let [id' (clojure.core/name id)]
          :when (not (get groups id'))]
      {:group-id  id'
       :name      (:en name)
       :order     0
       :timestamp (random/timestamp)
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

(register-handler-fx
  :load-default-contacts!
  [(inject-cofx ::get-default-contacts-and-groups)]
  (fn [{:keys [db default-contacts default-groups]} _]
    (let [{:contacts/keys [contacts] :group/keys [contact-groups]} db]
      {:dispatch-n (concat
                    (prepare-default-groups-events contact-groups default-groups)
                    (prepare-default-contacts-events contacts default-contacts)
                    (prepare-add-chat-events contacts default-contacts)
                    (prepare-add-contacts-to-groups-events contacts default-contacts))})))

(register-handler-fx
  :load-contacts
  [(inject-cofx :get-all-contacts)]
  (fn [{:keys [db all-contacts]} _]
    (let [contacts-list (map #(vector (:whisper-identity %) %) all-contacts)
          contacts (into {} contacts-list)]
      {:db         (update db :contacts/contacts #(merge contacts %))})))

(register-handler-fx
  :add-contacts
  [(inject-cofx :get-local-storage-data)]
  (fn [{:keys [db] :as cofx} [_ new-contacts]]
    (let [{:contacts/keys [contacts]} db
          new-contacts' (->> new-contacts
                             (map #(update-pending-status contacts %))
                             ;; NOTE(oskarth): Overwriting default contacts here
                             ;;(remove #(identities (:whisper-identity %)))
                             (map #(vector (:whisper-identity %) %))
                             (into {}))
          fx            {:db                (update db :contacts/contacts merge new-contacts')
                         :save-all-contacts (vals new-contacts')}]
      (transduce (map second)
                 (completing (partial loading-events/load-commands (assoc cofx :db (:db fx))))
                 fx
                 new-contacts'))))

(register-handler-db
  :remove-contacts-click-handler
  (fn [db _]
    (dissoc db
            :contacts/click-handler
            :contacts/click-action)))

(defn- add-new-contact [db {:keys [whisper-identity] :as contact}]
  {:db          (-> db
                    (update-in [:contacts/contacts whisper-identity] merge contact)
                    (assoc-in [:contacts/new-identity] ""))
   :save-contact contact})

(defn- own-info [{:accounts/keys [accounts current-account-id] :as db}]
  (let [{:keys [name photo-path address]} (get accounts current-account-id)
        fcm-token (get-in db [:notifications :fcm-token])]
    {:name          name
     :profile-image photo-path
     :address       address
     :fcm-token     fcm-token}))

(defn- send-contact-request [{:keys [db] :as cofx} chat-id]
  (transport/send (transport-contact/map->ContactRequest (own-info db)) chat-id cofx))

(defn- send-contact-request-confirmation [{:keys [db] :as cofx} chat-id]
  (transport/send (transport-contact/map->ContactRequestConfirmed (own-info db)) chat-id cofx))

(register-handler-fx
  :add-new-contact-and-open-chat
  (fn [{:keys [db] :as cofx} [_ {:keys [whisper-identity] :as contact}]]
    (when-not (get-in db [:contacts/contacts whisper-identity])
      (let [contact (assoc contact :address (public-key->address whisper-identity))
            fx (-> (navigation/navigate-to-clean db :home)
                   (add-new-contact contact)
                   (assoc :dispatch [:start-chat whisper-identity {:navigation-replace? true}]))]
        (merge fx (send-contact-request (assoc cofx :db (:db fx)) whisper-identity))))))

(defn add-pending-contact [{:keys [db] :as cofx} chat-or-whisper-id]
  (let [{:keys [chats] :contacts/keys [contacts]} db
        contact (-> (if-let [contact-info (get-in chats [chat-or-whisper-id :contact-info])]
                      (read-string contact-info)
                      (get contacts chat-or-whisper-id))
                    (assoc :address  (public-key->address chat-or-whisper-id)
                           :pending? false))
        fx      (add-new-contact db contact)]
    (merge fx (send-contact-request-confirmation (assoc cofx :db (:db fx)) chat-or-whisper-id))))

(register-handler-fx
  :add-pending-contact
  (fn [cofx [_ chat-or-whisper-id]]
    (add-pending-contact cofx chat-or-whisper-id)))

(register-handler-fx
  :add-pending-contact-and-open-chat
  (fn [cofx [_ whisper-id]]
    (-> (add-pending-contact cofx whisper-id)
        (update :db #(navigation/navigate-to-clean % :home))
        (assoc :dispatch [:start-chat whisper-id {:navigation-replace? true}]))))

(register-handler-fx
  :set-contact-identity-from-qr
  (fn [{{:accounts/keys [accounts current-account-id] :as db} :db} [_ _ contact-identity]]
    (let [current-account (get accounts current-account-id)]
      (cond-> {:db (assoc db :contacts/new-identity contact-identity)}
        (not (new-chat.db/validate-pub-key contact-identity current-account))
        (assoc :dispatch [:add-contact-handler])))))

(register-handler-fx
  :contact-update-received
  (fn [{:keys [db]} [_ {:keys [from payload]}]]
    (let [{:keys [chats current-public-key]} db]
      (when (not= current-public-key from)
        (let [{:keys [content timestamp]} payload
              {:keys [status name profile-image]} (:profile content)
              prev-last-updated (get-in db [:contacts/contacts from :last-updated])]
          (when (<= prev-last-updated timestamp)
            (let [contact {:whisper-identity from
                           :name             name
                           :photo-path       profile-image
                           :status           status
                           :last-updated     timestamp}]
              {:dispatch-n (concat [[:update-contact! contact]]
                                   (when (chats from)
                                     [[:update-chat! {:chat-id from
                                                      :name    name}]]))})))))))

(register-handler-fx
  :update-keys-received
  (fn [{:keys [db]} [_ {:keys [from payload]}]]
    (let [{{:keys [public private]} :keypair
           timestamp                :timestamp} payload
          prev-last-updated (get-in db [:contacts/contacts from :keys-last-updated])]
      (when (<= prev-last-updated timestamp)
        (let [contact {:whisper-identity  from
                       :public-key        public
                       :private-key       private
                       :keys-last-updated timestamp}]
          {:dispatch [:update-contact! contact]})))))

(register-handler-fx
  :contact-online-received
  (fn [{:keys [db]} [_ {:keys                          [from]
                        {{:keys [timestamp]} :content} :payload}]]
    (let [prev-last-online (get-in db [:contacts/contacts from :last-online])]
      (when (and timestamp (< prev-last-online timestamp))
        {::reset-pending-messages from
         :dispatch                [:update-contact! {:whisper-identity from
                                                     :last-online      timestamp}]}))))

(register-handler-fx
  :hide-contact
  (fn [{:keys [db]} [_ {:keys [whisper-identity] :as contact}]]
    {::stop-watching-contact (merge
                              (select-keys db [:web3])
                              (select-keys contact [:whisper-identity]))
     :dispatch-n [[:update-contact! {:whisper-identity whisper-identity
                                     :pending?         true}]
                  [:account-update-keys]]}))

;;used only by status-dev-cli
(register-handler-fx
  :remove-contact
  (fn [{:keys [db]} [_ whisper-identity pred]]
    (let [contact (get-in db [:contacts/contacts whisper-identity])]
      (when (and contact (pred contact))
        {:db              (update db :contacts/contacts dissoc whisper-identity)
         ::delete-contact contact}))))

(register-handler-fx
  :open-contact-toggle-list
  (fn [{:keys [db]} [_ group-type]]
    {:db       (-> db
                   (assoc :group/group-type group-type
                          :group/selected-contacts #{}
                          :new-chat-name "")
                   (navigation/navigate-to :contact-toggle-list))}))

(register-handler-fx
  :open-chat-with-contact
  (fn [{:keys [db] :as cofx} [_ {:keys [whisper-identity dapp?] :as contact}]]
    (-> {:db         (navigation/navigate-to-clean db :home)
         :dispatch-n [[:start-chat whisper-identity]]}
        (cond-> (when-not dapp?)
          (as-> fx
              (merge fx (send-contact-request-confirmation (assoc cofx :db (:db fx)) whisper-identity)))))))

(register-handler-fx
  :add-contact-handler
  (fn [{:keys [db]}]
    (let [{:contacts/keys [new-identity]} db]
      (when (and new-identity (not (string/blank? new-identity)))
        {:dispatch (if (get-in db [:contacts/contacts new-identity])
                     [:add-pending-contact-and-open-chat new-identity]
                     [:add-new-contact-and-open-chat {:name             (generate-gfy new-identity)
                                                      :photo-path       (identicon new-identity)
                                                      :whisper-identity new-identity}])}))))
