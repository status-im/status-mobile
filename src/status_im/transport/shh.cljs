(ns ^{:doc "Whisper API and events for managing keys and posting messages"}
 status-im.transport.shh
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]
            [status-im.transport.message.transit :as transit]
            [status-im.transport.utils :as transport.utils]
            [taoensso.timbre :as log]
            [status-im.ethereum.json-rpc :as json-rpc]))

(defn generate-sym-key-from-password
  [{:keys [password on-success on-error]}]
  (json-rpc/call {:method "shh_generateSymKeyFromPassword"
                  :params [password]
                  :on-success on-success
                  :on-error on-error}))

(defn post-message
  [{:keys [whisper-message on-success on-error]}]
  (json-rpc/call {:method "shh_generateSymKeyFromPassword"
                  :params [whisper-message]
                  :on-success on-success
                  :on-error on-error}))

(defn send-direct-message!
  [direct-message success-event error-event messages-count]
  (json-rpc/call {:method "shhext_sendDirectMessage"
                  :params [(update direct-message :payload (comp ethereum/utf8-to-hex
                                                                 transit/serialize))]
                  :on-success #(if success-event
                                 (re-frame/dispatch (conj success-event % messages-count))
                                 (log/debug :shh/post-success))
                  :on-error #(re-frame/dispatch [error-event %])}))

(re-frame/reg-fx
 :shh/send-direct-message
 (fn [post-calls]
   (doseq [{:keys [payload src dst success-event error-event]
            :or   {error-event :transport/send-status-message-error}} post-calls]
     (let [direct-message  {:pubKey  dst
                            :sig     src
                            :payload payload}]
       (send-direct-message! direct-message success-event error-event 1)))))

(re-frame/reg-fx
 :shh/send-pairing-message
 (fn [params]
   (let [{:keys [payload src success-event error-event]
          :or   {error-event :transport/send-status-message-error}} params]
     (json-rpc/call {:method "shhext_sendDirectMessage"
                     :params [{:sig src
                               :pubKey src
                               ;; Send to any device
                               :DH true
                               :payload (-> payload
                                            transit/serialize
                                            ethereum/utf8-to-hex)}]
                     :on-success #(if success-event
                                    (re-frame/dispatch (conj success-event % 1))
                                    (log/debug :shh/post-success))
                     :on-error #(re-frame/dispatch [error-event %])}))))

(re-frame/reg-fx
 :shh/send-group-message
 (fn [params]
   (let [{:keys [payload src dsts success-event error-event]
          :or   {error-event :transport/send-status-message-error}} params
         payload (-> payload
                     transit/serialize
                     ethereum/utf8-to-hex)]
     (doseq [{:keys [public-key chat]} dsts]
       (let [message {:pubKey public-key
                      :sig src
                      :payload payload}]
         (json-rpc/call {:method "shhext_sendDirectMessage"
                         :params [message]
                         :on-success #(if success-event
                                        (re-frame/dispatch (conj success-event % (count dsts)))
                                        (log/debug :shh/post-success))
                         :on-error #(re-frame/dispatch [error-event %])}))))))

(defn send-public-message! [message success-event error-event]
  (json-rpc/call {:method "shhext_sendPublicMessage"
                  :params [message]
                  :on-success #(if success-event
                                 (re-frame/dispatch (conj success-event % 1))
                                 (log/debug :shh/post-success))
                  :on-error #(re-frame/dispatch [error-event %])}))

(re-frame/reg-fx
 :shh/send-public-message
 (fn [post-calls]
   (doseq [{:keys [payload src chat success-event error-event]
            :or   {error-event :transport/send-status-message-error}} post-calls]
     (let [message {:chat chat
                    :sig src
                    :payload (-> payload
                                 transit/serialize
                                 ethereum/utf8-to-hex)}]
       (send-public-message! message success-event error-event)))))

(re-frame/reg-fx
 :shh/post
 (fn [post-calls]
   (doseq [{:keys [message success-event error-event]
            :or   {error-event :transport/send-status-message-error}} post-calls]
     (post-message {:whisper-message (update message :payload (comp ethereum/utf8-to-hex
                                                                    transit/serialize))
                    :on-success      (if success-event
                                       #(re-frame/dispatch (conj success-event % 1))
                                       #(log/debug :shh/post-success))
                    :on-error        #(re-frame/dispatch [error-event %])}))))

(defn get-sym-key
  [{:keys [sym-key-id on-success on-error]}]
  (json-rpc/call {:method "shh_getSymKey"
                  :params [sym-key-id]
                  :on-success on-success
                  :on-error on-error}))

(defn log-error [error]
  (log/error :shh/get-new-sym-key-error error))

(re-frame/reg-fx
 :shh/generate-sym-key-from-password
 (fn [args]
   (doseq [{:keys [password on-success]} args]
     (generate-sym-key-from-password {:password   password
                                      :on-success (fn [sym-key-id]
                                                    (get-sym-key {:sym-key-id sym-key-id
                                                                  :on-success (fn [sym-key]
                                                                                (on-success sym-key sym-key-id))
                                                                  :on-error log-error}))
                                      :on-error   log-error}))))
