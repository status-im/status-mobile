(ns status-im.chat.commands.sending
  (:require [status-im.constants :as constants]
            [status-im.chat.commands.protocol :as protocol]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.commands.input :as commands.input]
            [status-im.chat.models :as chat]
            [status-im.chat.models.message :as chat.message]
            [status-im.utils.fx :as fx]))

;; TODO(janherich) remove after couple of releases when danger of messages
;; with old command references will be low
(defn- new->old
  [path parameter-map]
  (get {["send" #{:personal-chats}]    {:content {:command-ref ["transactor" :command 83 "send"]
                                                  :command "send"
                                                  :bot "transactor"
                                                  :command-scope-bitmask 83}
                                        :content-type constants/content-type-command}
        ["request" #{:personal-chats}] {:content {:command-ref ["transactor" :command 83 "request"]
                                                  :request-command-ref ["transactor" :command 83 "send"]
                                                  :command "request"
                                                  :request-command "send"
                                                  :bot "transactor"
                                                  :command-scope-bitmask 83
                                                  :prefill [(get parameter-map :asset)
                                                            (get parameter-map :amount)]}
                                        :content-type constants/content-type-command-request}}
       path
       {:content-type constants/content-type-command}))

(defn- create-command-message
  "Create message map from chat-id, command & input parameters"
  [chat-id type parameter-map cofx]
  (let [command-path                   (commands/command-id type)
        ;; TODO(janherich) this is just for backward compatibility, can be removed later
        {:keys [content content-type]} (new->old command-path parameter-map)
        new-parameter-map              (and (satisfies? protocol/EnhancedParameters type)
                                            (protocol/enhance-send-parameters type parameter-map cofx))]
    {:chat-id      chat-id
     :content-type content-type
     :content      (merge {:command-path command-path
                           :params       (or new-parameter-map parameter-map)}
                          content)}))

(fx/defn validate-and-send
  "Validates and sends command in current chat"
  [{:keys [db now] :as cofx} input-text {:keys [type params] :as command}]
  (let [chat-id       (:current-chat-id db)
        parameter-map (commands.input/parse-parameters params input-text)]
    (if-let [validation-error (protocol/validate type parameter-map cofx)]
      ;; errors during validation
      {:db (chat/set-chat-ui-props db {:validation-messages  validation-error})}
      ;; no errors
      (if (satisfies? protocol/Yielding type)
        ;; yield control implemented, don't send the message
        (protocol/yield-control type parameter-map cofx)
        ;; no yield control, proceed with sending the command message
        (let [command-message (create-command-message chat-id type parameter-map cofx)]
          (fx/merge cofx
                    #(protocol/on-send type command-message %)
                    (commands.input/set-command-reference nil)
                    (chat.message/send-message command-message)))))))

(fx/defn send
  "Sends command with given parameters in particular chat"
  [cofx chat-id {:keys [type]} parameter-map]
  (let [command-message (create-command-message chat-id type parameter-map cofx)]
    (fx/merge cofx
              #(protocol/on-send type command-message %)
              (commands.input/set-command-reference nil)
              (chat.message/send-message command-message))))
