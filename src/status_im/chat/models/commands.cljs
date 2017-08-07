(ns status-im.chat.models.commands
  (:require [status-im.chat.constants :as chat-consts]
            [status-im.bots.constants :as bots-constants]
            [clojure.string :as str]
            [taoensso.timbre :as log]))

(defn scope->bit-mask
  "Transforms scope map to a single integer value by generating a bit mask."
  [{:keys [global? registered-only? personal-chats? group-chats? can-use-for-dapps?]}]
  (bit-or (when global? 1)
          (when registered-only? 2)
          (when personal-chats? 4)
          (when group-chats? 8)
          (when can-use-for-dapps? 16)))

(defn get-mixable-commands
  "Returns all commands of mixable contacts."
  [{:contacts/keys [contacts]}]
  (->> contacts
       (vals)
       (filter :mixable?)
       (mapv :commands)
       (mapcat #(into [] %))
       (reduce (fn [acc [k v]] (update acc k #(into % v))) {})))

(defn get-mixable-identities
  "Returns a lazy-seq of all mixable contacts. Each contact contains only one key `:identity`."
  [{:contacts/keys [contacts]}]
  (->> contacts
       (vals)
       (filter :mixable?)
       (map (fn [{:keys [whisper-identity]}]
              {:identity whisper-identity}))))

(defn- transform-commands-map
  "Transforms a map of commands to a flat list."
  [commands]
  (->> commands
       (map val)
       (remove empty?)
       (flatten)))

(defn get-possible-requests
  "Returns a list of all possible requests for current chat."
  [{:keys [current-chat-id] :as db}]
  (let [requests (->> (get-in db [:chats current-chat-id :requests])
                      (map (fn [{:keys [type chat-id bot] :as req}]
                             [type (map (fn [resp]
                                          (assoc resp :request req))
                                        (get-in db [:contacts/contacts (or bot chat-id) :responses type]))]))
                      (remove (fn [[_ items]] (empty? items)))
                      (into {}))]
    (transform-commands-map requests)))

(defn get-possible-commands
  "Returns a list of all possible commands for current chat."
  [{:keys [current-chat-id] :as db}]
  (->> (get-in db [:chats current-chat-id :contacts])
       (into (get-mixable-identities db))
       (map (fn [{:keys [identity]}]
              (let [commands (get-in db [:contacts/contacts identity :commands])]
                (transform-commands-map commands))))
       (flatten)))

(defn get-possible-global-commands
  "Returns a list of all possible global commands for current chat."
  [{:keys [global-commands] :as db}]
  (transform-commands-map global-commands))

(defn commands-for-chat
  "Returns a list of filtered commands for current chat.
   Uses scopes to filter commands."
  [{:keys          [global-commands chats]
    :contacts/keys [contacts]
    :accounts/keys [accounts current-account-id]
    :as            db} chat-id]
  (let [global-commands (get-possible-global-commands db)
        commands        (get-possible-commands db)
        account         (get accounts current-account-id)
        commands        (-> (into [] global-commands)
                            (into commands))
        {chat-contacts :contacts} (get chats chat-id)]
    (remove (fn [{:keys [scope]}]
              (or
               (and (:registered-only? scope)
                    (not (:address account)))
               (and (not (:personal-chats? scope))
                    (= (count chat-contacts) 1))
               (and (not (:group-chats? scope))
                    (> (count chat-contacts) 1))
               (and (not (:can-use-for-dapps? scope))
                    (every? (fn [{:keys [identity]}]
                              (get-in contacts [identity :dapp?]))
                            chat-contacts))))
            commands)))

(defn set-command-for-content
  "Sets the information about command for a specified message content.
   We need to use this command because `command` field in persistent storage (db) doesn't
   contain all information about command — we save only the name of it."
  [commands global-commands content]
  (if (map? content)
    (let [{:keys [command bot]} content]
      (if (and bot (not (bots-constants/mailman-bot? bot)))
        (update content :command #((keyword bot) global-commands))
        (update content :command #((keyword command) commands))))
    content))

(defn set-command-for-request
  "Sets the information about command for a specified request."
  [{:keys [message-id content] :as message} possible-requests possible-commands]
  (let [requests (->> possible-requests
                      (map (fn [{:keys [request] :as message}]
                             [(:message-id request) message]))
                      (into {}))
        commands (->> possible-commands
                      (map (fn [{:keys [name] :as message}]
                             [name message]))
                      (into {}))]
    (assoc content :command (or (get requests message-id)
                                (get commands (get content :command))))))