(ns status-im.chat.events.receive-message
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.chat.models.message :as message-model]
            [status-im.utils.handlers :as handlers]
            [status-im.models.transactions :as wallet.transactions]))

;;;; Handlers

(re-frame.core/reg-fx
 :chat-received-message/add-fx
 (fn [messages]
   (re-frame/dispatch [:chat-received-message/add messages])))

(defn- filter-messages [cofx messages]
  (:accumulated (reduce (fn [{:keys [seen-ids] :as acc}
                             {:keys [message-id] :as message}]
                          (if (and (message-model/add-to-chat? cofx message)
                                   (not (seen-ids message-id)))
                            (-> acc
                                (update :seen-ids conj message-id)
                                (update :accumulated conj message))
                            acc))
                        {:seen-ids    #{}
                         :accumulated []}
                        messages)))

(handlers/register-handler-fx
 :chat-received-message/add
 [(re-frame/inject-cofx :random-id-generator)]
 (fn [cofx [_ messages]]
   (message-model/receive-many cofx (filter-messages cofx messages))))
