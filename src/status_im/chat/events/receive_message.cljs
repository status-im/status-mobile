(ns status-im.chat.events.receive-message
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.chat.models.message :as message-model]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.models.transactions :as wallet.transactions]))

;;;; Handlers

(re-frame.core/reg-fx
 :chat-received-message/add-fx
 (fn [messages]
   (re-frame/dispatch [:chat-received-message/add messages])))

(defn- filter-messages [messages cofx]
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
 message-model/receive-interceptors
 (fn [cofx [messages]]
   (message-model/receive-many (filter-messages messages cofx) cofx)))

