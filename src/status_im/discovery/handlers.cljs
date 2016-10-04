(ns status-im.discovery.handlers
  (:require [re-frame.core :refer [after dispatch enrich]]
            [status-im.utils.utils :refer [first-index]]
            [status-im.utils.handlers :refer [register-handler]]
            [status-im.protocol.core :as protocol]
            [status-im.navigation.handlers :as nav]
            [status-im.data-store.discovery :as discoveries]
            [status-im.utils.handlers :as u]
            [status-im.utils.datetime :as time]
            [status-im.utils.random :as random]))

(register-handler :init-discoveries
  (fn [db _]
    (-> db
        (assoc :discoveries []))))

(defn calculate-priority [{:keys [chats contacts]} from payload]
  (let [contact (get contacts from)
        chat (get chats from)
        seen-online-recently? (< (- (time/now-ms) (get contact :last-online))
                                 time/hour)]
    (+ (time/now-ms)                                        ;; message is newer => priority is higher
       (if contact time/day 0)                              ;; user exists in contact list => increase priority
       (if chat time/day 0)                                 ;; chat with this user exists => increase priority
       (if seen-online-recently? time/hour 0)               ;; the user was online recently => increase priority
       )))

(defmethod nav/preload-data! :discovery
  [{:keys [discoveries] :as db} _]
  (-> db
      (assoc :tags (discoveries/get-all-tags))
      ;; todo add limit
      ;; todo hash-map with whisper-id as key and sorted by last-update
      ;; may be more efficient here
      (assoc :discoveries (discoveries/get-all))))

(register-handler :discovery-response-received
  (u/side-effect!
    (fn [{:keys [current-public-key] :as db} [_ {:keys [from payload]}]]
      (when-not (= current-public-key from)
        (let [{:keys [discovery-id profile hashtags]} payload
              {:keys [name profile-image status]} profile
              discovery {:message-id   discovery-id
                         :name         name
                         :photo-path   profile-image
                         :status       status
                         :whisper-id   from
                         :tags         (map #(hash-map :name %) hashtags)
                         :last-updated (js/Date.)
                         :priority     (calculate-priority db from payload)}]
          (dispatch [:add-discovery discovery]))))))

(register-handler :broadcast-status
  (u/side-effect!
    (fn [{:keys [current-public-key web3 current-account-id accounts]}
         [_ status hashtags]]
      (let [{:keys [name photo-path]} (get accounts current-account-id)]
        (protocol/broadcats-discoveries!
          {:web3     web3
           :hashtags (set hashtags)
           :message  {:from       current-public-key
                      :message-id (random/id)
                      :payload    {:status  status
                                   :profile {:name          name
                                             :status        status
                                             :profile-image photo-path}}}})
        (protocol/watch-hashtags! {:web3     web3
                                   :hashtags (set hashtags)
                                   :callback #(dispatch [:incoming-message %1 %2])})))))

(register-handler :show-discovery-tag
  (fn [db [_ tag]]
    (dispatch [:navigate-to :discovery-tag])
    (assoc db :current-tag tag)))

(defn add-discovery
  [{db-discoveries :discoveries
    :as            db} [_ {:keys [message-id] :as discovery}]]
  (let [updated-discoveries (if-let [i (first-index #(= (:message-id %) message-id) db-discoveries)]
                              (assoc db-discoveries i discovery)
                              (conj db-discoveries discovery))]
    (-> db
        (assoc :new-discovery discovery)
        (assoc :discoveries updated-discoveries))))

(defn save-discovery!
  [{:keys [new-discovery]} _]
  (discoveries/save new-discovery))

(defn reload-tags!
  [db _]
  (assoc db :tags (discoveries/get-all-tags)))

(register-handler :add-discovery
  (-> add-discovery
      ((after save-discovery!))
      ((enrich reload-tags!))))

(register-handler
  :remove-old-discoveries!
  (u/side-effect!
    (fn [_ _]
      (discoveries/delete :priority :asc 1000 200))))
