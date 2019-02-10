(ns status-im.chat.commands.sending
  (:require [status-im.constants :as constants]
            [status-im.chat.commands.protocol :as protocol]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.commands.input :as commands.input]
            [status-im.chat.models :as chat]
            [status-im.chat.models.message :as chat.message]
            [status-im.utils.fx :as fx]))

(defn- create-command-message
  "Create message map from chat-id, command & input parameters"
  [chat-id type parameter-map cofx]
  (let [command-path      (commands/command-id type)
        new-parameter-map (and (satisfies? protocol/EnhancedParameters type)
                               (protocol/enhance-send-parameters type parameter-map cofx))
        params            (merge (or new-parameter-map parameter-map)
                                 (when (satisfies? protocol/Extension type)
                                   {:extension-id (protocol/extension-id type)}))]
    {:chat-id      chat-id
     :content-type constants/content-type-command
     :content      {:chat-id      chat-id
                    :command-path command-path
                    :params       params}}))

(fx/defn validate-and-send
  "Validates and sends command in current chat"
  [{:keys [db] :as cofx} input-text {:keys [type params]} custom-params]
  (let [chat-id       (:current-chat-id db)
        parameter-map (merge (commands.input/parse-parameters params input-text) custom-params)]
    (if-let [validation-error (protocol/validate type parameter-map cofx)]
      ;; errors during validation
      {:db (chat/set-chat-ui-props db {:validation-messages validation-error})}
      ;; no errors
      (let [command-message (commands/enrich-command-message-for-events db (create-command-message chat-id type parameter-map cofx))]
        (if (satisfies? protocol/Yielding type)
          ;; yield control implemented, don't send the message
          (protocol/yield-control type command-message cofx)
          ;; no yield control, proceed with sending the command message
          (fx/merge cofx
                    #(protocol/on-send type command-message %)
                    (commands.input/set-command-reference nil)
                    (chat.message/send-message command-message)))))))

(fx/defn send
  "Sends command with given parameters in particular chat"
  [{:keys [db] :as cofx} chat-id {:keys [type]} parameter-map]
  (let [command-message (create-command-message chat-id type parameter-map cofx)]
    (fx/merge cofx
              #(protocol/on-send type (commands/enrich-command-message-for-events db command-message) %)
              (commands.input/set-command-reference nil)
              (chat.message/send-message command-message))))
