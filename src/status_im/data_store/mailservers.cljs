(ns status-im.data-store.mailservers
  (:require [cljs.tools.reader.edn :as edn]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.utils.fx :as fx]
            [status-im.ethereum.json-rpc :as json-rpc]))

(re-frame/reg-cofx
 :data-store/get-all-mailservers
 (fn [cofx _]
   []))

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

(defn deserialize-mailserver-topic [serialized-mailserver-topic]
  (-> serialized-mailserver-topic
      (update :chat-ids edn/read-string)))

(defn save-mailserver-topic-tx
  "Returns tx function for saving mailserver topic"
  [{:keys [topic mailserver-topic]}])

(defn delete-mailserver-topic-tx
  "Returns tx function for deleting mailserver topic"
  [topic])

(defn save-chat-requests-range
  [chat-requests-range])

(re-frame/reg-fx
 ::all-chat-requests-ranges
 (fn [on-success]
   (on-success {})))

(defn delete-range
  [chat-id])
