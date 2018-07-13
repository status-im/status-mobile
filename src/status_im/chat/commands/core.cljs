(ns status-im.chat.commands.core
  (:require [re-frame.core :as re-frame]
            [clojure.set :as set]
            [clojure.string :as string]
            [pluto.host :as host]
            [status-im.constants :as constants]
            [status-im.chat.constants :as chat-constants]
            [status-im.chat.commands.protocol :as protocol]
            [status-im.chat.commands.impl.transactions :as transactions]
            [status-im.chat.models :as chat-model]
            [status-im.chat.models.input :as input-model]
            [status-im.chat.models.message :as message-model]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]))

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
  (str chat-constants/command-char (protocol/id type)))

(defn command-description
  "Returns description for the command."
  [type]
  (protocol/description type))

(defn accessibility-label
  "Returns accessibility button label for command, derived from its id"
  [type]
  (keyword (str (protocol/id type) "-button")))

(defn generate-short-preview
  "Returns short preview for command"
  [{:keys [type]} command-message]
  (protocol/short-preview type command-message))

(defn generate-preview
  "Returns preview for command"
  [{:keys [type]} command-message]
  (protocol/preview type command-message))

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
  [commands]
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

(defn load-commands
  "Takes collection of things implementing the command protocol and db,
  correctly indexes them and adds them to db in a way that preserves existing commands"
  [commands {:keys [db]}]
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
  (reify host/AppHook
    (id [_] :commands)
    (properties [_] {:scope         #{:personal-chats :public-chats}
                     :description   :string
                     :short-preview :view
                     :preview       :view
                     :parameters    [{:id           :keyword
                                      :type         {:one-of #{:text :phone :password :number}}
                                      :placeholder  :string
                                      :suggestions? :component}]})
    (hook-in [_ id {:keys [description scope parameters preview short-preview]} cofx]
      (let [new-command (reify protocol/Command
                          (id [_] (name id))
                          (scope [_] scope)
                          (description [_] description)
                          (parameters [_] parameters)
                          (validate [_ _ _])
                          (on-send [_ _ _])
                          (on-receive [_ _ _])
                          (short-preview [_ props] (short-preview props))
                          (preview [_ props] (preview props)))]
        (load-commands [new-command] cofx)))
    (unhook [_ id {:keys [scope]} {:keys [db] :as cofx}]
      (remove-command (get-in db [:id->command [(name id) scope] :type]) cofx))))

(handlers/register-handler-fx
 :load-commands
 [re-frame/trim-v]
 (fn [cofx [commands]]
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

(defn- current-param-position [input-text selection]
  (when selection
    (when-let [subs-input-text (subs input-text 0 selection)]
      (let [input-params   (input-model/split-command-args subs-input-text)
            param-index    (dec (count input-params))
            wrapping-count (get (frequencies subs-input-text) chat-constants/arg-wrapping-char 0)]
        (if (and (string/ends-with? subs-input-text chat-constants/spacing-char)
                 (even? wrapping-count))
          param-index
          (dec param-index))))))

(defn- command-completion [input-params params]
  (let [input-params-count (count input-params)
        params-count       (count params)]
    (cond
      (= input-params-count params-count) :complete
      (< input-params-count params-count) :less-then-needed
      (> input-params-count params-count) :more-than-needed)))

(defn selected-chat-command
  "Takes input text, text-selection and `protocol/id->command-props` map (result of 
  the `chat-commands` fn) and returns the corresponding `command-props` entry, 
  or nothing if input text doesn't match any available command.
  Besides keys `:params` and `:type`, the returned map contains: 
  * `:input-params` - parsed parameters from the input text as map of `param-id->entered-value`
  # `:current-param-position` - index of the parameter the user is currently focused on (cursor position
  in relation to parameters), could be nil if the input is not selected
  # `:command-completion` - indication of command completion, possible values are `:complete`,
  `:less-then-needed` and `more-then-needed`"
  [input-text text-selection id->command-props]
  (when (input-model/starts-as-command? input-text)
    (let [[command-name & input-params] (input-model/split-command-args input-text)]
      (when-let [{:keys [params] :as command-props} (get id->command-props (subs command-name 1))] ;; trim leading `/` for lookup
        command-props
        (let [input-params (into {}
                                 (keep-indexed (fn [idx input-value]
                                                 (when (not (string/blank? input-value))
                                                   (when-let [param-name (get-in params [idx :id])]
                                                     [param-name input-value]))))
                                 input-params)]
          (assoc command-props
                 :input-params input-params
                 :current-param-position (current-param-position input-text text-selection)
                 :command-completion (command-completion input-params params)))))))

(defn set-command-parameter
  "Set value as command parameter for the current chat"
  [last-param? param-index value {:keys [db]}]
  (let [{:keys [current-chat-id chats]} db
        [command & params]  (-> (get-in chats [current-chat-id :input-text])
                                input-model/split-command-args)
        param-count         (count params)
        ;; put the new value at the right place in parameters array
        new-params          (cond-> (into [] params)
                              (< param-index param-count) (assoc param-index value)
                              (>= param-index param-count) (conj value))
        ;; if the parameter is not the last one for the command, add space
        input-text                (cond-> (str command chat-constants/spacing-char
                                               (input-model/join-command-args
                                                new-params))
                                    (and (not last-param?)
                                         (or (= 0 param-count)
                                             (= param-index (dec param-count))))
                                    (str chat-constants/spacing-char))]
    {:db (-> db
             (chat-model/set-chat-ui-props {:validation-messages nil})
             (assoc-in [:chats current-chat-id :input-text] input-text))}))

(defn select-chat-input-command
  "Takes command and (optional) map of input-parameters map and sets it as current-chat input"
  [{:keys [type params]} input-params {:keys [db]}]
  (let [{:keys [current-chat-id chat-ui-props]} db]
    {:db (-> db
             (chat-model/set-chat-ui-props {:show-suggestions?   false
                                            :validation-messages nil})
             (assoc-in [:chats current-chat-id :input-text]
                       (str (command-name type)
                            chat-constants/spacing-char
                            (input-model/join-command-args input-params))))}))

(defn parse-parameters
  "Parses parameters from input for defined command params,
  returns map of `param-name->param-value`"
  [params input-text]
  (let [input-params (->> (input-model/split-command-args input-text) rest (into []))]
    (into {}
          (keep-indexed (fn [idx {:keys [id]}]
                          (when-let [value (get input-params idx)]
                            [id value])))
          params)))
