(ns status-im.models.requests
  (:require [status-im.persistence.realm.core :as r]))

(defn get-requests []
  (-> (r/get-all :account :request)
      r/collection->map))

(defn create-request [request]
  (r/create :account :request request true))

(defn save-request [request]
  (r/write :account
           (fn []
             (create-request request))))

(defn save-requests [requests]
  (r/write :account #(mapv create-request requests)))

(defn requests-list []
  (r/get-all :account :request))

