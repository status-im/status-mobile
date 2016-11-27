(ns status-im.discovery.handlers
  (:require [re-frame.core :refer [after dispatch enrich]]
            [status-im.utils.utils :refer [first-index]]
            [status-im.utils.handlers :refer [register-handler get-hashtags]]
            [status-im.protocol.core :as protocol]
            [status-im.navigation.handlers :as nav]
            [status-im.data-store.discovery :as discoveries]
            [status-im.utils.handlers :as u]
            [status-im.utils.datetime :as time]
            [status-im.utils.random :as random]))

(def request-discoveries-interval-s 600)

(register-handler :init-discoveries
  (fn [db _]
    (-> db
        (assoc :tags [])
        (assoc :discoveries {}))))

(defn identities [contacts]
  (->> (map second contacts)
       (remove (fn [{:keys [dapp? pending]}]
                 (or pending dapp?)))
       (map :whisper-identity)))

(defmethod nav/preload-data! :discovery
  [db _]
  (dispatch [:set :discovery-show-search? false])
  (-> db
      (assoc :tags (discoveries/get-all-tags))
      (assoc :discoveries (->> (discoveries/get-all :desc)
                               (map (fn [{:keys [message-id] :as discovery}]
                                      [message-id discovery]))
                               (into {})))))

(register-handler :broadcast-status
  (u/side-effect!
    (fn [{:keys [current-public-key web3 current-account-id accounts contacts]}
         [_ status hashtags]]
      (let [{:keys [name photo-path]} (get accounts current-account-id)
            message-id (random/id)
            message    {:message-id message-id
                        :from       current-public-key
                        :payload    {:message-id message-id
                                     :status     status
                                     :hashtags   (vec hashtags)
                                     :profile    {:name          name
                                                  :profile-image photo-path}}}]
        (doseq [id (identities contacts)]
          (protocol/send-status!
            {:web3    web3
             :message (assoc message :to id)}))
        (dispatch [:status-received message])))))

(register-handler :status-received
  (u/side-effect!
    (fn [{:keys [discoveries] :as db} [_ {:keys [from payload]}]]
      (when (and (not (discoveries/exists? (:message-id payload)))
                 (not (get discoveries (:message-id payload))))
        (let [{:keys [message-id status hashtags profile]} payload
              {:keys [name profile-image]} profile
              discovery {:message-id   message-id
                         :name         name
                         :photo-path   profile-image
                         :status       status
                         :whisper-id   from
                         :tags         (map #(hash-map :name %) hashtags)
                         :created-at   (time/now-ms)}]
          (dispatch [:add-discovery discovery]))))))

(register-handler :start-requesting-discoveries
  (fn [{:keys [request-discoveries-timer] :as db}]
    (when request-discoveries-timer
      (js/clearInterval request-discoveries-timer))
    (dispatch [:request-discoveries])
    (assoc db :request-discoveries-timer
              (js/setInterval #(dispatch [:request-discoveries])
                              (* request-discoveries-interval-s 1000)))))

(register-handler :request-discoveries
  (u/side-effect!
    (fn [{:keys [current-public-key web3 contacts]}]
      (doseq [id (identities contacts)]
        (when-not (protocol/message-pending? web3 :discoveries-request id)
          (protocol/send-discoveries-request!
            {:web3    web3
             :message {:from       current-public-key
                       :to         id
                       :message-id (random/id)}}))))))

(register-handler :discoveries-send-portions
  (u/side-effect!
    (fn [{:keys [current-public-key contacts web3]} [_ to]]
      (when (get contacts to)
        (protocol/send-discoveries-response!
          {:web3        web3
           :discoveries (discoveries/get-all :asc)
           :message     {:from current-public-key
                         :to   to}})))))

(register-handler :discoveries-request-received
  (u/side-effect!
    (fn [_ [_ {:keys [from]}]]
      (dispatch [:discoveries-send-portions from]))))

(register-handler :discoveries-response-received
  (u/side-effect!
    (fn [{:keys [discoveries contacts]} [_ {:keys [payload from]}]]
      (when (get contacts from)
        (when-let [data (:data payload)]
          (doseq [{:keys [message-id] :as discovery} data]
            (when (and (not (discoveries/exists? message-id))
                       (not (get discoveries message-id)))
              (let [discovery (assoc discovery :created-at (time/now-ms))]
                (dispatch [:add-discovery discovery])))))))))

(defn add-discovery
  [db [_ discovery]]
  (assoc db :new-discovery discovery))

(defn save-discovery!
  [{:keys [new-discovery]} _]
  (discoveries/save new-discovery))

(defn reload-tags!
  [db _]
  (assoc db :tags (discoveries/get-all-tags)
            :discoveries (->> (discoveries/get-all :desc)
                              (map (fn [{:keys [message-id] :as discovery}]
                                     [message-id discovery]))
                              (into {}))))

(register-handler :add-discovery
  (-> add-discovery
      ((after save-discovery!))
      ((enrich reload-tags!))))

(register-handler
  :remove-old-discoveries!
  (u/side-effect!
    (fn [_ _]
      (discoveries/delete :created-at :asc 1000 200))))
