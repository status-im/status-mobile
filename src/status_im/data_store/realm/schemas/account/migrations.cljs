(ns status-im.data-store.realm.schemas.account.migrations
  (:require [taoensso.timbre :as log]
            [cljs.reader :as reader]
            [status-im.chat.models.message-content :as message-content]
            [status-im.transport.utils :as transport.utils]
            [cljs.tools.reader.edn :as edn]
            [status-im.js-dependencies :as dependencies]
            [clojure.string :as string]
            [cljs.tools.reader.edn :as edn]))

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

(defn v25 [old-realm new-realm]
  (log/debug "migrating v25 account database")
  (let [new-messages (.objects new-realm "message")
        user-statuses (.objects new-realm "user-status")
        old-ids->new-ids (volatile! {})
        updated-messages-ids (volatile! #{})
        updated-message-statuses-ids (volatile! #{})
        messages-to-be-deleted (volatile! [])
        statuses-to-be-deleted (volatile! [])]
    (dotimes [i (.-length new-messages)]
      (let [message (aget new-messages i)
            message-id (aget message "message-id")
            from (aget message "from")
            chat-id (:chat-id (edn/read-string (aget message "content")))
            clock-value (aget message "clock-value")
            new-message-id (transport.utils/message-id
                            {:from        from
                             :chat-id     chat-id
                             :clock-value clock-value})]
        (vswap! old-ids->new-ids assoc message-id new-message-id)))

    (dotimes [i (.-length new-messages)]
      (let [message (aget new-messages i)
            old-message-id (aget message "message-id")
            content (edn/read-string (aget message "content"))
            response-to (:response-to content)
            new-message-id (get @old-ids->new-ids old-message-id)]
        (if (contains? @updated-messages-ids new-message-id)
          (vswap! messages-to-be-deleted conj message)
          (do
            (vswap! updated-messages-ids conj new-message-id)
            (aset message "message-id" new-message-id)
            (when (and response-to (get @old-ids->new-ids response-to))
              (let [new-content (assoc content :response-to
                                       (get @old-ids->new-ids response-to))]
                (aset message "content" (prn-str new-content))))))))

    (doseq [message @messages-to-be-deleted]
      (.delete new-realm message))

    (dotimes [i (.-length user-statuses)]
      (let [user-status (aget user-statuses i)
            message-id     (aget user-status "message-id")
            new-message-id (get @old-ids->new-ids message-id)
            public-key     (aget user-status "public-key")
            new-status-id (str new-message-id "-" public-key)]
        (if (contains? @updated-message-statuses-ids new-status-id)
          (vswap! statuses-to-be-deleted conj user-status)
          (do
            (vswap! updated-message-statuses-ids conj new-status-id)
            (aset user-status "status-id" new-status-id)
            (aset user-status "message-id" new-message-id)))))

    (doseq [status @statuses-to-be-deleted]
      (.delete new-realm status))))

(defn v26 [old-realm new-realm]
  (let [user-statuses (.objects new-realm "user-status")]
    (dotimes [i (.-length user-statuses)]
      (let [user-status   (aget user-statuses i)
            status-id     (aget user-status "message-id")
            message-id    (aget user-status "message-id")
            public-key    (aget user-status "public-key")
            new-status-id (str message-id "-" public-key)]
        (when (and (= "-" (last status-id)))
          (if (.objectForPrimaryKey
               new-realm
               "user-status"
               new-status-id)
            (.delete new-realm user-status)
            (aset user-status "status-id" new-status-id))))))
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

(defrecord Message [content content-type message-type clock-value timestamp])

(defn sha3 [s]
  (.sha3 dependencies/Web3.prototype s))

(defn replace-ns [str-message]
  (string/replace-first
   str-message
   "status-im.data-store.realm.schemas.account.migrations"
   "status-im.transport.message.protocol"))

(defn old-message-id
  [message]
  (sha3 (replace-ns (pr-str message))))

(defn v27 [old-realm new-realm]
  (let [messages (.objects new-realm "message")]
    (dotimes [i (.-length messages)]
      (let [js-message     (aget messages i)
            message        {:content      (edn/read-string
                                           (aget js-message "content"))
                            :content-type (aget js-message "content-type")
                            :message-type (keyword
                                           (aget js-message "message-type"))
                            :clock-value  (aget js-message "clock-value")
                            :timestamp    (aget js-message "timestamp")}
            message-record (map->Message message)
            old-message-id (old-message-id message-record)]
        (aset js-message "old-message-id" old-message-id)))))
