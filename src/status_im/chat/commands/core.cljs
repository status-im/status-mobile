(ns status-im.chat.commands.core
  (:require [clojure.set :as set]
            [status-im.chat.commands.protocol :as protocol]
            [status-im.chat.commands.impl.transactions :as transactions]
            [status-im.chat.models.input :as input-model]))

(def ^:private arg-wrapping-char "\"")
(def ^:private command-char "/")
(def ^:private space-char " ")

(def commands-register
  "Register of all commands. Whenever implementing a new command,
  provide the implementation in the `status-im.chat.commands.impl.*` ns,
  and add its instance here."
  #{(transactions/PersonalSendCommand.)})

(defn validate-and-send
  "Validates and sends command in current chat"
  [command cofx]
  nil)

(defn send
  "Sends command with given arguments in particular chat"
  [command chat-id cofx]
  nil)

(def command-id (juxt protocol/id protocol/scope))

(defn- prepare-params
  "Prepares parameters sequence of command by providing suggestion components with
  selected-event injected with correct arg indexes and `last-arg?` flag."
  [command]
  (let [parameters     (protocol/parameters command)
        last-param-idx (dec (count parameters))]
    (into []
          (map-indexed (fn [idx {:keys [suggestions] :as param}]
                         (if suggestions
                           (update param :suggestions partial
                                   (fn [value]
                                     [:set-command-parameter
                                      (= idx last-param-idx) idx value]))
                           param))
                       parameters))))

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
  [commands {:keys [db]}]
  (let [id->command              (reduce (fn [acc command]
                                           (assoc acc (command-id command)
                                                  {:type   command
                                                   :params (prepare-params command)}))
                                         {}
                                         commands)
        access-scope->command-id (reduce-kv (fn [acc command-id {:keys [type]}]
                                              (let [access-scopes (add-exclusive-choices
                                                                   (protocol/scope type)
                                                                   protocol/or-scopes)]
                                                (reduce (fn [acc access-scope]
                                                          (assoc acc
                                                                 access-scope
                                                                 command-id))
                                                        acc
                                                        access-scopes)))
                                            {}
                                            id->command)]
    {:db (assoc db
                :id->command              id->command
                :access-scope->command-id access-scope->command-id)}))

(defn set-command-parameter
  "Set value as command parameter for the current chat"
  [last-param? param-index value {:keys [db]}]
  (let [{:keys [current-chat-id]} db
        [command & params]  (-> (get-in db [:chats current-chat-id :input-text])
                                input-model/split-command-args)
        param-count         (count params)
        ;; put the new value at the right place in parameters array
        new-params          (cond-> (into [] params)
                              (< param-index param-count) (assoc param-index value)
                              (>= param-index param-count) (conj value))
        ;; if the parameter is not the last one for the command, add space
        input-text                (cond-> (str command space-char
                                               (input-model/join-command-args
                                                new-params))
                                    (and (not last-param?)
                                         (or (= 0 param-count)
                                             (= param-index (dec param-count))))
                                    (str space-char))]
    {:db (assoc-in db [:chats current-chat-id :input-text]
                   (input-model/text->emoji input-text))}))
