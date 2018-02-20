(ns status-im.ui.screens.group.events
  (:require [status-im.protocol.core :as protocol]
            [re-frame.core :refer [dispatch reg-fx reg-cofx inject-cofx]]
            [status-im.utils.handlers :refer [register-handler-db register-handler-fx]]
            [status-im.ui.components.styles :refer [default-chat-color]]
            [status-im.data-store.contact-groups :as groups]
            [clojure.string :as string]
            [status-im.utils.random :as random]
            [status-im.ui.screens.group.navigation]
            [status-im.utils.datetime :as datetime]
            [re-frame.core :as re-frame]))

;;;; COFX

(reg-cofx
  ::get-all-contact-groups
  (fn [coeffects _]
    (let [groups (->> (groups/get-all)
                      (map (fn [{:keys [group-id] :as group}]
                             [group-id group]))
                      (into {}))]
      (assoc coeffects :all-groups groups))))

;;;; FX

(reg-fx
  ::save-contact-group
  (fn [new-group]
    (groups/save new-group)))

(reg-fx
  ::save-contact-groups
  (fn [new-groups]
    (groups/save-all new-groups)))

(reg-fx
  ::save-contact-group-property
  (fn [[contact-group-id property-name value]]
    (groups/save-property contact-group-id property-name value)))

(reg-fx
  ::add-contacts-to-contact-group
  (fn [[contact-group-id selected-contacts]]
    (groups/add-contacts contact-group-id selected-contacts)))

;;;; Handlers

(register-handler-db
  :deselect-contact
  (fn [db [_ id]]
    (update db :group/selected-contacts disj id)))

(register-handler-db
  :select-contact
  (fn [db [_ id]]
    (update db :group/selected-contacts conj id)))

(register-handler-db
  :deselect-participant
  (fn [db [_ id]]
    (update db :selected-participants disj id)))

(register-handler-db
  :select-participant
  (fn [db [_ id]]
    (update db :selected-participants conj id)))

(register-handler-fx
  :create-new-contact-group
  [(re-frame/inject-cofx :now)]
  (fn [{{:group/keys [contact-groups selected-contacts] :as db} :db
        now :now} [_ group-name]]
    (let [selected-contacts' (mapv #(hash-map :identity %) selected-contacts)
          new-group {:group-id    (random/id)
                     :name        group-name
                     :order       (count contact-groups)
                     :timestamp   now
                     :contacts    selected-contacts'}]
      {:db (update db :group/contact-groups merge {(:group-id new-group) new-group})
       ::save-contact-group new-group})))

(register-handler-fx
  ::update-contact-group
  (fn [{:keys [db]} [_ new-group]]
    {:db (update db :group/contact-groups merge {(:group-id new-group) new-group})
     ::save-contact-group new-group}))

(defn update-pending-status [old-groups {:keys [group-id pending?] :as group}]
  (let [{old-pending :pending?
         :as         old-group} (get old-groups group-id)
        pending?' (if old-pending (and old-pending pending?) pending?)]
    (assoc group :pending? (boolean pending?'))))

(register-handler-fx
  :add-contact-groups
  (fn [{{:group/keys [contact-groups] :as db} :db} [_ new-groups]]
    (let [identities  (set (keys contact-groups))
          old-groups-count (count identities)
          new-groups' (->> new-groups
                           (map #(update-pending-status contact-groups %))
                           (remove #(identities (:group-id %)))
                           (map #(vector (:group-id %2) (assoc %2 :order %1)) (iterate inc old-groups-count))
                           (into {}))]
      {:db (update db :group/contact-groups merge new-groups')
       ::save-contact-groups (into [] (vals new-groups'))})))

(register-handler-fx
  :load-contact-groups
  [(inject-cofx ::get-all-contact-groups)]
  (fn [{:keys [db all-groups]} _]
    {:db (assoc db :group/contact-groups all-groups)}))

(defn move-item [v from to]
  (if (< from to)
    (concat (subvec v 0 from)
            (subvec v (inc from) (inc to))
            [(v from)]
            (subvec v (inc to)))
    (concat (subvec v 0 to)
            [(v from)]
            (subvec v to from)
            (subvec v (inc from)))))

(register-handler-db
  :change-contact-group-order
  (fn [{:group/keys [groups-order] :as db} [_ from to]]
    (if (>= to 0)
      (assoc db :group/groups-order (move-item (vec groups-order) from to))
      db)))

(register-handler-fx
  :save-contact-group-order
  (fn [{{:group/keys [contact-groups groups-order] :as db} :db} _]
    (let [new-groups (mapv #(assoc (contact-groups (second %)) :order (first %))
                            (map-indexed vector (reverse groups-order)))]
      {:db (update db :group/contact-groups merge (map #(vector (:group-id %) %) new-groups))
       ::save-contact-groups new-groups})))

(register-handler-fx
  :set-contact-group-name
  (fn [{{:keys [new-chat-name] :group/keys [contact-group-id] :as db} :db} _]
    {:db (assoc-in db [:group/contact-groups contact-group-id :name] new-chat-name)
     ::save-contact-group-property [contact-group-id :name new-chat-name]}))

(register-handler-fx
  :add-selected-contacts-to-group
  (fn [{{:group/keys [contact-groups contact-group-id selected-contacts] :as db} :db} _]
    (let [new-identities (mapv #(hash-map :identity %) selected-contacts)]
      {:db (update-in db [:group/contact-groups contact-group-id :contacts] #(into [] (set (concat % new-identities))))
       ::add-contacts-to-contact-group [contact-group-id selected-contacts]})))

(register-handler-fx
  :add-contacts-to-group
  (fn [{:keys [db]} [_ group-id contacts]]
    (let [new-identities (mapv #(hash-map :identity %) contacts)]
      (when (get-in db [:group/contact-groups group-id])
        {:db (update-in db [:group/contact-groups group-id :contacts] #(into [] (set (concat % new-identities))))
         ::add-contacts-to-contact-group [group-id contacts]}))))

(defn remove-contact-from-group [whisper-identity]
  (fn [contacts]
    (remove #(= whisper-identity (:identity %)) contacts)))

(register-handler-fx
  :remove-contact-from-group
  (fn [{:keys [db]} [_ whisper-identity group-id]]
    (let [{:group/keys [contact-groups]} db
          group' (update (contact-groups group-id) :contacts (remove-contact-from-group whisper-identity))]
      {:dispatch [::update-contact-group group']})))

(register-handler-fx
  :delete-contact-group
  (fn [{{:group/keys [contact-group-id] :as db} :db} _]
    {:db (assoc-in db [:group/contact-groups contact-group-id :pending?] true)
     ::save-contact-group-property [contact-group-id :pending? true]}))