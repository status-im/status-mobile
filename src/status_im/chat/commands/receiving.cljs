(ns status-im.chat.commands.receiving
  (:require [status-im.chat.commands.protocol :as protocol]
            [status-im.utils.fx :as fx]))

;; TODO(janherich) remove after couple of releases when danger of messages
;; with old command references will be low
(def ^:private old->new-path
  {["transactor" :command 83 "send"]    ["send" #{:personal-chats}]
   ["transactor" :command 83 "request"] ["request" #{:personal-chats}]})

(defn lookup-command-by-ref
  "Function which takes message object and looks up associated entry from the
  `id->command` map if it can be found"
  [{:keys [content]} id->command]
  (when-let [path (or (:command-path content)
                      (:command-ref content))]
    (or (get id->command path)
        (get id->command (get old->new-path path)))))

(fx/defn receive
  "Performs receive effects for command message. Does nothing
  when message is not of the command type or command can't be found."
  [{:keys [db] :as cofx} message]
  (let [id->command (:id->command db)]
    (when-let [{:keys [type]} (lookup-command-by-ref message id->command)]
      (protocol/on-receive type message cofx))))

(defn enhance-receive-parameters
  "Enhances parameters for the received command message.
  If the message is not of the command type, or command doesn't implement the
  `EnhancedParameters` protocol, returns unaltered message, otherwise updates
  its parameters."
  [{:keys [content] :as message} {:keys [db] :as cofx}]
  (let [id->command    (:id->command db)
        {:keys [type]} (lookup-command-by-ref message id->command)]
    (if-let [new-params (and (satisfies? protocol/EnhancedParameters type)
                             (protocol/enhance-receive-parameters type (:params content) cofx))]
      (assoc-in message [:content :params] new-params)
      message)))
