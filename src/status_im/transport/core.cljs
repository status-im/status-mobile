(ns ^{:doc "API to init and stop whisper messaging"}
 status-im.transport.core
  (:require status-im.transport.filters
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.data-store.transport :as transport-store]
            [status-im.mailserver.core :as mailserver]
            [status-im.transport.message.core :as message]
            [status-im.transport.shh :as shh]
            [status-im.transport.utils :as transport.utils]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]
            [taoensso.timbre :as log]))

(fx/defn init-whisper
  "Initialises whisper protocol by:
  - adding fixed shh discovery filter
  - restoring existing symetric keys along with their unique filters
  - (optionally) initializing mailserver"
  [{:keys [db web3] :as cofx}]
  (log/debug :init-whisper)
  (when-let [public-key (get-in db [:account/account :public-key])]
    (let [topic (transport.utils/get-topic constants/contact-discovery)]
      (fx/merge cofx
                {:shh/add-discovery-filter
                 {:web3           web3
                  :private-key-id public-key
                  :topic          topic}
                 :shh/restore-sym-keys-batch
                 {:web3       web3
                  :transport  (keep (fn [[chat-id {:keys [topic sym-key]
                                                   :as   chat}]]
                                      (when (and topic sym-key)
                                        (assoc chat :chat-id chat-id)))
                                    (:transport/chats db))
                  :on-success #(re-frame/dispatch [::sym-keys-added %])}}
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
                  {:keys [topic]} (get updated-chats chat-id)]
              {:updated-chats (assoc-in updated-chats
                                        [chat-id :sym-key-id] sym-key-id)
               :filters       (conj filters {:sym-key-id sym-key-id
                                             :topic      topic
                                             :chat-id    chat-id})}))
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
  [{:keys [db]} callback]
  (let [{:transport/keys [filters]} db]
    {:shh/remove-filters [filters callback]}))
