(ns status-im.chat.commands.core
  (:require [re-frame.core :as re-frame]
            [clojure.set :as set]
            [pluto.reader.hooks :as hooks]
            [status-im.constants :as constants]
            [status-im.chat.constants :as chat.constants]
            [status-im.chat.commands.protocol :as protocol]
            [status-im.chat.commands.impl.transactions :as transactions]
            [status-im.contact.db :as db]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.fx :as fx]))

(def register
  "Register of all commands. Whenever implementing a new command,
  provide the implementation in the `status-im.chat.commands.impl.*` ns,
  and add its instance here."
  #{(transactions/PersonalSendCommand.)
    (transactions/PersonalRequestCommand.)})

(def command-id (juxt protocol/id protocol/scope))

(defn command-name
  "Given the command instance, returns command name as displayed in chat input,
  with leading `/` character."
  [type]
  (str chat.constants/command-char (protocol/id type)))

(defn command-description
  "Returns description for the command."
  [type]
  (protocol/description type))

(defn accessibility-label
  "Returns accessibility button label for command, derived from its id"
  [type]
  (keyword (str (protocol/id type) "-button")))

(defn- contact->address [contact]
  (str "0x" (db/public-key->address contact)))

(defn add-chat-contacts
  "Enrich command-message by adding contact list of the current private or group chat"
  [contacts {:keys [public? group-chat] :as command-message}]
  (cond
    public? command-message
    group-chat (assoc command-message :contacts (map contact->address contacts))
    :else (assoc command-message :contact (contact->address (first contacts)))))

(defn enrich-command-message-for-events
  "adds new pairs to command-message to be consumed by extension events"
  [db {:keys [chat-id] :as command-message}]
  (let [{:keys [contacts public? group-chat]} (get-in db [:chats chat-id])]
    (add-chat-contacts contacts (assoc command-message :public? public? :group-chat group-chat))))

(defn generate-short-preview
  "Returns short preview for command"
  [{:keys [type]} command-message]
  (protocol/short-preview type command-message))

(defn generate-preview
  "Returns preview for command"
  [{:keys [type]} command-message]
  (protocol/preview type command-message))

(defn- add-exclusive-choices [initial-scope exclusive-choices]
  (reduce (fn [scopes-set exclusive-choices]
            (reduce (fn [scopes-set scope]
                      (let [exclusive-match (set/intersection scope exclusive-choices)]
                        (if (seq exclusive-match)
                          (reduce conj
                                  (disj scopes-set scope)
                                  (map (partial conj
                                                (set/difference scope exclusive-match))
                                       exclusive-match))
                          scopes-set)))
                    scopes-set
                    scopes-set))
          #{initial-scope}
          exclusive-choices))

