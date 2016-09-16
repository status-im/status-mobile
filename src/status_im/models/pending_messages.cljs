(ns status-im.models.pending-messages
  (:require [status-im.persistence.realm.core :as r]
            [re-frame.core :refer [dispatch]]
            [cljs.reader :refer [read-string]]
            [status-im.utils.random :refer [timestamp]]
            [clojure.string :refer [join split]]
            [clojure.walk :refer [stringify-keys keywordize-keys]]
            [status-im.constants :as c]
            [status-im.utils.types :refer [clj->json json->clj]]
            [status-im.commands.utils :refer [generate-hiccup]]
            [status-im.utils.logging :as log]))

(defn get-pending-messages []
  (let [collection (-> (r/get-by-fields :account :pending-message :or [[:status :sending]
                                                                       [:status :sent]
                                                                       [:status :failed]])
                       (r/sorted :timestamp :desc)
                       (r/realm-collection->list))]
    (->> collection
         (map (fn [{:keys [message-id] :as message}]
                (let [message (-> message
                                  (update :message json->clj)
                                  (update :identities json->clj))]
                  [message-id message])))
         (into {}))))

(defn upsert-pending-message!
  [message]
  (r/write :account
           (fn []
             (let [message (-> message
                               (update :message clj->json)
                               (update :identities clj->json))]
               (r/create :account :pending-message message true)))))

(defn remove-pending-message! [message-id]
  (r/write :account
           (fn []
             (r/delete :account (r/get-by-field :account :pending-message :message-id message-id)))))
