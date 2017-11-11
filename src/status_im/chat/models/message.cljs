(ns status-im.chat.models.message
  (:require [re-frame.core :as re-frame]
            [status-im.chat.events.commands :as commands-events]
            [status-im.chat.events.input :as input-events]
            [status-im.chat.events.messages :as messages-events]
            [status-im.chat.events.requests :as requests-events]
            [status-im.chat.models :as model]
            [status-im.chat.models.unviewed-messages :as unviewed-messages-model]
            [status-im.chat.utils :as chat-utils]
            [status-im.constants :as const]
            [status-im.utils.clocks :as clocks]
            [taoensso.timbre :as log]))

(def receive-interceptors
  [(re-frame/inject-cofx :message-exists?)
   (re-frame/inject-cofx :get-last-stored-message)
   (re-frame/inject-cofx :get-db-message)
   (re-frame/inject-cofx :pop-up-chat?)
   (re-frame/inject-cofx :get-last-clock-value)
   (re-frame/inject-cofx :random-id)
   (re-frame/inject-cofx :get-stored-chat)
   re-frame/trim-v])

(defn- get-current-identity
  [{:accounts/keys [accounts current-account-id]}]
  (get-in accounts [current-account-id :public-key]))

(defn receive
  [{:keys [db message-exists? get-last-stored-message get-stored-chat gfy-generator
           pop-up-chat? get-last-clock-value now get-db-message] :as cofx} messages]
  (reduce
    (fn [{:keys [db] :as current-cofx} message]
      (let [{:keys [from group-id chat-id content-type message-id timestamp clock-value]
             :or   {clock-value 0}} message
            chat-identifier  (or group-id chat-id from)
            current-identity (get-current-identity db)]

        (if (and (not (message-exists? message-id))
              (not= from current-identity)
              (pop-up-chat? chat-identifier))

          (let [group-chat?      (not (nil? group-id))
                enriched-message (assoc (chat-utils/check-author-direction
                                          (get-last-stored-message chat-identifier)
                                          message)
                                   :chat-id chat-identifier
                                   :timestamp (or timestamp now)
                                   :clock-value (clocks/receive
                                                  clock-value
                                                  (get-last-clock-value chat-identifier)))]
            (cond->
              (model/upsert-chat (assoc current-cofx :get-stored-chat get-stored-chat
                                                     :gfy-generator gfy-generator
                                                     :now now)
                {:chat-id    chat-identifier
                 :group-chat group-chat?})

              true
              (as-> fx'
                (merge fx' (messages-events/add-message (assoc current-cofx :db (:db fx')) chat-identifier enriched-message)))

              (get-in enriched-message [:content :command])
              (as-> fx'
                (merge fx' (commands-events/get-preview
                             (assoc current-cofx :db (:db fx')
                                                 :get-db-message get-db-message)
                             enriched-message)))

              (= (:content-type enriched-message) const/content-type-command-request)
              (as-> fx'
                (merge fx' (requests-events/add-request (assoc current-cofx :db (:db fx')) chat-identifier enriched-message)))

              true
              (as-> fx'
                (assoc fx' :db (input-events/update-suggestions (:db fx'))
                           :dispatch-n [[:chat-commands/jail-request-data enriched-message :short-preview]]))))
          current-cofx)))
    {:db db}
    messages))