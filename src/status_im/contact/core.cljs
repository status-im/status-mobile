(ns status-im.contact.core
  (:require [re-frame.core :as re-frame]
            [status-im.contact.db :as contact.db]
            [status-im.data-store.contacts :as contacts-store]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.mailserver.core :as mailserver]
            [status-im.transport.filters.core :as transport.filters]
            [status-im.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]
            [clojure.string :as string]))

(fx/defn load-contacts
  {:events [::contacts-loaded]}
  [{:keys [db] :as cofx} all-contacts]
  (let [contacts-list (map #(vector (:public-key %) (if (empty? (:address %))
                                                      (dissoc % :address)
                                                      %))
                           all-contacts)
        contacts (into {} contacts-list)]
    {:db (cond-> (-> db
                     (update :contacts/contacts #(merge contacts %))
                     (assoc :contacts/blocked (contact.db/get-blocked-contacts all-contacts))))}))

(defn build-contact
  [{{:keys [multiaccount]
     :contacts/keys [contacts]} :db} public-key]
  (cond-> (contact.db/public-key->contact contacts public-key)
    (= public-key (:public-key multiaccount))
    (assoc :name (:name multiaccount))))

(defn- own-info
  [db]
  (let [{:keys [name preferred-name identicon address]} (:multiaccount db)]
    {:name          (or preferred-name name)
     :profile-image identicon
     :address       address}))

(fx/defn ensure-contacts
  [{:keys [db]} contacts]
  {:db (update db :contacts/contacts
               #(reduce (fn [acc {:keys [public-key] :as contact}]
                          (-> acc
                              (update public-key merge contact)
                              (assoc-in [public-key :nickname] (:nickname contact))))
                        %
                        contacts))
   :dispatch-n (map (fn [{:keys [public-key] :as contact}]
                      (when (contact.db/added? contact)
                        [:start-profile-chat public-key]))
                    contacts)})

(fx/defn upsert-contact
  [{:keys [db] :as cofx}
   {:keys [public-key] :as contact}]
  (fx/merge cofx
            {:db            (-> db
                                (update-in [:contacts/contacts public-key] merge contact))}
            (transport.filters/load-contact contact)
            #(contacts-store/save-contact % (get-in % [:db :contacts/contacts public-key]))))

(fx/defn send-contact-request
  [{:keys [db] :as cofx} {:keys [public-key] :as contact}]
  (let [{:keys [name profile-image]} (own-info db)]
    {::json-rpc/call [{:method (json-rpc/call-ext-method "sendContactUpdate")
                       :params [public-key name profile-image]
                       :on-success #(log/debug "contact request sent" public-key)}]}))

(fx/defn add-contact
  "Add a contact and set pending to false"
  {:events [:contact.ui/add-to-contact-pressed]
   :interceptors [(re-frame/inject-cofx :random-id-generator)]}
  [{:keys [db] :as cofx} public-key nickname]
  (when (not= (get-in db [:multiaccount :public-key]) public-key)
    (let [contact (cond-> (get-in db [:contacts/contacts public-key]
                                  (build-contact cofx public-key))
                    (and nickname (not (string/blank? nickname)))
                    (assoc :nickname nickname)
                    :else
                    (update :system-tags
                            (fnil #(conj % :contact/added) #{})))]
      (fx/merge cofx
                {:db (dissoc db :contacts/new-identity)
                 :dispatch [:start-profile-chat public-key]}
                (upsert-contact contact)
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
              {:db (assoc-in db [:contacts/contacts public-key] new-contact)
               :dispatch [:chat/remove-update-chat public-key]}
              (contacts-store/save-contact new-contact))))

(fx/defn create-contact
  "Create entry in contacts"
  [{:keys [db] :as cofx} public-key]
  (when (not= (get-in db [:multiaccount :public-key]) public-key)
    (let [contact (build-contact cofx public-key)]
      (log/info "create contact" contact)
      (fx/merge cofx
                {:db (dissoc db :contacts/new-identity)}
                (upsert-contact contact)))))

(fx/defn initialize-contacts [cofx]
  (contacts-store/fetch-contacts-rpc cofx #(re-frame/dispatch [::contacts-loaded %])))

(fx/defn open-contact-toggle-list
  {:events [:contact.ui/start-group-chat-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (assoc db
                        :group/selected-contacts #{}
                        :new-chat-name "")}
            (navigation/navigate-to-cofx :create-group-chat nil)))

(fx/defn name-verified
  {:events [:contacts/ens-name-verified]}
  [{:keys [db now] :as cofx} public-key ens-name]
  (let [contact (-> (get-in db [:contacts/contacts public-key]
                            (build-contact cofx public-key))
                    (assoc :name ens-name
                           :last-ens-clock-value now
                           :ens-verified-at now
                           :ens-verified true))]
    (upsert-contact cofx contact)))

(fx/defn update-nickname
  {:events [:contacts/update-nickname]}
  [{:keys [db] :as cofx} public-key nickname]
  (let [contact (-> (build-contact cofx public-key)
                    (merge (get-in db [:contacts/contacts public-key])))]
    (fx/merge cofx
              {:db (assoc-in db [:contacts/contacts public-key]
                             (if (string/blank? nickname)
                               (dissoc contact :nickname)
                               (assoc contact :nickname nickname)))}
              (upsert-contact {:public-key public-key})
              (navigation/navigate-back))))
