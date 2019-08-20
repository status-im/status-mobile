(ns status-im.data-store.mailservers
  (:require [cljs.tools.reader.edn :as edn]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]))

(re-frame/reg-cofx
 :data-store/get-all-mailservers
 (fn [cofx _]
   []))

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

(re-frame/reg-cofx
 :data-store/all-gaps
 (fn [cofx _]
   (assoc cofx
          :data-store/all-gaps
          {})))

(defn save-mailserver-requests-gap
  [gap])

(defn delete-mailserver-requests-gaps
  [ids])

(defn delete-all-gaps-by-chat
  [chat-id])

(defn delete-range
  [chat-id])
