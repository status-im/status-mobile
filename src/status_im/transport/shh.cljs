(ns ^{:doc "Whisper API and events for managing keys and posting messages"}
 status-im.transport.shh
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]
            [status-im.transport.message.transit :as transit]
            [status-im.transport.utils :as transport.utils]
            [taoensso.timbre :as log]))

(defn generate-sym-key-from-password
  [{:keys [web3 password on-success on-error]}]
  (.. web3
      -shh
      (generateSymKeyFromPassword password (fn [err resp]
                                             (if-not err
                                               (on-success resp)
                                               (on-error err))))))

(defn post-message
  [{:keys [web3 whisper-message on-success on-error]}]
  (.. web3
      -shh
      (extPost (clj->js whisper-message) (fn [err resp]
                                           (if-not err
                                             (on-success resp)
                                             (on-error err))))))

(defn handle-response [success-event error-event messages-count]
  (fn [err resp]
    (if-not err
      (if success-event
        (re-frame/dispatch (conj success-event resp messages-count))
        (log/debug :shh/post-success))
      (re-frame/dispatch [error-event err resp]))))

(defn send-direct-message! [web3 direct-message success-event error-event count]
  (.. web3
      -shh
      (sendDirectMessage
       (clj->js (update direct-message :payload (comp ethereum/utf8-to-hex
                                                      transit/serialize)))
       (handle-response success-event error-event count))))

(re-frame/reg-fx
 :shh/send-direct-message
 (fn [post-calls]
   (doseq [{:keys [web3 payload src dst success-event error-event]
            :or   {error-event :transport/send-status-message-error}} post-calls]
     (let [direct-message  {:pubKey  dst
                            :sig     src
                            :payload payload}]
       (send-direct-message! web3 direct-message success-event error-event 1)))))

(re-frame/reg-fx
 :shh/send-pairing-message
 (fn [params]
   (let [{:keys [web3 payload src success-event error-event]
          :or   {error-event :transport/send-status-message-error}} params
         message (clj->js {:sig src
                           :pubKey src
                           ;; Send to any device
                           :DH true
                           :payload (-> payload
                                        transit/serialize
                                        ethereum/utf8-to-hex)})]
     (.. web3
         -shh
         (sendDirectMessage
          message
          (handle-response success-event error-event 1))))))

(re-frame/reg-fx
 :shh/send-group-message
 (fn [params]
   (let [{:keys [web3 payload src dsts success-event error-event]
          :or   {error-event :transport/send-status-message-error}} params]
     (doseq [{:keys [public-key chat]} dsts]
       (let [message
             (clj->js {:pubKey public-key
                       :sig src
                       :payload (-> payload
                                    transit/serialize
                                    ethereum/utf8-to-hex)})]

         (.. web3
             -shh
             (sendDirectMessage
              message
              (handle-response success-event error-event (count dsts)))))))))

(defn send-public-message! [web3 message success-event error-event]
  (.. web3
      -shh
      (sendPublicMessage
       (clj->js message)
       (handle-response success-event error-event 1))))

(re-frame/reg-fx
 :shh/send-public-message
 (fn [post-calls]
   (doseq [{:keys [web3 payload src chat success-event error-event]
            :or   {error-event :transport/send-status-message-error}} post-calls]
     (let [message {:chat chat
                    :sig src
                    :payload (-> payload
                                 transit/serialize
                                 ethereum/utf8-to-hex)}]
       (send-public-message! web3 message success-event error-event)))))

(re-frame/reg-fx
 :shh/post
 (fn [post-calls]
   (doseq [{:keys [web3 message success-event error-event]
            :or   {error-event :transport/send-status-message-error}} post-calls]
     (post-message {:web3            web3
                    :whisper-message (update message :payload (comp ethereum/utf8-to-hex
                                                                    transit/serialize))
                    :on-success      (if success-event
                                       #(re-frame/dispatch (conj success-event % 1))
                                       #(log/debug :shh/post-success))
                    :on-error        #(re-frame/dispatch [error-event %])}))))

(defn get-sym-key
  [{:keys [web3 sym-key-id on-success on-error]}]
  (.. web3
      -shh
      (getSymKey sym-key-id (fn [err resp]
                              (if-not err
                                (on-success resp)
                                (on-error err))))))

(defn log-error [error]
  (log/error :shh/get-new-sym-key-error error))

(re-frame/reg-fx
 :shh/generate-sym-key-from-password
 (fn [args]
   (doseq [{:keys [web3 password on-success]} args]
     (generate-sym-key-from-password {:web3       web3
                                      :password   password
                                      :on-success (fn [sym-key-id]
                                                    (get-sym-key {:web3       web3
                                                                  :sym-key-id sym-key-id
                                                                  :on-success (fn [sym-key]
                                                                                (on-success sym-key sym-key-id))
                                                                  :on-error log-error}))
                                      :on-error   log-error}))))