(defn index-commands
  "Takes collecton of things implementing the command protocol, and
  correctly indexes them  by their composite ids and access scopes."
  [commands]
  (let [id->command              (reduce (fn [acc command]
                                           (assoc acc (command-id command)
                                                  {:type   command
                                                   :params (into [] (protocol/parameters command))}))
                                         {}
                                         commands)
        access-scope->command-id (reduce-kv (fn [acc command-id {:keys [type]}]
                                              (let [access-scopes (add-exclusive-choices
                                                                   (protocol/scope type)
                                                                   protocol/or-scopes)]
                                                (reduce (fn [acc access-scope]
                                                          (update acc
                                                                  access-scope
                                                                  (fnil conj #{})
                                                                  command-id))
                                                        acc
                                                        access-scopes)))
                                            {}
                                            id->command)]
    {:id->command              id->command
     :access-scope->command-id access-scope->command-id}))

(fx/defn load-commands
  "Takes collection of things implementing the command protocol and db,
  correctly indexes them and adds them to db in a way that preserves existing commands"
  [{:keys [db]} commands]
  (let [{:keys [id->command access-scope->command-id]} (index-commands commands)]
    {:db (-> db
             (update :id->command merge id->command)
             (update :access-scope->command-id #(merge-with (fnil into #{}) % access-scope->command-id)))}))

(defn remove-command
  "Remove command form db, correctly updating all indexes"
  [command {:keys [db]}]
  (let [id (command-id command)]
    {:db (-> db
             (update :id->command dissoc id)
             (update :access-scope->command-id (fn [access-scope->command-id]
                                                 (reduce (fn [acc [scope command-ids-set]]
                                                           (if (command-ids-set id)
                                                             (if (= 1 (count command-ids-set))
                                                               acc
                                                               (assoc acc scope (disj command-ids-set id)))
                                                             (assoc acc scope command-ids-set)))
                                                         {}
                                                         access-scope->command-id))))}))

(def command-hook
  "Hook for extensions"
  {:properties
   {:description?   :string
    :scope          #{:personal-chats :public-chats :group-chats}
    :short-preview? :view
    :preview?       :view
    :on-send?       :event
    :on-receive?    :event
    :on-send-sync?  :event
    :parameters?     [{:id           :keyword
                       :type         {:one-of #{:text :phone :password :number}}
                       :placeholder  :string
                       :suggestions? :view}]}
   :hook
   (reify hooks/Hook
     (hook-in [_ id {extension-id :id} {:keys [description scope parameters preview short-preview
                                               on-send on-receive on-send-sync]} cofx]
       (let [new-command (if on-send-sync
                           (reify protocol/Command
                             (id [_] (name id))
                             (scope [_] scope)
                             (description [_] description)
                             (parameters [_] (or parameters []))
                             (validate [_ _ _])
                             (on-send [_ command-message _] (when on-send {:dispatch (on-send command-message)}))
                             (on-receive [_ command-message _] (when on-receive {:dispatch (on-receive command-message)}))
                             (short-preview [_ props] (when short-preview (short-preview props)))
                             (preview [_ props] (when preview (preview props)))
                             protocol/Yielding
                             (yield-control [_ props _] {:dispatch (on-send-sync props)})
                             protocol/Extension
                             (extension-id [_] extension-id))
                           (reify protocol/Command
                             (id [_] (name id))
                             (scope [_] scope)
                             (description [_] description)
                             (parameters [_] (or parameters []))
                             (validate [_ _ _])
                             (on-send [_ command-message _] (when on-send {:dispatch (on-send command-message)}))
                             (on-receive [_ command-message _] (when on-receive {:dispatch (on-receive command-message)}))
                             (short-preview [_ props] (when short-preview (short-preview props)))
                             (preview [_ props] (when preview (preview props)))
                             protocol/Extension
                             (extension-id [_] extension-id)))]
         (load-commands cofx [new-command])))
     (unhook [_ id _ {:keys [scope]} {:keys [db] :as cofx}]
       (remove-command (get-in db [:id->command [(name id) scope] :type]) cofx)))})

(handlers/register-handler-fx
 :load-commands
 (fn [cofx [_ commands]]
   (load-commands commands cofx)))

(defn chat-commands
  "Takes `id->command`, `access-scope->command-id` and `chat` parameters and returns
  entries map of `id->command` map eligible for given chat.
  Note that the result map is keyed just by `protocol/id` of command entries,
  not the unique composite ids of the global `id->command` map.
  That's because this function is already returning local commands for particular
  chat and locally, they should always have unique `protocol/id`."
  [id->command access-scope->command-id {:keys [chat-id group-chat public?]}]
  (let [global-access-scope (cond-> #{}
                              (not group-chat) (conj :personal-chats)
                              (and group-chat (not public?)) (conj :group-chats)
                              public? (conj :public-chats))
        chat-access-scope   #{chat-id}]
    (reduce (fn [acc command-id]
              (let [{:keys [type] :as command-props} (get id->command command-id)]
                (assoc acc (protocol/id type) command-props)))
            {}
            (concat (get access-scope->command-id global-access-scope)
                    (get access-scope->command-id chat-access-scope)))))
