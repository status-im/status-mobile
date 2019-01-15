(ns status-im.data-store.realm.schemas.account.migrations
  (:require [taoensso.timbre :as log]
            [cljs.reader :as reader]
            [status-im.chat.models.message-content :as message-content]
            [status-im.transport.utils :as transport.utils]
            [cljs.tools.reader.edn :as edn]
            [clojure.string :as string]
            [status-im.constants :as constants]
            [cognitect.transit :as transit]
            [status-im.js-dependencies :as dependencies]
            [status-im.utils.clocks :as utils.clocks]))

(defn v1 [old-realm new-realm]
  (log/debug "migrating v1 account database: " old-realm new-realm))

(defn v2 [old-realm new-realm]
  (log/debug "migrating v2 account database: " old-realm new-realm))

(defn v3 [old-realm new-realm]
  (log/debug "migrating v3 account database: " old-realm new-realm))

(defn v4 [old-realm new-realm]
  (log/debug "migrating v4 account database: " old-realm new-realm))

(defn v5 [old-realm new-realm]
  (log/debug "migrating chats schema v5")
  (let [chats (.objects new-realm "chat")]
    (dotimes [i (.-length chats)]
      (js-delete (aget chats i) "contact-info"))))

(defn v6 [old-realm new-realm]
  (log/debug "migrating v6 account database: " old-realm new-realm))

(defn v7 [old-realm new-realm]
  (log/debug "migrating messages schema v7")
  (let [messages (.objects new-realm "message")]
    (dotimes [i (.-length messages)]
      (js-delete (aget messages i) "user-statuses"))))

(defn message-by-id [realm message-id]
  (some-> realm
          (.objects "message")
          (.filtered (str "message-id = \"" message-id "\""))
          (aget 0)))

(defn v8 [old-realm new-realm]
  (log/debug "migrating v8 account database")
  (let [browsers     (.objects new-realm "browser")
        old-browsers (.objects old-realm "browser")]
    (dotimes [i (.-length browsers)]
      (let [browser     (aget browsers i)
            old-browser (aget old-browsers i)
            url         (aget old-browser "url")]
        (aset browser "history-index" 0)
        (aset browser "history" (clj->js [url]))))))

(defn v9 [old-realm new-realm]
  (log/debug "migrating v9 account database"))

(defn v10 [old-realm new-realm]
  (log/debug "migrating v10 account database")
  (some-> old-realm
          (.objects "request")
          (.filtered (str "status = \"answered\""))
          (.map (fn [request _ _]
                  (let [message-id  (aget request "message-id")
                        message     (message-by-id new-realm message-id)
                        content     (reader/read-string (aget message "content"))
                        new-content (assoc-in content [:params :answered?] true)]
                    (aset message "content" (pr-str new-content)))))))

(defn v11 [old-realm new-realm]
  (log/debug "migrating v11 account database")
  (let [mailservers     (.objects new-realm "mailserver")]
    (dotimes [i (.-length mailservers)]
      (aset (aget mailservers i) "fleet" "eth.beta"))))

(defn v12 [old-realm new-realm]
  (log/debug "migrating v12 account database")
  (some-> new-realm
          (.objects "message")
          (.filtered (str "content-type = \"text/plain\""))
          (.map (fn [message _ _]
                  (let [content     (aget message "content")
                        new-content {:text content}]
                    (aset message "content" (pr-str new-content))))))
  (some-> new-realm
          (.objects "message")
          (.filtered (str "content-type = \"emoji\""))
          (.map (fn [message _ _]
                  (let [content     (aget message "content")
                        new-content {:text content}]
                    (aset message "content" (pr-str new-content)))))))

(defn v13 [old-realm new-realm]
  (log/debug "migrating v13 account database"))

(defn v14 [old-realm new-realm]
  (log/debug "migrating v14 account database")
  (some-> new-realm
          (.objects "message")
          (.filtered (str "content-type = \"command-request\""))
          (.map (fn [message _ _]
                  (when message
                    (aset message "content-type" "command"))))))

(defn v15 [old-realm new-realm]
  (log/debug "migrating v15 account database"))

(defn v16 [old-realm new-realm]
  (log/debug "migrating v16 account database"))

(defn v17 [old-realm new-realm]
  (log/debug "migrating v17 account database"))

