(ns status-im.contact.core
  (:require
   [re-frame.core :as re-frame]
   [status-im.multiaccounts.model :as multiaccounts.model]
   [status-im.transport.filters.core :as transport.filters]
   [status-im.contact.db :as contact.db]
   [status-im.ethereum.core :as ethereum]
   [status-im.data-store.contacts :as contacts-store]
   [status-im.mailserver.core :as mailserver]
   [status-im.transport.message.contact :as message.contact]
   [status-im.transport.message.protocol :as protocol]
   [status-im.tribute-to-talk.db :as tribute-to-talk]
   [status-im.tribute-to-talk.whitelist :as whitelist]
   [status-im.ui.screens.navigation :as navigation]
   [status-im.utils.config :as config]
   [status-im.utils.fx :as fx]))

(fx/defn load-contacts
  {:events [::contacts-loaded]}
  [{:keys [db] :as cofx} all-contacts]
  (let [contacts-list (map #(vector (:public-key %) %) all-contacts)
        contacts (into {} contacts-list)
        tr-to-talk-enabled? (-> db tribute-to-talk/get-settings tribute-to-talk/enabled?)]
    (fx/merge cofx
              {:db (cond-> (-> db
                               (update :contacts/contacts #(merge contacts %))
                               (assoc :contacts/blocked (contact.db/get-blocked-contacts all-contacts)))
                     tr-to-talk-enabled?
                     (assoc :contacts/whitelist (whitelist/get-contact-whitelist all-contacts)))}
              ;; TODO: This is currently called twice, once we load chats & when we load filters.
              ;; For now leaving as it is as the next step is not to have this being called from status-react
              ;; as both contacts & chats are in status-go, but we still need to signals the filters to
              ;; status-react for mailsevers/gaps, so will address separately
              (transport.filters/load-filters))))

(defn build-contact
  [{{:keys [chats multiaccount]
     :contacts/keys [contacts]} :db} public-key]
  (cond-> (contact.db/public-key->contact contacts public-key)
    (= public-key (:public-key multiaccount))
    (assoc :name (:name multiaccount))))

(defn- own-info
  [db]
  (let [{:keys [name preferred-name photo-path address]} (:multiaccount db)]
    {:name          (or preferred-name name)
     :profile-image photo-path
     :address       address}))

(fx/defn upsert-contact
  [{:keys [db] :as cofx}
   {:keys [public-key] :as contact}]
  (fx/merge cofx
            {:db            (-> db
                                (update-in [:contacts/contacts public-key] merge contact))}
            (transport.filters/load-contact contact)
            (contacts-store/save-contact contact)))

(fx/defn send-contact-request
  [{:keys [db] :as cofx} {:keys [public-key] :as contact}]
  (if (contact.db/pending? contact)
    (protocol/send (message.contact/map->ContactRequest (own-info db)) public-key cofx)
    (protocol/send (message.contact/map->ContactRequestConfirmed (own-info db)) public-key cofx)))

(fx/defn add-contact
  "Add a contact and set pending to false"
  [{:keys [db] :as cofx} public-key]
  (when (not= (get-in db [:multiaccount :public-key]) public-key)
    (let [contact (-> (get-in db [:contacts/contacts public-key]
                              (build-contact cofx public-key))
                      (update :system-tags
                              (fnil #(conj % :contact/added) #{})))]
      (fx/merge cofx
                {:db (assoc-in db [:contacts/new-identity] "")}
                (upsert-contact contact)
                (whitelist/add-to-whitelist public-key)
                (send-contact-request contact)
                (mailserver/process-next-messages-request)))))

(fx/defn remove-contact
  "Remove a contact from current account's contact list"
  {:events [:contact.ui/remove-contact-pressed]}
  [{:keys [db] :as cofx} {:keys [public-key] :as contact}]
  (let [new-contact (update contact
                            :system-tags
                            (fnil #(disj % :contact/added) #{}))]
    (fx/merge cofx
              {:db (assoc-in db [:contacts/contacts public-key] new-contact)}
              (contacts-store/save-contact new-contact))))

(fx/defn create-contact
  "Create entry in contacts"
  [{:keys [db] :as cofx} public-key]
  (when (not= (get-in db [:multiaccount :public-key]) public-key)
    (let [contact (build-contact cofx public-key)]
      (fx/merge cofx
                {:db (assoc-in db [:contacts/new-identity] "")}
                (upsert-contact contact)))))

(fx/defn handle-contact-update
  [{{:contacts/keys [contacts] :as db} :db :as cofx}
   public-key
   timestamp
   {:keys [name profile-image address] :as m}]
  ;; We need to convert to timestamp ms as before we were using now in ms to
  ;; set last updated
  ;; Using whisper timestamp mostly works but breaks in a few scenarios:
  ;; 2 updates sent in the same second
  ;; when using multi-device & clocks are out of sync
  ;; Using logical clocks is probably the correct way to handle it, but an overkill
  ;; for now
  (let [timestamp-ms       (* timestamp 1000)
        prev-last-updated  (get-in db [:contacts/contacts public-key :last-updated])
        current-public-key (multiaccounts.model/current-public-key cofx)]
    (when (and (not= current-public-key public-key)
               (< prev-last-updated timestamp-ms))
      (let [contact          (get contacts public-key)

            ;; Backward compatibility with <= 0.9.21, as they don't send
            ;; address in contact updates
            contact-props
            (cond-> {:public-key   public-key
                     :photo-path   profile-image
                     :name         name
                     :address      (or address
                                       (:address contact)
                                       (ethereum/public-key->address public-key))
                     :last-updated timestamp-ms
                     :system-tags  (conj (get contact :system-tags #{})
                                         :contact/request-received)})]
        (upsert-contact cofx contact-props)))))

(fx/defn initialize-contacts [cofx]
  (contacts-store/fetch-contacts-rpc cofx #(re-frame/dispatch [::contacts-loaded %])))

(fx/defn open-contact-toggle-list
  [{:keys [db :as cofx]}]
  (fx/merge cofx
            {:db (assoc db
                        :group/selected-contacts #{}
                        :new-chat-name "")}
            (navigation/navigate-to-cofx :contact-toggle-list nil)))

(fx/defn set-tribute
  [{:keys [db] :as cofx} public-key tribute-to-talk]
  (let [contact (-> (or (build-contact cofx public-key)
                        (get-in db [:contacts/contacts public-key]))
                    (assoc :tribute-to-talk (or tribute-to-talk
                                                {:disabled? true})))]
    {:db (assoc-in db [:contacts/contacts public-key] contact)
     :insert-identicons [[public-key [:contacts/contacts public-key :identicon]]]
     :insert-gfycats    [[public-key [:contacts/contacts public-key :name]]
                         [public-key [:contacts/contacts public-key :alias]]]}))

(defn add-ens-names [contacts names]
  (reduce-kv (fn [acc public-key-keyword result]
               (let [verified   (:verified result)
                     error      (:error result)
                     ens-name   (:name result)
                     ens-verified-at (:verifiedAt result)
                     public-key (str "0x" (name public-key-keyword))
                     contact    (contact.db/public-key->contact contacts public-key)]

                 (if error
                   (assoc acc public-key contact)
                   (assoc acc public-key
                          (assoc contact
                                 ;; setting the name for now as ens-verification is not enabled because of geth 1.9 upgrade
                                 :name ens-name
                                 :ens-verified-at ens-verified-at
                                 :ens-verified verified)))))
             (or contacts {})
             names))

(fx/defn names-verified
  {:events [:contacts/ens-names-verified]}
  [{:keys [db]} names]
  {:db (update db :contacts/contacts add-ens-names names)})
