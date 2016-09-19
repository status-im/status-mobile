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
            [cljs.reader :as reader]
            [clojure.string :as str]))

(defn get-pending-messages! []
  (->> (r/get-all :account :pending-message)
       r/realm-collection->list
       (map (fn [message]
              (update message :topics reader/read-string)))))

(defn- get-id
  [message-id to]
  (let [to' (if (and to (str/starts-with? to "0x"))
              (subs to 2)
              to)
        to'' (when to' (subs to' 0 7))
        id' (if to''
              (str message-id "-" (subs to'' 0 7))
              message-id)]
    id'))

(defn add-pending-message!
  [{:keys [id to group-id message] :as pending-message}]
  (r/write :account
           (fn []
             (let [{:keys [from topics payload]} message
                   id' (get-id id to)
                   chat-id (or group-id to)
                   message' (-> pending-message
                                (assoc :id id'
                                       :from from
                                       :message-id id
                                       :chat-id chat-id
                                       :payload payload
                                       :topics (prn-str topics))
                                (dissoc :message))]
               (r/create :account :pending-message message' true)))))

(defn remove-pending-message!
  [{{:keys [message-id]} :payload}]
  (r/write :account
           (fn []
             (r/delete :account (r/get-by-field :account :pending-message :message-id message-id)))))

(defn remove-all-by-chat [chat-id]
  (r/write
    :account
    (fn []
      (r/delete :account
                (r/get-by-field :account :pending-message :chat-id chat-id)))))
