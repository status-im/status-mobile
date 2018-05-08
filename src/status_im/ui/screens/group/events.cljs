(ns status-im.ui.screens.group.events
  (:require [clojure.set :as set]
            [re-frame.core :as re-frame]
            [status-im.utils.js-resources :as js-res]
            [status-im.utils.handlers :as handlers]
            [status-im.ui.screens.group.navigation]))

;;;; COFX

(re-frame/reg-cofx
 :get-default-contacts-and-groups
 (fn [coeffects _]
   (assoc coeffects
          :default-contacts js-res/default-contacts
          :default-groups js-res/default-contact-groups)))

;;;; Handlers

(handlers/register-handler-db
 :deselect-contact
 (fn [db [_ id]]
   (update db :group/selected-contacts disj id)))

(handlers/register-handler-db
 :select-contact
 (fn [db [_ id]]
   (update db :group/selected-contacts conj id)))

(handlers/register-handler-db
 :deselect-participant
 (fn [db [_ id]]
   (update db :selected-participants disj id)))

(handlers/register-handler-db
 :select-participant
 (fn [db [_ id]]
   (update db :selected-participants conj id)))

(handlers/register-handler-fx
 :create-new-contact-group
 [re-frame/trim-v (re-frame/inject-cofx :now) (re-frame/inject-cofx :random-id)]
 (fn [{{:group/keys [contact-groups selected-contacts] :as db} :db group-id :random-id now :now} [group-name]]
   (let [new-group {:group-id    group-id
                    :name        group-name
                    :order       (count contact-groups)
                    :timestamp   now
                    :contacts    selected-contacts}]
     {:db                            (assoc-in db [:group/contact-groups group-id] new-group)
      :data-store/save-contact-group new-group})))

(defn add-default-groups
  [{:keys [db now default-groups]}]
  (let [new-groups      (into {}
                              (map (fn [[id props]]
                                     (let [group-id (name id)]
                                       [group-id {:group-id  group-id
                                                  :name      (-> props :name :en)
                                                  :order     0
                                                  :timestamp now
                                                  :contacts  (:contacts props)}])))
                              default-groups)
        existing-groups (:group/contact-groups db)
        groups-to-add   (select-keys new-groups (set/difference (set (keys new-groups))
                                                                (set (keys existing-groups))))]
    {:db                             (update db :group/contact-groups merge groups-to-add)
     :data-store/save-contact-groups (vals groups-to-add)}))

(handlers/register-handler-fx
 :load-contact-groups
 [(re-frame/inject-cofx :data-store/get-all-contact-groups)]
 (fn [{:keys [db all-contact-groups]} _]
   {:db (assoc db :group/contact-groups all-contact-groups)}))

(handlers/register-handler-fx
 :set-contact-group-name
 (fn [{{:keys [new-chat-name] :group/keys [contact-group-id] :as db} :db} _]
   {:db                                     (assoc-in db
                                                      [:group/contact-groups contact-group-id :name]
                                                      new-chat-name)
    :data-store/save-contact-group-property [contact-group-id :name new-chat-name]}))

(handlers/register-handler-fx
 :add-selected-contacts-to-group
 (fn [{{:group/keys [contact-groups contact-group-id selected-contacts] :as db} :db} _]
   {:db                                       (update-in db
                                                         [:group/contact-groups contact-group-id :contacts]
                                                         #(into [] (set (concat % selected-contacts))))
    :data-store/add-contacts-to-contact-group [contact-group-id selected-contacts]}))

(handlers/register-handler-fx
 :remove-contact-from-group
 [re-frame/trim-v]
 (fn [{:keys [db]} [whisper-identity group-id]]
   (let [group (-> db
                   (get-in [:group/contact-groups group-id])
                   (update :contacts (partial remove #(= whisper-identity %))))]
     {:db                            (assoc-in db [:group/contact-groups group-id] group)
      :data-store/save-contact-group group})))