(defn v18
  "reset last request to 1 to fetch 7 past days of history"
  [old-realm new-realm]
  (log/debug "migrating v18 account database")
  (some-> new-realm
          (.objects "transport-inbox-topic")
          (.map (fn [inbox-topic _ _]
                  (aset inbox-topic "last-request" 1)))))

(defn v19 [old-realm new-realm]
  (log/debug "migrating v19 account database"))

(defn v20 [old-realm new-realm]
  (log/debug "migrating v20 account database")
  (some-> new-realm
          (.objects "message")
          (.filtered (str "content-type = \"text/plain\""))
          (.map (fn [message _ _]
                  (let [content     (reader/read-string (aget message "content"))
                        new-content (message-content/enrich-content content)]
                    (aset message "content" (pr-str new-content)))))))

(defn v21 [old-realm new-realm]
  (log/debug "migrating v21 account database"))

(defn v22 [old-realm new-realm]
  (log/debug "migrating v22 account database"))

(defn v23
  "the primary key for contact was whisper-identity
  change to public-key and remove whisper-identity field"
  [old-realm new-realm]
  (log/debug "migrating v20 account database")
  (let [old-contacts (.objects old-realm "contact")
        new-contacts (.objects new-realm "contact")]
    (dotimes [i (.-length old-contacts)]
      (let [old-contact (aget old-contacts i)
            new-contact (aget new-contacts i)
            whisper-identity (aget old-contact "whisper-identity")]
        (aset new-contact "public-key" whisper-identity))))
  (let [old-user-statuses (.objects old-realm "user-status")
        new-user-statuses (.objects new-realm "user-status")]
    (dotimes [i (.-length old-user-statuses)]
      (let [old-user-status (aget old-user-statuses i)
            new-user-status (aget new-user-statuses i)
            whisper-identity (aget old-user-status "whisper-identity")]
        (aset new-user-status "public-key" whisper-identity)))))

(defn v24 [old-realm new-realm]
  (log/debug "migrating v24 account database"))

(defn v25 [old-realm new-realm])

(defn v26 [old-realm new-realm]
  (let [chats (.objects new-realm "chat")]
    (dotimes [i (.-length chats)]
      (let [chat                (aget chats i)
            chat-id             (aget chat "chat-id")
            user-statuses-count (-> (.objects new-realm "user-status")
                                    (.filtered (str "chat-id=\"" chat-id "\""
                                                    " and "
                                                    "status = \"received\""))
                                    (.-length))]
        (aset chat "unviewed-messages-count" user-statuses-count)))))

;; Message record's interface was
;; copied from status-im.transport.message.protocol
;; to ensure that any further changes to this record will not
;; affect migrations
(defrecord Message [content content-type message-type clock-value timestamp])

(defn replace-ns [str-message]
  (string/replace-first
   str-message
   "status-im.data-store.realm.schemas.account.migrations"
   "status-im.transport.message.protocol"))

(defn sha3 [s]
  (.sha3 dependencies/Web3.prototype s))

(defn old-message-id
  "Calculates the same `message-id` as was used in `0.9.31`"
  [message]
  (sha3 (replace-ns (pr-str message))))

;; The code below copied from status-im.transport.message.transit
;; in order to make sure that future changes will not have any impact
;; on migrations
(defn- new->legacy-command-data [{:keys [command-path params] :as content}]
  (get {["send" #{:personal-chats}]    [{:command-ref ["transactor" :command 83 "send"]
                                         :command "send"
                                         :bot "transactor"
                                         :command-scope-bitmask 83}
                                        constants/content-type-command]
        ["request" #{:personal-chats}] [{:command-ref ["transactor" :command 83 "request"]
                                         :request-command-ref ["transactor" :command 83 "send"]
                                         :command "request"
                                         :request-command "send"
                                         :bot "transactor"
                                         :command-scope-bitmask 83
                                         :prefill [(get params :asset)
                                                   (get params :amount)]}
                                        constants/content-type-command-request]}
       command-path))

