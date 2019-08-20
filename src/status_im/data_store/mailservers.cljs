(ns status-im.data-store.mailservers
  (:require [cljs.tools.reader.edn :as edn]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]))

(defn save-tx
  "Returns tx function for saving a mailserver"
  [{:keys [id] :as mailserver}]
  (fn [realm]))

(defn delete-tx
  "Returns tx function for deleting a mailserver"
  [id]
  (fn [realm]))

(defn deserialize-mailserver-topic [serialized-mailserver-topic]
  (-> serialized-mailserver-topic
      (update :chat-ids edn/read-string)))

(defn save-mailserver-topic-tx
  "Returns tx function for saving mailserver topic"
  [{:keys [topic mailserver-topic]}]
  (fn [realm]))

(defn delete-mailserver-topic-tx
  "Returns tx function for deleting mailserver topic"
  [topic]
  (fn [realm]
    (log/debug "deleting mailserver-topic:" topic)))

(defn save-chat-requests-range
  [chat-requests-range]
  (fn [realm]))

(defn save-mailserver-requests-gap
  [gap]
  (fn [realm]
    (log/debug "saving gap" gap)))

(defn delete-mailserver-requests-gaps
  [ids]
  (fn [realm]
    (log/debug "deleting gaps" ids)))

(defn delete-all-gaps-by-chat
  [chat-id]
  (fn [realm]
    (log/debug "deleting all gaps for chat" chat-id)))

(defn delete-range
  [chat-id]
  (fn [realm]
    (log/debug "deleting range" chat-id)))

(re-frame/reg-cofx
 :data-store/all-chat-requests-ranges
 (fn [cofx _]
   cofx))

(re-frame/reg-cofx
 :data-store/all-gaps
 (fn [cofx _]
   (assoc cofx :data-store/all-gaps {})))

(re-frame/reg-cofx
 :data-store/get-all-mailservers
 (fn [cofx _]
   (assoc cofx :data-store/mailservers [])))

(re-frame/reg-cofx
 :data-store/mailserver-topics
 (fn [cofx _]
   (assoc cofx :data-store/mailserver-topics {})))
