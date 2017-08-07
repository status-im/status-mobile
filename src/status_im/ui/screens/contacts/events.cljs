(ns status-im.ui.screens.contacts.events
  (:require [re-frame.core :refer [dispatch trim-v reg-fx reg-cofx inject-cofx]]
            [status-im.utils.handlers :refer [register-handler-db register-handler-fx]]
            [status-im.data-store.contacts :as contacts]
            [status-im.utils.crypt :refer [encrypt]]
            [clojure.string :as s]
            [status-im.protocol.core :as protocol]
            [status-im.utils.utils :refer [http-post]]
            [status-im.utils.phone-number :refer [format-phone-number]]
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
            [cljs.spec.alpha :as spec]))

;;;; COFX

(reg-cofx
  ::get-all-contacts
  (fn [coeffects _]
    (assoc coeffects :all-contacts (contacts/get-all))))

(reg-cofx
  ::get-default-contacts-and-groups
  (fn [coeffects _]
    (assoc coeffects :default-contacts js-res/default-contacts
                     :default-groups js-res/default-contact-groups)))

;;;; FX

(reg-fx
  ::watch-contact
  (fn [{:keys [web3 whisper-identity public-key private-key]}]
    (protocol/watch-user! {:web3     web3
                           :identity whisper-identity
                           :keypair  {:public  public-key
                                      :private private-key}
                           :callback #(dispatch [:incoming-message %1 %2])})))

(reg-fx
  ::stop-watching-contact
  (fn [{:keys [web3 whisper-identity]}]
    (protocol/stop-watching-user! {:web3     web3
                                   :identity whisper-identity})))

(reg-fx
  ::send-contact-request-fx
  (fn [{:keys [web3 current-public-key name whisper-identity
               photo-path current-account-id status fcm-token
               updates-public-key updates-private-key] :as params}]
    (protocol/contact-request!
      {:web3    web3
       :message {:from       current-public-key
                 :to         whisper-identity
                 :message-id (random/id)
                 :payload    {:contact {:name          name
                                        :profile-image photo-path
                                        :address       current-account-id
                                        :status        status
                                        :fcm-token     fcm-token}
                              :keypair {:public  updates-public-key
                                        :private updates-private-key}}}})))

(reg-fx
  ::reset-pending-messages
  (fn [from]
    (protocol/reset-pending-messages! from)))

(reg-fx
  ::save-contact
  (fn [contact]
    (contacts/save contact)))

(reg-fx
  ::save-contacts!
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

(defn- normalize-phone-contacts [contacts]
  (let [contacts' (js->clj contacts :keywordize-keys true)]
    (map (fn [{:keys [thumbnailPath phoneNumbers] :as contact}]
           {:name          (contact-name contact)
            :photo-path    thumbnailPath
            :phone-numbers phoneNumbers}) contacts')))

(reg-fx
  ::fetch-contacts-from-phone!
  (fn [on-contacts-event-creator]
    (.getAll rn-dependencies/contacts
             (fn [error contacts]
               (if error
                 (log/debug :error-on-fetching-loading error)
                 (let [contacts' (normalize-phone-contacts contacts)]
                   (dispatch [::get-contacts-identities contacts' on-contacts-event-creator])))))))

(defn- get-contacts-by-hash [contacts]
  (->> contacts
       (mapcat (fn [{:keys [phone-numbers] :as contact}]
                 (map (fn [{:keys [number]}]
                        (let [number' (format-phone-number number)]
                          [(encrypt number')
                           (-> contact
                               (assoc :phone-number number')
                               (dissoc :phone-numbers))]))
                      phone-numbers)))
       (into {})))

(defn- add-identity [contacts-by-hash contacts]
  (map (fn [{:keys [phone-number-hash whisper-identity address]}]
         (let [contact (contacts-by-hash phone-number-hash)]
           (assoc contact :whisper-identity whisper-identity
                          :address address)))
       (js->clj contacts)))

(reg-fx
  ::request-stored-contacts
  (fn [{:keys [contacts on-contacts-event-creator]}]
    (let [contacts-by-hash (get-contacts-by-hash contacts)
          data (or (keys contacts-by-hash) '())]
      (http-post "get-contacts" {:phone-number-hashes data}
                 (fn [{:keys [contacts]}]
                   (dispatch (on-contacts-event-creator (add-identity contacts-by-hash contacts))))))))

(reg-fx
  ::request-contacts-by-address
  (fn [id]
    (http-post "get-contacts-by-address" {:addresses [id]}
               (fn [{:keys [contacts]}]
                 (if (> (count contacts) 0)
                   (let [{:keys [whisper-identity]} (first contacts)
                         contact {:name             (generate-gfy whisper-identity)
                                  :address          id
                                  :photo-path       (identicon whisper-identity)
                                  :whisper-identity whisper-identity}]
                     (if (contacts/exists? whisper-identity)
                       (dispatch [:add-pending-contact whisper-identity])
                       (dispatch [:add-new-contact-and-open-chat contact])))
                   (dispatch [:set :contacts/new-public-key-error (label :t/unknown-address)]))))))

;;;; Handlers

(register-handler-fx
  ::get-contacts-identities
  [trim-v]
  (fn [_ [contacts on-contacts-event-creator]]
    {::request-stored-contacts {:contacts                   contacts
                                :on-contacts-event-creator  on-contacts-event-creator}}))

(register-handler-fx
  :sync-contacts
  [trim-v]
  (fn [_ [on-contacts-event-creator]]
    {::fetch-contacts-from-phone! on-contacts-event-creator}))

(register-handler-fx
  :watch-contact
  (fn [{:keys [db]} [_ {:keys [public-key private-key] :as contact}]]
    (when (and public-key private-key)
      {::watch-contact (merge
                         (select-keys db [:web3])
                         (select-keys contact [:whisper-identity :public-key :private-key]))})))

(register-handler-fx
  :update-contact!
  (fn [{:keys [db]} [_ {:keys [whisper-identity] :as contact}]]
    (when (get-in db [:contacts/contacts whisper-identity])
      {:db            (update-in db [:contacts/contacts whisper-identity] merge contact)
       ::save-contact contact})))

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

(defn- prepare-default-contacts-events [contacts default-contacts]
  [[:add-contacts
    (for [[id {:keys [name photo-path public-key add-chat?
                      dapp? dapp-url dapp-hash bot-url unremovable? mixable?]}] default-contacts
          :let [id' (clojure.core/name id)]
          :when (not (get contacts id'))]
      {:whisper-identity id'
       :address          (public-key->address id')
       :name             (:en name)
       :photo-path       photo-path
       :public-key       public-key
       :unremovable?     (boolean unremovable?)
       :mixable?         (boolean mixable?)
       :dapp?            dapp?
       :dapp-url         (:en dapp-url)
       :bot-url          bot-url
       :dapp-hash        dapp-hash})]])

(defn- prepare-add-chat-events [contacts default-contacts]
  (for [[id {:keys [name add-chat?]}] default-contacts
        :let [id' (clojure.core/name id)]
        :when (and (not (get contacts id')) add-chat?)]
    [:add-chat id' {:name (:en name)}]))

(defn- prepare-bot-commands-events [contacts default-contacts]
  (for [[id {:keys [bot-url]}] default-contacts
        :let [id' (clojure.core/name id)]
        :when bot-url]
    [:load-commands! id']))

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
                     (prepare-bot-commands-events contacts default-contacts)
                     (prepare-add-contacts-to-groups-events contacts default-contacts))})))

(register-handler-fx
  :load-contacts
  [(inject-cofx ::get-all-contacts)]
  (fn [{:keys [db all-contacts]} _]
    (let [contacts-list (map #(vector (:whisper-identity %) %) all-contacts)
          contacts (into {} contacts-list)]
      {:db         (assoc db :contacts/contacts contacts)
       :dispatch-n (mapv (fn [_ contact] [:watch-contact contact]) contacts)})))

(register-handler-fx
  :add-contacts
  (fn [{:keys [db]} [_ new-contacts]]
    (let [{:contacts/keys [contacts]} db
          identities (set (keys contacts))
          new-contacts' (->> new-contacts
                             (map #(update-pending-status contacts %))
                             (remove #(identities (:whisper-identity %)))
                             (map #(vector (:whisper-identity %) %))
                             (into {}))]
      {:db              (update db :contacts/contacts merge new-contacts')
       ::save-contacts! (vals new-contacts')})))

(register-handler-db
  :remove-contacts-click-handler
  (fn [db _]
    (dissoc db
            :contacts/click-handler
            :contacts/click-action)))

(register-handler-fx
  ::send-contact-request
  (fn [{{:accounts/keys [accounts current-account-id] :as db} :db} [_ contact]]
    (let [current-account (get accounts current-account-id)
          fcm-token (get-in db [:notifications :fcm-token])]
      {::send-contact-request-fx (merge
                                   (select-keys db [:current-public-key :web3])
                                   {:current-account-id current-account-id :fcm-token fcm-token}
                                   (select-keys contact [:whisper-identity])
                                   (select-keys current-account [:name :photo-path :status
                                                                 :updates-public-key :updates-private-key]))})))

(register-handler-fx
  ::add-new-contact
  (fn [{:keys [db]} [_ {:keys [whisper-identity] :as contact}]]
    {:db            (-> db
                        (update-in [:contacts/contacts whisper-identity] merge contact)
                        (assoc :contacts/new-identity ""))
     :dispatch      [::send-contact-request contact]
     ::save-contact contact}))

(register-handler-fx
  :add-new-contact-and-open-chat
  (fn [{:keys [db]} [_ {:keys [whisper-identity] :as contact}]]
    (when-not (get-in db [:contacts/contacts whisper-identity])
      (let [contact (assoc contact :address (public-key->address whisper-identity))]
        {:dispatch-n [[::add-new-contact contact]
                      [:start-chat whisper-identity {} :navigation-replace]]}))))

(register-handler-fx
  :add-pending-contact
  (fn [{:keys [db]} [_ chat-or-whisper-id]]
    (let [{:keys [chats] :contacts/keys [contacts]} db
          contact (if-let [contact-info (get-in chats [chat-or-whisper-id :contact-info])]
                    (read-string contact-info)
                    (get contacts chat-or-whisper-id))
          contact' (assoc contact :address (public-key->address chat-or-whisper-id)
                                  :pending? false)]
      {:dispatch-n [[::add-new-contact contact']
                    [:watch-contact contact']
                    [:discoveries-send-portions chat-or-whisper-id]]})))

(register-handler-db
  :set-contact-identity-from-qr
  (fn [db [_ _ contact-identity]]
    (assoc db :contacts/new-identity contact-identity)))

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
                   (assoc-in [:toolbar-search :show] nil)
                   (assoc-in [:toolbar-search :text] ""))
     :dispatch [:navigate-to :contact-toggle-list]}))

(register-handler-fx
  :open-chat-with-contact
  (fn [_ [_ {:keys [whisper-identity dapp?] :as contact}]]
    {:dispatch-n (concat
                   [[:navigate-to-clean :chat-list]
                    [:start-chat whisper-identity {}]]
                   (when-not dapp?
                     [[::send-contact-request contact]]))}))

(register-handler-fx
  :add-contact-handler
  (fn [{:keys [db]} [_ id]]
    (if (spec/valid? :global/address id)
      {::request-contacts-by-address id}
      {:dispatch (if (get-in db [:contacts/contacts id])
                   [:add-pending-contact id]
                   [:add-new-contact-and-open-chat {:name             (generate-gfy id)
                                                    :photo-path       (identicon id)
                                                    :whisper-identity id}])})))
