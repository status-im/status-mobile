(ns status-im.tribute-to-talk.whitelist
  (:require [status-im.contact.db :as contact.db]
            [status-im.data-store.contacts :as contacts-store]
            [status-im.tribute-to-talk.db :as tribute-to-talk.db]
            [status-im.utils.fx :as fx]))

(defn whitelisted-by? [{:keys [system-tags]}]
  (or (contains? system-tags :contact/request-received)
      (contains? system-tags :tribute-to-talk/paid)
      (contains? system-tags :tribute-to-talk/received)))

(defn whitelisted? [{:keys [system-tags]}]
  (or (contains? system-tags :contact/added)
      (contains? system-tags :tribute-to-talk/paid)
      (contains? system-tags :tribute-to-talk/received)))

(defn get-contact-whitelist
  [contacts]
  (reduce (fn [acc {:keys [public-key] :as contact}]
            (if (whitelisted? contact)
              (conj acc public-key) acc))
          (hash-set) contacts))

(fx/defn add-to-whitelist
  "Add contact to whitelist"
  [{:keys [db]} public-key]
  {:db (update db :contacts/whitelist (fnil conj #{}) public-key)})

(defn- mark-tribute
  [{:keys [db now] :as cofx} public-key tag]
  (let [contact (-> (contact.db/public-key->contact
                     (:contacts/contacts db)
                     public-key)
                    (assoc :last-updated now)
                    (update :system-tags conj tag))]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:contacts/contacts public-key] contact))
               :data-store/tx [(contacts-store/save-contact-tx contact)]}
              (add-to-whitelist public-key))))

(fx/defn mark-tribute-paid
  [cofx public-key]
  (mark-tribute cofx public-key :tribute-to-talk/paid))

(fx/defn mark-tribute-received
  [cofx public-key]
  (mark-tribute cofx public-key :tribute-to-talk/received))

(fx/defn enable-whitelist
  [{:keys [db] :as cofx}]
  (if (tribute-to-talk.db/enabled? (tribute-to-talk.db/get-settings db))
    {:db (assoc db :contacts/whitelist
                (get-contact-whitelist (vals (:contacts/contacts db))))}
    {:db (dissoc db :contacts/whitelist)}))

(fx/defn filter-message
  "clojure semantics of filter, if true the message is allowed
  if it is a user message and tribute to talk is enabled, the user must be
  in the whitelist or there must be a valid tribute transaction id passed
  along the message"
  [{:keys [db] :as cofx} received-message-fx message-type tribute-transaction from]
  ;; if it is not a user-message or the user is whitelisted it passes
  (if (or (not= :user-message message-type)
          (contains? (:contacts/whitelist db) from))
    received-message-fx
    ;; if ttt is disabled it passes
    (if-let [snt-amount (:snt-amount (tribute-to-talk.db/get-settings db))]
      (let [contact (get-in db [:contacts/contacts from])]
        ;; if the tribute is not paid the message is dropped
        ;; otherwise it passes and the user is added to the whitelist
        ;; through system tags
        (when (tribute-to-talk.db/valid-tribute-transaction?
               db snt-amount tribute-transaction from)
          (fx/merge cofx
                    received-message-fx
                    (mark-tribute-received from))))
      received-message-fx)))
