(ns syng-im.models.messages
  (:require [syng-im.persistence.realm :as r]
            [cljs.reader :refer [read-string]]
            [syng-im.utils.random :refer [timestamp]]
            [syng-im.db :as db]
            [syng-im.utils.logging :as log]
            [clojure.string :refer [join split]]
            [clojure.walk :refer [stringify-keys keywordize-keys]]
            [syng-im.constants :as c]))

(defn- map-to-str
  [m]
  (join ";" (map #(join "=" %) (stringify-keys m))))

(defn- str-to-map
  [s]
  (keywordize-keys (apply hash-map (split s #"[;=]"))))

(defn select-chat-last-message [chat]
  (when-let [last-msg-id (:last-msg-id chat)]
    (r/single-cljs (r/get-by-field :msgs :msg-id last-msg-id))))

(defn save-message [chat-id {:keys [from to msg-id content content-type outgoing] :or {outgoing false
                                                                                       to       nil} :as msg}]
  (log/debug "save-message" chat-id msg)
  (when-not (r/exists? :msgs :msg-id msg-id)
    (r/write
     (fn []
       (let [chat         (r/single-cljs (r/get-by-field :chats :chat-id chat-id))
             last-message (select-chat-last-message chat)
             content (if (string? content)
                       content
                       (map-to-str content))]
         (r/create :msgs {:chat-id         chat-id
                          :msg-id          msg-id
                          :from            from
                          :to              to
                          :content         content
                          :content-type    content-type
                          :outgoing        outgoing
                          :timestamp       (timestamp)
                          :delivery-status nil
                          :same-author     (if last-message
                                             (= (:from last-message) from)
                                             true)
                          :same-direction  (if last-message
                                             (= (:outgoing last-message) outgoing)
                                             true)} true)
         (r/create :chats {:chat-id     (:chat-id chat)
                           :last-msg-id msg-id}
                   true))))))

(defn get-messages [chat-id]
  (->> (-> (r/get-by-field :msgs :chat-id chat-id)
           (r/sorted :timestamp :asc)
           (r/collection->map))
       (into '())
       (map (fn [{:keys [content-type] :as message}]
              (if (#{c/content-type-command c/content-type-command-request}
                    content-type)
                (update message :content str-to-map)
                message)))))

(defn message-by-id [msg-id]
  (r/single-cljs (r/get-by-field :msgs :msg-id msg-id)))

(defn update-message! [{:keys [msg-id] :as msg}]
  (log/debug "update-message!" msg)
  (r/write
    (fn []
      (when (r/exists? :msgs :msg-id msg-id)
        (r/create :msgs msg true)))))