(deftype MessageHandler []
  Object
  (tag [this v] "c4")
  (rep [this {:keys [content content-type message-type clock-value timestamp]}]
    (condp = content-type
      constants/content-type-text ;; append new content add the end, still pass content the old way at the old index
      #js [(:text content) content-type message-type clock-value timestamp content]
      constants/content-type-command ;; handle command compatibility issues
      (let [[legacy-content legacy-content-type] (new->legacy-command-data content)]
        #js [(merge content legacy-content) (or legacy-content-type content-type) message-type clock-value timestamp])
      ;; no need for legacy conversions for rest of the content types
      #js [content content-type message-type clock-value timestamp])))

(def writer (transit/writer :json
                            {:handlers
                             {Message (MessageHandler.)}}))

(defn serialize
  "Serializes a record implementing the StatusMessage protocol using the custom writers"
  [o]
  (transit/write writer o))

(defn raw-payload
  [message]
  (transport.utils/from-utf8 (serialize message)))

(defn v27 [old-ream new-realm]
  (let [messages (.objects new-realm "message")
        user-statuses (.objects new-realm "user-status")
        old-ids->new-ids (volatile! {})
        messages-to-be-deleted (volatile! [])
        statuses-to-be-deleted (volatile! [])]
    (dotimes [i (.-length messages)]
      (let [message         (aget messages i)
            prev-message-id (aget message "message-id")
            content         (-> (aget message "content")
                                edn/read-string
                                (dissoc :should-collapse? :metadata :render-recipe))
            content-type    (aget message "content-type")
            message-type    (keyword
                             (aget message "message-type"))
            clock-value     (aget message "clock-value")
            from            (aget message "from")
            timestamp       (aget message "timestamp")
            message-record  (Message. content content-type message-type
                                      clock-value timestamp)
            old-message-id  (old-message-id message-record)
            raw-payload     (raw-payload message-record)
            message-id      (transport.utils/message-id from raw-payload)
            raw-payload-hash (transport.utils/sha3 raw-payload)]
        (vswap! old-ids->new-ids assoc prev-message-id message-id)
        (if (.objectForPrimaryKey
             new-realm
             "message"
             message-id)
          (vswap! messages-to-be-deleted conj message)
          (do
            (aset message "message-id" message-id)
            (aset message "raw-payload-hash" raw-payload-hash)
            (aset message "old-message-id" old-message-id)))))

    (doseq [message @messages-to-be-deleted]
      (.delete new-realm message))

    (dotimes [i (.-length user-statuses)]
      (let [user-status (aget user-statuses i)
            message-id     (aget user-status "message-id")
            new-message-id (get @old-ids->new-ids message-id)
            public-key     (aget user-status "public-key")
            new-status-id (str new-message-id "-" public-key)]
        (if (.objectForPrimaryKey
             new-realm
             "user-status"
             new-status-id)
          (vswap! statuses-to-be-deleted conj user-status)
          (when (contains? @old-ids->new-ids message-id)
            (aset user-status "status-id" new-status-id)
            (aset user-status "message-id" new-message-id)))))

    (doseq [status @statuses-to-be-deleted]
      (.delete new-realm status))))

(defn get-last-message [realm chat-id]
  (->
   (.objects realm "message")
   (.filtered (str "chat-id=\"" chat-id "\""))
   (.sorted "timestamp" true)
   (aget 0)))

(defn v28 [old-realm new-realm])

(defn get-last-clock-value [realm chat-id]
  (if-let [last-message
           (-> (.objects realm "message")
               (.filtered (str "chat-id=\"" chat-id "\""))
               (.sorted "clock-value" true)
               (aget 0))]
    (->
     last-message
     (aget "clock-value")
     (utils.clocks/safe-timestamp))
    0))

(defn v29 [old-realm new-realm]
  (let [chats (.objects new-realm "chat")]
    (dotimes [i (.-length chats)]
      (let [chat (aget chats i)
            chat-id (aget chat "chat-id")]
        (when-let [last-clock-value (get-last-clock-value new-realm chat-id)]
          (aset chat "last-clock-value" last-clock-value))))))

(defn v30 [old-realm new-realm]
  (let [chats (.objects new-realm "chat")]
    (dotimes [i (.-length chats)]
      (let [chat (aget chats i)
            chat-id (aget chat "chat-id")]
        (when-let [last-message (get-last-message new-realm chat-id)]
          (let [content (aget last-message "content")
                content-type (aget last-message "content-type")]
            (aset chat "last-message-content" content)
            (aset chat "last-message-content-type" content-type)))))))

(defn v34 [old-realm new-realm]
  (let [chats (.objects new-realm "chat")]
    (dotimes [i (.-length chats)]
      (let [chat (aget chats i)
            chat-id (aget chat "chat-id")]
        (aset chat "group-chat-local-version" 0)))))
