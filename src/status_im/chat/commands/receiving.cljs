(ns status-im.chat.commands.receiving
  (:require [status-im.chat.commands.protocol :as protocol]
            [status-im.utils.fx :as fx]))

(defn lookup-command-by-ref
  "Function which takes message object and looks up associated entry from the
  `id->command` map if it can be found"
  [{:keys [content]} id->command]
  (get id->command (:command-path content)))

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
