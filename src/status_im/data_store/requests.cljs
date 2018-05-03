(ns status-im.data-store.requests
  (:require [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]))

(re-frame/reg-cofx
  :data-store/get-unanswered-requests
  (fn [cofx _]
    (assoc cofx :stored-unanswered-requests (-> @core/account-realm
                                                (core/get-by-field :request :status "open")
                                                (core/all-clj :request)))))

(defn save-request-tx
  "Returns tx function for saving request"
  [request]
  (fn [realm]
    (core/create realm :request request true)))

(defn mark-request-as-answered-tx
  "Given chat-id and message-id, returns tx function for marking request as answered"
  [chat-id message-id]
  (fn [realm]
    (some-> (core/get-by-fields realm :request :and [[:chat-id chat-id]
                                                     [:message-id message-id]])
            core/single
            (aset "status" "answered"))))
