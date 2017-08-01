(ns status-im.group.navigation
  (:require [status-im.navigation.handlers :as nav]))

(defn clear-toolbar-search [db]
  (-> db
      (assoc-in [:toolbar-search :show] nil)
      (assoc-in [:toolbar-search :text] "")))

(defmethod nav/preload-data! :add-contacts-toggle-list
  [db _]
  (->
    (assoc db :group/selected-contacts #{})
    (clear-toolbar-search)))


(defmethod nav/preload-data! :add-participants-toggle-list
  [db _]
  (->
    (assoc db :selected-participants #{})
    (clear-toolbar-search)))

(defmethod nav/preload-data! :new-public-chat
  [db]
  (dissoc db :public-group-topic))

(defmethod nav/preload-data! :group-contacts
  [db [_ _ group-id show-search?]]
  (-> db
      (assoc :group/contact-group-id group-id)
      (update :toolbar-search
              assoc
              :show (when show-search? :contact-list)
              :text "")))

(defmethod nav/preload-data! :edit-contact-group
  [db [_ _ group group-type]]
  (if group
    (assoc db :group/contact-group-id (:group-id group)
              :group/group-type group-type
              :new-chat-name (:name group))
    db))

(defmethod nav/preload-data! :reorder-groups
  [db [_ _]]
  (assoc db :group/groups-order (->> (vals (:group/contact-groups db))
                                     (remove :pending?)
                                     (sort-by :order >)
                                     (map :group-id))))