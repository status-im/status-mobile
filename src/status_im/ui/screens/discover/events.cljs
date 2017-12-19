(ns status-im.ui.screens.discover.events
  (:require [re-frame.core :refer [after dispatch enrich]]
            [status-im.utils.utils :refer [first-index]]
            [status-im.utils.handlers :refer [register-handler get-hashtags]]
            [status-im.protocol.core :as protocol]
            [status-im.ui.screens.navigation :as nav]
            [status-im.data-store.discover :as discoveries]
            [status-im.utils.handlers :as u]
            [status-im.utils.datetime :as time]
            [status-im.utils.random :as random]
            [taoensso.timbre :as log]
            [status-im.utils.handlers :as handlers]))

(def request-discoveries-interval-s 600)

(register-handler :init-discoveries
  (fn [db _]
    (-> db
        (assoc :tags [])
        (assoc :discoveries {}))))

(defmethod nav/preload-data! :discover
  [db _]
  (-> db
      (assoc-in [:toolbar-search :show] nil)
      (assoc :tags (discoveries/get-all-tags))
      (assoc :discoveries (->> (discoveries/get-all :desc)
                               (map (fn [{:keys [message-id] :as discover}]
                                      [message-id discover]))
                               (into {})))))

;; todo(goranjovic): at the moment we do nothing when a status without hashtags is posted
;; but we probably should post a special "delete" status that removes any previous
;; hashtag statuses in that scenario. In any case, that's the reason why this event
;; gets even the statuses without a hashtag - it may need to do stuff with them as well.
(register-handler :broadcast-status
  (u/side-effect!
    (fn [{:keys [current-public-key web3]
          :accounts/keys [accounts current-account-id]
          :contacts/keys [contacts]}
         [_ status]]
      (if-let [hashtags (seq (handlers/get-hashtags status))]
        (let [{:keys [name photo-path]} (get accounts current-account-id)
              message-id (random/id)
              message    {:message-id message-id
                          :from       current-public-key
                          :payload    {:message-id message-id
                                       :status     status
                                       :hashtags   (vec hashtags)
                                       :profile    {:name          name
                                                    :profile-image photo-path}}}]
          (doseq [id (u/identities contacts)]
            (protocol/send-status!
              {:web3    web3
               :message (assoc message :to id)}))
          (dispatch [:status-received message]))))))

(register-handler :status-received
  (u/side-effect!
    (fn [{:keys [discoveries]} [_ {:keys [from payload]}]]
      (when (and (not (discoveries/exists? (:message-id payload)))
                 (not (get discoveries (:message-id payload))))
        (let [{:keys [message-id status hashtags profile]} payload
              {:keys [name profile-image]} profile
              discover {:message-id   message-id
                         :name         name
                         :photo-path   profile-image
                         :status       status
                         :whisper-id   from
                         :tags         (map #(hash-map :name %) hashtags)
                         :created-at   (time/now-ms)}]
          (dispatch [:add-discover discover]))))))

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
    (fn [{:keys [current-public-key web3]
          :contacts/keys [contacts]}]
      (doseq [id (u/identities contacts)]
        (when-not (protocol/message-pending? web3 :discoveries-request id)
          (protocol/send-discoveries-request!
            {:web3    web3
             :message {:from       current-public-key
                       :to         id
                       :message-id (random/id)}}))))))

(register-handler :discoveries-send-portions
  (u/side-effect!
    (fn [{:keys [current-public-key web3]
          :contacts/keys [contacts]} [_ to]]
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
    (fn [{:keys [discoveries]
          :contacts/keys [contacts]} [_ {:keys [payload from]}]]
      (when (get contacts from)
        (when-let [data (:data payload)]
          (doseq [{:keys [message-id] :as discover} data]
            (when (and (not (discoveries/exists? message-id))
                       (not (get discoveries message-id)))
              (let [discover (assoc discover :created-at (time/now-ms))]
                (dispatch [:add-discover discover])))))))))

(defn add-discover
  [db [_ discover]]
  (assoc db :new-discover discover))

(defn save-discover!
  [{:keys [new-discover]} _]
  (discoveries/save new-discover))

(defn reload-tags!
  [db _]
  (assoc db :tags (discoveries/get-all-tags)
            :discoveries (->> (discoveries/get-all :desc)
                              (map (fn [{:keys [message-id] :as discover}]
                                     [message-id discover]))
                              (into {}))))

(register-handler :add-discover
  (u/handlers->
    add-discover
    save-discover!
    reload-tags!))

(register-handler :remove-old-discoveries!
  (u/side-effect!
    (fn [_ _]
      (discoveries/delete :created-at :asc 1000 200))))

(handlers/register-handler-fx
  :show-status-author-profile
  (fn [{db :db} [_ identity]]
    {:db       (assoc db :contacts/identity identity)
     :dispatch [:navigate-to :profile]}))
