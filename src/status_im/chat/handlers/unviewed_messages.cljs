(ns status-im.chat.handlers.unviewed-messages
  (:require [re-frame.core :refer [after enrich path dispatch]]
            [status-im.utils.handlers :refer [register-handler]]
            [status-im.persistence.realm.core :as realm]))

(defn delivered-messages []
  (-> (realm/get-by-fields
        :account :message
        {:delivery-status :delivered
         :outgoing        false})
      (realm/collection->map)))

(defn set-unviewed-messages [db]
  (let [messages (->> (::raw-unviewed-messages db)
                      (group-by :chat-id)
                      (map (fn [[id messages]]
                             [id {:messages-ids (map :msg-id messages)
                                  :count        (count messages)}]))
                      (into {}))]
    (-> db
        (assoc :unviewed-messages messages)
        (dissoc ::raw-unviewed-messages))))

(defn load-messages! [db]
  (let [messages (delivered-messages)]
    (assoc db ::raw-unviewed-messages messages)))

(register-handler ::set-unviewed-messages set-unviewed-messages)

(register-handler :load-unviewed-messages!
  (after #(dispatch [::set-unviewed-messages]))
  load-messages!)

(register-handler :add-unviewed-message
  (path :unviewed-messages)
  (fn [db [_ chat-id message-id]]
    (-> db
        (update-in [chat-id :messages-ids] conj message-id)
        (update-in [chat-id :count] inc))))

(register-handler :remove-unviewed-messages
  (path :unviewed-messages)
  (fn [db [_ chat-id]]
    (dissoc db chat-id)))
