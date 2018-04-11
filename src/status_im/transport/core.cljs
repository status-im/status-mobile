(ns ^{:doc "API to init and stop whisper messaging"}
    status-im.transport.core
  (:require [cljs.spec.alpha :as spec]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.transport.message.core :as message]
            [status-im.transport.filters :as filters]
            [status-im.transport.utils :as transport.utils]
            [taoensso.timbre :as log]
            [status-im.transport.inbox :as inbox]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.transport.db :as transport.db]))

(defn init-whisper
  "Initialises whisper protocol by:
  - adding fixed shh discovery filter
  - restoring existing symetric keys along with their unique filters
  - (optionally) initializing offline inboxing"
  [current-account-id {:keys [db web3] :as cofx}]
  (log/debug :init-whisper)
  (when-let [public-key (get-in db [:accounts/accounts current-account-id :public-key])]
    (let [sym-key-added-callback (fn [chat-id sym-key sym-key-id]
                                   (re-frame/dispatch [:sym-key-added {:chat-id    chat-id
                                                                        :sym-key    sym-key
                                                                        :sym-key-id sym-key-id}]))
          topic (transport.utils/get-topic constants/contact-discovery)]
      (handlers-macro/merge-fx cofx
                         {:shh/add-discovery-filter {:web3           web3
                                                     :private-key-id public-key
                                                     :topic topic}
                          :shh/restore-sym-keys {:web3       web3
                                                 :transport  (:transport/chats db)
                                                 :on-success sym-key-added-callback}}
                         (inbox/initialize-offline-inbox)))))

;;TODO (yenda) uncomment and rework once go implements persistence
#_(doseq [[chat-id {:keys [sym-key-id topic] :as chat}] transport]
    (when sym-key-id
      (filters/add-filter! web3
                           {:symKeyID sym-key-id
                            :topics   [topic]}
                           (fn [js-error js-message]
                             (re-frame/dispatch [:protocol/receive-whisper-message js-error js-message chat-id])))))

(def unsubscribe-from-chat transport.utils/unsubscribe-from-chat)

(defn stop-whisper
  "Stops whisper protocol by removing all existing shh filters
  It is necessary to remove the filters because status-go there isn't currently a logout feature in status-go
  to clean-up after logout. When logging out of account A and logging in account B, account B would receive
  account A messages without this."
  [{:keys [db]}]
  (let [{:transport/keys [chats discovery-filter]} db
        chat-filters                               (mapv :filter (vals chats))
        all-filters                                (conj chat-filters discovery-filter)]
    {:shh/remove-filters all-filters}))
