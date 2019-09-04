(ns status-im.data-store.mailservers
  (:require [cljs.tools.reader.edn :as edn]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.utils.fx :as fx]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.data-store.realm.core :as core]))

(re-frame/reg-cofx
 :data-store/get-all-mailservers
 (fn [cofx _]
   (assoc cofx :data-store/mailservers (mapv #(-> %
                                                  (update :id keyword)
                                                  (update :fleet keyword))
                                             (-> @core/account-realm
                                                 (core/get-all :mailserver)
                                                 (core/all-clj :mailserver))))))

(defn mailserver-request-gaps->rpc [{:keys [chat-id] :as gap}]
  (-> gap
      (assoc :chatId chat-id)
      (dissoc :chat-id)))

(fx/defn load-gaps [cofx chat-id success-fn]
  {::json-rpc/call [{:method "mailservers_getMailserverRequestGaps"
                     :params [chat-id]
                     :on-success #(let [indexed-gaps (reduce (fn [acc {:keys [id] :as g}]
                                                               (assoc acc id g))
                                                             {}
                                                             %)]
                                    (success-fn chat-id indexed-gaps))
                     :on-failure #(log/error "failed to fetch gaps" %)}]})

(fx/defn save-gaps [cofx gaps]
  {::json-rpc/call [{:method "mailservers_addMailserverRequestGaps"
                     :params [(map mailserver-request-gaps->rpc gaps)]
                     :on-success #(log/info "saved gaps successfully")
                     :on-failure #(log/error "failed to save gap" %)}]})

(fx/defn delete-gaps [cofx ids]
  {::json-rpc/call [{:method "mailservers_deleteMailserverRequestGaps"
                     :params [ids]
                     :on-success #(log/info "deleted gaps successfully")
                     :on-failure #(log/error "failed to delete gap" %)}]})

(fx/defn delete-gaps-by-chat-id [cofx chat-id]
  {::json-rpc/call [{:method "mailservers_deleteMailserverRequestGapsByChatID"
                     :params [chat-id]
                     :on-success #(log/info "deleted gaps successfully")
                     :on-failure #(log/error "failed to delete gap" %)}]})

(defn save-tx
  "Returns tx function for saving a mailserver"
  [{:keys [id] :as mailserver}]
  (fn [realm]
    (core/create realm
                 :mailserver
                 mailserver
                 true)))

(defn delete-tx
  "Returns tx function for deleting a mailserver"
  [id]
  (fn [realm]
    (core/delete realm
                 (core/get-by-field realm :mailserver :id (name id)))))

(defn deserialize-mailserver-topic [serialized-mailserver-topic]
  (-> serialized-mailserver-topic
      (update :chat-ids edn/read-string)))

(re-frame/reg-cofx
 :data-store/mailserver-topics
 (fn [cofx _]
   (assoc cofx
          :data-store/mailserver-topics
          (reduce (fn [acc {:keys [topic] :as mailserver-topic}]
                    (assoc acc topic (deserialize-mailserver-topic mailserver-topic)))
                  {}
                  (-> @core/account-realm
                      (core/get-all :mailserver-topic)
                      (core/all-clj :mailserver-topic))))))

(defn save-mailserver-topic-tx
  "Returns tx function for saving mailserver topic"
  [{:keys [topic mailserver-topic]}]
  (fn [realm]
    (log/debug "saving mailserver-topic:" topic mailserver-topic)
    (core/create realm
                 :mailserver-topic
                 (-> mailserver-topic
                     (assoc :topic topic)
                     (update :chat-ids pr-str))
                 true)))

(defn delete-mailserver-topic-tx
  "Returns tx function for deleting mailserver topic"
  [topic]
  (fn [realm]
    (log/debug "deleting mailserver-topic:" topic)
    (let [mailserver-topic (.objectForPrimaryKey realm
                                                 "mailserver-topic"
                                                 topic)]
      (core/delete realm mailserver-topic))))

(defn save-chat-requests-range
  [chat-requests-range]
  (fn [realm]
    (log/debug "saving ranges" chat-requests-range)
    (core/create realm :chat-requests-range chat-requests-range true)))

(re-frame/reg-fx
 ::all-chat-requests-ranges
 (fn [on-success]
   (on-success (reduce (fn [acc {:keys [chat-id] :as range}]
                         (assoc acc chat-id range))
                       {}
                       (-> @core/account-realm
                           (core/get-all :chat-requests-range)
                           (core/all-clj :chat-requests-range))))))

(defn delete-range
  [chat-id]
  (fn [realm]
    (log/debug "deleting range" chat-id)
    (core/delete realm
                 (core/get-by-field realm :chat-requests-range :chat-id chat-id))))
