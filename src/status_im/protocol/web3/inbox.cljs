(ns status-im.protocol.web3.inbox
  (:require [re-frame.core :as re-frame]
            [status-im.native-module.core :as status]
            [status-im.utils.handlers :as handlers]
            [status-im.protocol.web3.utils :as web3.utils]
            [taoensso.timbre :as log]))

(defn request-messages [web3 wnode topic to from sym-key-id success-fn error-fn]
  (log/info "offline inbox: sym-key-id" sym-key-id)
  (let [opts (merge {:mailServerPeer wnode
                     :topic          topic
                     :symKeyID       sym-key-id}
                    (when from {:from from})
                    (when to {:to to}))]
    (log/info "offline inbox: request-messages request")
    (log/info "offline inbox: request-messages args" (pr-str opts))
    (.requestMessages (web3.utils/shh web3)
                      (clj->js opts)
                      (fn [err resp]
                        (if-not err
                          (success-fn resp)
                          (error-fn err))))))

(re-frame/reg-fx
  ::request-messages
  (fn [{:keys [wnode topic to from sym-key-id web3]}]
    (request-messages web3
                      wnode
                      topic
                      to
                      from
                      sym-key-id
                      #(log/info "offline inbox: request-messages response" %)
                      #(re-frame/dispatch [:inbox/request-messages sym-key-id %]))))

(handlers/register-handler-fx
  :inbox/request-messages
  (fn [{:keys [db]} [_ sym-key-id error]]
    (if error
      (log/error "Error requesting messages to mailserver " error)
      (log/info "Requesting messages to mailserver"))
    (let [web3 (:web3 db)
          wnode-id (get db :inbox/wnode)
          wnode    (get-in db [:inbox/wnodes wnode-id :address])
          topic    (:inbox/topic db)
          to       nil
          from     nil]
      {::request-messages {:wnode      wnode
                           :topic      topic
                           :to         to
                           :from       from
                           :sym-key-id sym-key-id
                           :web3       web3}})))
