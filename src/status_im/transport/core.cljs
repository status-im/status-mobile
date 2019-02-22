(ns ^{:doc "API to init and stop whisper messaging"}
 status-im.transport.core
  (:require status-im.transport.filters
            [re-frame.core :as re-frame]
            [status-im.native-module.core :as status]
            [status-im.mailserver.core :as mailserver]
            [status-im.transport.message.core :as message]
            [status-im.transport.partitioned-topic :as transport.topic]
            [status-im.contact-code.core :as contact-code]
            [status-im.utils.publisher :as publisher]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]
            [taoensso.timbre :as log]
            status-im.transport.shh
            [status-im.utils.config :as config]))

(defn get-public-key-topics [chats]
  (keep (fn [[chat-id {:keys [topic sym-key one-to-one]}]]
          (cond (and (not sym-key) topic)
                {:topic   topic
                 :chat-id chat-id}

                ;; we have to listen the topic to which we are going to send
                ;; a message, otherwise the message will not match bloom
                (and config/partitioned-topic-enabled? one-to-one)
                {:topic   (transport.topic/partitioned-topic-hash chat-id)
                 :chat-id chat-id
                 :minPow 1
                 :callback (constantly nil)}))
        chats))

(defn set-node-info [{:keys [db]} node-info]
  {:db (assoc db :node-info node-info)})

(defn fetch-node-info []
  (let [args    {:jsonrpc "2.0"
                 :id      2
                 :method  "admin_nodeInfo"}
        payload (.stringify js/JSON (clj->js args))]
    (status/call-private-rpc payload
                             (handlers/response-handler #(re-frame/dispatch [:transport.callback/node-info-fetched %])
                                                        #(log/error "node-info: failed error" %)))))

(re-frame/reg-fx
 ::fetch-node-info
 (fn []
   (fetch-node-info)))

(fx/defn init-whisper
  "Initialises whisper protocol by:
  - adding fixed shh discovery filter
  - restoring existing symetric keys along with their unique filters
  - (optionally) initializing mailserver"
  [{:keys [db web3] :as cofx}]
  (when-let [public-key (get-in db [:account/account :public-key])]
    (let [public-key-topics (get-public-key-topics (:transport/chats db))
          discovery-topics (transport.topic/discovery-topics public-key)]
      (fx/merge cofx
                {:shh/add-discovery-filters
                 {:web3           web3
                  :private-key-id public-key
                  :topics         (concat public-key-topics
                                          (map
                                           (fn [discovery-topic]
                                             {:topic   discovery-topic
                                              :chat-id :discovery-topic})
                                           discovery-topics))}

                 ::fetch-node-info []
                 :shh/restore-sym-keys-batch
                 {:web3       web3
                  :transport  (keep (fn [[chat-id {:keys [topic sym-key]
                                                   :as   chat}]]
                                      (when (and topic sym-key)
                                        (assoc chat :chat-id chat-id)))
                                    (:transport/chats db))
                  :on-success #(re-frame/dispatch [::sym-keys-added %])}}
                (publisher/start-fx)
                (contact-code/init)
                (mailserver/connect-to-mailserver)
                (message/resend-contact-messages [])))))

;;TODO (yenda) remove once go implements persistence
;;Since symkeys are not persisted, we restore them via add sym-keys,
;;this is the callback that is called when a key has been restored for a particular chat.
;;it saves the sym-key-id in app-db to send messages later
;;and starts a filter to receive messages
(handlers/register-handler-fx
 ::sym-keys-added
 (fn [{:keys [db]} [_ keys]]
   (log/debug "PERF" ::sym-keys-added (count keys))
   (let [web3 (:web3 db)
         chats (:transport/chats db)
         {:keys [updated-chats filters]}
         (reduce
          (fn [{:keys [updated-chats filters]} chat]
            (let [{:keys [chat-id sym-key-id]} chat
                  {:keys [topic one-to-one]} (get updated-chats chat-id)]
              {:updated-chats (assoc-in updated-chats
                                        [chat-id :sym-key-id] sym-key-id)
               :filters       (conj filters {:sym-key-id sym-key-id
                                             :topic      topic
                                             :chat-id    chat-id
                                             :one-to-one one-to-one})}))
          {:updated-chats chats
           :filters       []}
          keys)]
     {:db              (assoc db :transport/chats updated-chats)
      :shh/add-filters {:web3    web3
                        :filters filters}})))

;;TODO (yenda) uncomment and rework once go implements persistence
#_(doseq [[chat-id {:keys [sym-key-id topic] :as chat}] transport]
    (when sym-key-id
      (filters/add-filter! web3
                           {:symKeyID sym-key-id
                            :topics   [topic]}
                           (fn [js-error js-message]
                             (re-frame/dispatch [:protocol/receive-whisper-message js-error js-message chat-id])))))

(fx/defn stop-whisper
  "Stops whisper protocol by removing all existing shh filters
  It is necessary to remove the filters because status-go there isn't currently a logout feature in status-go
  to clean-up after logout. When logging out of account A and logging in account B, account B would receive
  account A messages without this."
  [{:keys [db] :as cofx} callback]
  (let [{:transport/keys [filters]} db]
    (fx/merge
     cofx

     {:shh/remove-filters {:filters   (mapcat (fn [[chat-id chat-filters]]
                                                (map (fn [filter]
                                                       [chat-id filter])
                                                     chat-filters))
                                              filters)
                           :callback callback}}
     (publisher/stop-fx))))
