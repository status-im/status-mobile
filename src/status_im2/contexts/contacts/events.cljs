(ns status-im2.contexts.contacts.events
  (:require
   [utils.re-frame :as rf]
   [status-im.contact.db :as contact.db]
   [status-im.data-store.contacts :as contacts-store]))

(rf/defn load-contacts
  {:events [:contacts/contacts-loaded]}
  [{:keys [db] :as cofx} all-contacts]
  (let [contacts-list (map #(vector (:public-key %)
                                    (if (empty? (:address %))
                                      (dissoc % :address)
                                      %))
                           all-contacts)
        contacts      (into {} contacts-list)]
    {:db (cond-> (-> db
                     (update :contacts/contacts #(merge contacts %))
                     (assoc :contacts/blocked (contact.db/get-blocked-contacts all-contacts))))}))

(rf/defn initialize-contacts
  [cofx]
  (contacts-store/fetch-contacts-rpc cofx #(rf/dispatch [:contacts/contacts-loaded %])))