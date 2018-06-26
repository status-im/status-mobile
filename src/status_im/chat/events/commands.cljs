(ns status-im.chat.events.commands
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.chat.models.message :as models.message]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.handlers :as handlers]
            [status-im.i18n :as i18n]
            [status-im.utils.platform :as platform]
            [status-im.chat.events.shortcuts :as shortcuts]
            [status-im.utils.ethereum.tokens :as tokens]))

;;;; Helper fns

;;TODO(goranjovic): currently we only allow tokens which are enabled in Manage assets here
;; because balances are only fetched for them. Revisit this decision with regard to battery/network consequences
;; if we were to update all balances.
(defn- allowed-assets [chain account]
  (let [visible-token-symbols (get-in account [:settings :wallet :visible-tokens chain])]
    (->> (tokens/tokens-for chain)
         (filter #(not (:nft? %)))
         (filter #(contains? visible-token-symbols (:symbol %)))
         (map #(vector (-> % :symbol clojure.core/name)
                       (:decimals %)))
         (into {"ETH" 18}))))

(defn- generate-context
  "Generates context for jail call"
  [account current-account-id chat-id group-chat? to chain]
  (merge {:platform       platform/os
          :network        chain
          :from           current-account-id
          :to             to
          :allowed-assets (clj->js (allowed-assets chain account))
          :chat           {:chat-id    chat-id
                           :group-chat (boolean group-chat?)}}
         i18n/delimeters))

(defn request-command-message-data
  "Requests command message data from jail"
  [{:contacts/keys [contacts] :account/keys [account] :keys [chain] :as db}
   {{:keys [command command-scope-bitmask bot params type]} :content
    :keys [chat-id group-id] :as message}
   {:keys [data-type] :as opts}]
  (if-not (get contacts bot) ;; bot is not even in contacts, do nothing
    {:db db}
    (if (get-in contacts [bot :jail-loaded?])
      (let [path        [(if (= :response (keyword type)) :responses :commands)
                         [command command-scope-bitmask]
                         data-type]
            to          (get-in contacts [chat-id :address])
            address     (get-in db [:account/account :address])
            jail-params {:parameters params
                         :context    (generate-context account address chat-id (models.message/group-message? message) to chain)}]
        {:db        db
         :call-jail [{:jail-id                bot
                      :path                   path
                      :params                 jail-params
                      :callback-event-creator (fn [jail-response]
                                                [::jail-command-data-response
                                                 jail-response message opts])}]})
      {:db (update-in db [:contacts/contacts bot :jail-loaded-events]
                      conj [:request-command-message-data message opts])})))

;;;; Handlers

(handlers/register-handler-fx
 ::jail-command-data-response
 [re-frame/trim-v]
 (fn [{:keys [db]} [{{:keys [returned]} :result} {:keys [chat-id]} {:keys [proceed-event-creator]}]]
   (when proceed-event-creator
     {:dispatch (proceed-event-creator returned)})))

(defn short-preview? [opts]
  (= :short-preview (:data-type opts)))

(handlers/register-handler-fx
 :request-command-message-data
 [re-frame/trim-v (re-frame/inject-cofx :data-store/get-local-storage-data)]
 (fn [{:keys [db]} [message opts]]
   (if (and (short-preview? opts)
            (shortcuts/shortcut-override? message))
     (shortcuts/shortcut-override-fx db message)
     (request-command-message-data db message opts))))
