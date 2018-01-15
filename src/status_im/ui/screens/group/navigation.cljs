(ns status-im.ui.screens.group.navigation
  (:require [status-im.ui.screens.navigation :as nav]))

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
  [db [_ _ {:keys [show-search?]}]]
  (-> db
      (update :toolbar-search
              assoc
              :show (when show-search? :contact-list)
              :text "")))

(defmethod nav/preload-data! :reorder-groups
  [db [_ _]]
  (assoc db :group/groups-order (->> (vals (:group/contact-groups db))
                                     (remove :pending?)
                                     (sort-by :order >)
                                     (map :group-id))))
