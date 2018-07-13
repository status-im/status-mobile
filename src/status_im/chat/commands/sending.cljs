(ns status-im.chat.commands.sending
  (:require [status-im.constants :as constants]
            [status-im.chat.commands.protocol :as protocol]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.models :as chat-model]
            [status-im.chat.models.input :as input-model]
            [status-im.chat.models.message :as message-model]
            [status-im.utils.handlers-macro :as handlers-macro]))

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
  (let [command-path               (commands/command-id type)
        ;; TODO(janherich) this is just for backward compatibility, can be removed later
        {:keys [content content-type]} (new->old command-path parameter-map)]
    {:chat-id      chat-id
     :content-type content-type
     :content      (merge {:command-path command-path
                           :params       (if (satisfies? protocol/EnhancedParameters type)
                                           (protocol/enhance-parameters type parameter-map cofx)
                                           parameter-map)}
                          content)}))

(defn validate-and-send
  "Validates and sends command in current chat"
  [input-text {:keys [type params] :as command} {:keys [db now random-id] :as cofx}]
  (let [chat-id       (:current-chat-id db)
        parameter-map (commands/parse-parameters params input-text)]
    (if-let [validation-error (protocol/validate type parameter-map cofx)]
      ;; errors during validation
      {:db (chat-model/set-chat-ui-props db {:validation-messages  validation-error
                                             :sending-in-progress? false})}
      ;; no errors, define clean-up effects which will have to be performed in all cases
      (let [cleanup-fx (handlers-macro/merge-fx cofx
                                                {:db (chat-model/set-chat-ui-props
                                                      db {:sending-in-progress? false})}
                                                (input-model/set-chat-input-text nil))]
        (if (satisfies? protocol/Yielding type)
          ;; yield control implemented, don't send the message
          (handlers-macro/merge-fx cofx
                                   cleanup-fx
                                   (protocol/yield-control type parameter-map))
          ;; no yield control, proceed with sending the command message
          (let [command-message (create-command-message chat-id type parameter-map cofx)]
            (handlers-macro/merge-fx cofx
                                     cleanup-fx
                                     (protocol/on-send type command-message)
                                     (input-model/set-chat-input-metadata nil)
                                     (message-model/send-message command-message))))))))

(defn send
  "Sends command with given parameters in particular chat"
  [chat-id {:keys [type]} parameter-map cofx]
  (let [command-message (create-command-message chat-id type parameter-map cofx)]
    (handlers-macro/merge-fx cofx
                             (protocol/on-send type command-message)
                             (input-model/set-chat-input-metadata nil)
                             (message-model/send-message command-message))))
