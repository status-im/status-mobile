(ns status-im.data-store.realm.schemas.account.v19.core
  (:require [status-im.data-store.realm.schemas.account.v19.chat :as chat]
            [status-im.data-store.realm.schemas.account.v1.chat-contact :as chat-contact]
            [status-im.data-store.realm.schemas.account.v19.contact :as contact]
            [status-im.data-store.realm.schemas.account.v1.discover :as discover]
            [status-im.data-store.realm.schemas.account.v19.message :as message]
            [status-im.data-store.realm.schemas.account.v12.pending-message :as pending-message]
            [status-im.data-store.realm.schemas.account.v1.processed-message :as processed-message]
            [status-im.data-store.realm.schemas.account.v19.request :as request]
            [status-im.data-store.realm.schemas.account.v1.tag :as tag]
            [status-im.data-store.realm.schemas.account.v19.user-status :as user-status]
            [status-im.data-store.realm.schemas.account.v5.contact-group :as contact-group]
            [status-im.data-store.realm.schemas.account.v5.group-contact :as group-contact]
            [status-im.data-store.realm.schemas.account.v8.local-storage :as local-storage]
            [taoensso.timbre :as log]
            [cljs.reader :as reader]))

(def schema [chat/schema
             chat-contact/schema
             contact/schema
             discover/schema
             message/schema
             pending-message/schema
             processed-message/schema
             request/schema
             tag/schema
             user-status/schema
             contact-group/schema
             group-contact/schema
             local-storage/schema])

(defn remove-console-intro-message! [new-realm]
  (when-let [console-intro-message (some-> new-realm
                                           (.objects "message")
                                           (.filtered (str "message-id = \"intro-status\""))
                                           (aget 0))]
    (log/debug "v19 Removing console intro message " (pr-str console-intro-message))))

(defn remove-contact! [new-realm whisper-identity]
  (when-let [contact (some-> new-realm
                             (.objects "contact")
                             (.filtered (str "whisper-identity = \"" whisper-identity "\""))
                             (aget 0))]
    (log/debug "v19 Removing contact " (pr-str contact))
    (.delete new-realm contact)))

(def owner-command->new-props
  {;; console commands
   ["console" "password"] {:content-command-ref ["console" :response 42 "password"]
                           :content-command-scope-bitmask 42}
   ["console" "debug"] {:content-command-ref ["console" :command 50 "debug"]
                        :content-command-scope-bitmask 50}
   ["console" "phone"] {:content-command-ref ["console" :response 50 "phone"]
                        :content-command-scope-bitmask 50}
   ["console" "confirmation-code"] {:content-command-ref ["console" :response 50 "confirmation-code"]
                                    :content-command-scope-bitmask 50}
   ["console" "faucet"] {:content-command-ref ["console" :command 50 "faucet"]
                         :content-command-scope-bitmask 50}
   ;; mailman commands
   ["mailman" "location"] {:content-command-ref ["mailman" :command 215 "location"]
                           :content-command-scope-bitmask 215}
   ;; transactor personal
   ["transactor-personal" "send"] {:content-command-ref ["transactor" :command 83 "send"]
                                   :content-command-scope-bitmask 83
                                   :bot "transactor"}
   ["transactor-personal" "request"] {:content-command-ref ["transactor" :command 83 "request"]
                                      :content-command-scope-bitmask 83
                                      :bot "transactor"}
   ;; transactor group
   ["transactor-group" "send"] {:content-command-ref ["transactor" :command 85 "send"]
                                :content-command-scope-bitmask 85
                                :bot "transactor"}
   ["transactor-group" "request"] {:content-command-ref ["transactor" :command 85 "request"]
                                   :content-command-scope-bitmask 85
                                   :bot "transactor"}})

(def console-requests->new-props
  {;; console
   ["password"] {:content-command-ref ["console" :response 42 "password"]}
   ["phone"] {:content-command-ref ["console" :response 50 "phone"]}
   ["confirmation-code"] {:content-command-ref ["console" :response 50 "confirmation-code"]}})

(def transactor-requests->new-props
  {;; former transactor-personal request
   ["send" 1] {:content-command-ref ["transactor" :response 83 "send"]
               :content-command-scope-bitmask 83
               :bot "transactor"}
   ;; former transactor-group request
   ["send" 2] {:content-command-ref ["transactor" :response 85 "send"]
               :content-command-scope-bitmask 85
               :bot "transactor"}})

(defn update-commands [selector mapping new-realm content-type]
  (some-> new-realm
          (.objects "message")
          (.filtered (str "content-type = \"" content-type "\""))
          (.map (fn [object _ _]
                  (let [content (reader/read-string (aget object "content"))
                        new-props (get mapping (selector content))
                        new-content (merge content new-props)]
                    (log/debug "migrating v19 command/request database, updating: " content " with: " new-props)
                    (aset object "content" (pr-str new-content)))))))

(defn update-message-statuses [new-realm]
  (some-> new-realm
          (.objects "message")
          (.map (fn [msg _ _]
                  (let [message-id (aget msg "message-id")
                        chat-id    (aget msg "chat-id")
                        from       (aget msg "from")
                        msg-status (aget msg "message-status")
                        statuses   (aget msg "user-statuses")]
                    (when statuses 
                      (.map statuses (fn [status _ _]
                                       (aset status "status-id" (str message-id "-" from))
                                       (aset status "message-id" message-id)
                                       (aset status "chat-id"    chat-id)))
                      (.push statuses (clj->js {"status-id"        (str message-id "-anonymous")
                                                "message-id"       message-id
                                                "chat-id"          chat-id
                                                "status"           (or msg-status "received")
                                                "whisper-identity" (or from "anonymous")}))))))))

(defn migration [old-realm new-realm]
  (log/debug "migrating v19 account database: " old-realm new-realm)
  (remove-contact! new-realm "transactor-personal")
  (remove-contact! new-realm "transactor-group")
  (remove-console-intro-message! new-realm)
  (update-commands (juxt :bot :command) owner-command->new-props new-realm "command")
  (update-commands (juxt :command) console-requests->new-props new-realm "command-request")
  (update-commands (juxt :command (comp count :prefill)) transactor-requests->new-props new-realm "command-request")
  (update-message-statuses new-realm))
