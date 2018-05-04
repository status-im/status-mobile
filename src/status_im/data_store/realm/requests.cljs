(ns status-im.data-store.realm.requests
  (:require [status-im.data-store.realm.core :as realm]))

(defn get-all-unanswered
  []
  (-> @realm/account-realm
      (realm/get-by-field :request :status "open")
      (realm/all-clj :request)))

(defn save
  [request]
  (realm/save @realm/account-realm :request request true))

(defn- get-by-message-id
  [chat-id message-id]
  (-> @realm/account-realm
      (realm/get-by-fields :request :and [[:chat-id chat-id]
                                          [:message-id message-id]])
      realm/single))

(defn mark-as-answered
  [chat-id message-id]
  (realm/write @realm/account-realm
    (fn []
      (-> (get-by-message-id chat-id message-id)
          (.-status)
          (set! "answered")))))
