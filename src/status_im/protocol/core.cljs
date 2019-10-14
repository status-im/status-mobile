(ns status-im.protocol.core
  (:require [status-im.constants :as constants]
            [status-im.transport.core :as transport]
            [status-im.utils.fx :as fx]))

(defn add-custom-mailservers
  [db custom-mailservers]
  (reduce (fn [db {:keys [fleet] :as mailserver}]
            (let [{:keys [id] :as mailserver}
                  (-> mailserver
                      (update :id keyword)
                      (dissoc :fleet)
                      (assoc :user-defined true))]
              (assoc-in db
                        [:mailserver/mailservers (keyword fleet) id]
                        mailserver)))
          db
          custom-mailservers))

(defn add-mailserver-topics
  [db mailserver-topics]
  (assoc db
         :mailserver/topics
         (reduce (fn [acc {:keys [topic chat-ids]
                           :as mailserver-topic}]
                   (assoc acc topic
                          (update mailserver-topic :chat-ids
                                  #(into #{} %))))
                 {}
                 mailserver-topics)))

(defn add-mailserver-ranges
  [db mailserver-ranges]
  (assoc db
         :mailserver/ranges
         (reduce (fn [acc {:keys [chat-id] :as range}]
                   (assoc acc chat-id range))
                 {}
                 mailserver-ranges)))

(fx/defn initialize-protocol
  {:events [::initialize-protocol]}
  [{:keys [db] :as cofx}
   {:keys [mailserver-ranges mailserver-topics mailservers] :as data}]
  ;; NOTE: we need to wait for `:mailservers` `:mailserver-ranges` and
  ;; `:mailserver-topics` before we can proceed to init whisper
  ;; since those are populated by separate events, we check here
  ;; that everything has been initialized before moving forward
  (let [initialization-protocol (apply conj (get db :initialization-protocol #{})
                                       (keys data))
        initialization-complete? (= initialization-protocol
                                    #{:mailservers
                                      :mailserver-ranges
                                      :mailserver-topics
                                      :default-mailserver})]
    (fx/merge cofx
              {:db (cond-> db
                     mailserver-ranges
                     (add-mailserver-ranges mailserver-ranges)
                     mailserver-topics
                     (add-mailserver-topics mailserver-topics)
                     mailservers
                     (add-custom-mailservers mailservers)
                     initialization-complete?
                     (assoc :rpc-url constants/ethereum-rpc-url)
                     initialization-complete?
                     (dissoc :initialization-protocol)
                     (not initialization-complete?)
                     (assoc :initialization-protocol initialization-protocol))}
              (when initialization-complete?
                (transport/init-whisper)))))
