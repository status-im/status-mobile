(ns status-im.data-store.realm.schemas.account.migrations
  (:require [taoensso.timbre :as log]
            [cljs.reader :as reader]))

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
                  (aset message "content-type" "command")))))

(defn v15 [old-realm new-realm]
  (log/debug "migrating v15 account database"))

(defn v16 [old-realm new-realm]
  (log/debug "migrating v16 account database"))
