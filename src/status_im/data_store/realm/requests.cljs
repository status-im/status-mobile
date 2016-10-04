(ns status-im.data-store.realm.requests
  (:require [status-im.data-store.realm.core :as realm]))

(defn get-all
  []
  (realm/get-all @realm/account-realm :request))

(defn get-all-as-list
  []
  (-> (get-all)
      realm/realm-collection->list))

(defn get-open-by-chat-id
  [chat-id]
  (-> (realm/get-by-fields @realm/account-realm :request :and [[:chat-id chat-id]
                                                               [:status "open"]])
      (realm/sorted :added :desc)
      (realm/realm-collection->list)))

(defn save
  [request]
  (realm/save @realm/account-realm :request request true))

(defn save-all
  [requests]
  (realm/save-all @realm/account-realm :request requests true))

(defn- get-by-message-id
  [chat-id message-id]
  (-> (realm/get-by-fields @realm/account-realm :request :and [[:chat-id chat-id]
                                                               [:message-id message-id]])
      (realm/single)))

(defn mark-as-answered
  [chat-id message-id]
  (realm/write @realm/account-realm
               (fn []
                 (-> (get-by-message-id chat-id message-id)
                     (.-status)
                     (set! "answered")))))
