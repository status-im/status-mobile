(ns status-im.contacts.navigation
  (:require [status-im.navigation.handlers :as nav]))

(defmethod nav/preload-data! :group-contacts
  [db [_ _ group show-search?]]
  (-> db
      (assoc :contacts-group group)
      (update :toolbar-search assoc
              :show (when show-search? :contact-list)
              :text "")))

(defmethod nav/preload-data! :edit-group
  [db [_ _ group group-type]]
  (if group
    (assoc db :contact-group-id (:group-id group)
              :group-type group-type
              :new-chat-name (:name group))
    db))

(defmethod nav/preload-data! :contact-list
  [db [_ _ click-handler]]
  (-> db
      (assoc-in [:toolbar-search :show] nil)
      (assoc-in [:contacts/list-ui-props :edit?] false)
      (assoc-in [:contacts/ui-props :edit?] false)
      (assoc :contacts/click-handler click-handler)))

(defmethod nav/preload-data! :reorder-groups
  [db [_ _]]
  (assoc db :groups-order (->> (vals (:contact-groups db))
                               (remove :pending?)
                               (sort-by :order >)
                               (map :group-id))))