(ns ^{:doc "Whisper API and events for managing keys and posting messages"}
 status-im.transport.shh
  (:require [taoensso.timbre :as log]
            [re-frame.core :as re-frame]
            [status-im.transport.utils :as transport.utils]
            [status-im.transport.message.transit :as transit]))

(defn get-new-key-pair [{:keys [web3 on-success on-error]}]
  (if web3
    (.. web3
        -shh
        (newKeyPair (fn [err resp]
                      (if-not err
                        (on-success resp)
                        (on-error err)))))
    (on-error "web3 not available.")))

(re-frame/reg-fx
 :shh/get-new-key-pair
 (fn [{:keys [web3 success-event error-event]}]
   (get-new-key-pair {:web3       web3
                      :on-success #(re-frame/dispatch [success-event %])
                      :on-error   #(re-frame/dispatch [error-event %])})))

(defn get-public-key [{:keys [web3 key-pair-id on-success on-error]}]
  (if (and web3 key-pair-id)
    (.. web3
        -shh
        (getPublicKey key-pair-id (fn [err resp]
                                    (if-not err
                                      (on-success resp)
                                      (on-error err)))))
    (on-error "web3 or key-pair id not available.")))

(re-frame/reg-fx
 :shh/get-public-key
 (fn [{:keys [web3 key-pair-id success-event error-event]}]
   (get-public-key {:web3        web3
                    :key-pair-id key-pair-id
                    :on-success  #(re-frame/dispatch [success-event %])
                    :on-error    #(re-frame/dispatch [error-event %])})))

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

(defn handle-response [success-event error-event]
  (fn [err resp]
    (if-not err
      (if success-event
        (re-frame/dispatch (conj success-event resp))
        (log/debug :shh/post-success))
      (re-frame/dispatch [error-event err resp]))))

(re-frame/reg-fx
 :shh/send-direct-message
 (fn [post-calls]
   (doseq [{:keys [web3 payload src dst success-event error-event]
            :or   {error-event :protocol/send-status-message-error}} post-calls]
     (let [direct-message (clj->js {:pubKey dst
                                    :sig src
                                    :payload (-> payload
                                                 transit/serialize
                                                 transport.utils/from-utf8)})]
       (.. web3
           -shh
           (sendDirectMessage
            direct-message
            (handle-response success-event error-event)))))))

(re-frame/reg-fx
 :shh/send-group-message
 (fn [params]
   (let [{:keys [web3 payload src dsts success-event error-event]
          :or   {error-event :protocol/send-status-message-error}} params
         message (clj->js {:pubKeys dsts
                           :sig src
                           :payload (-> payload
                                        transit/serialize
                                        transport.utils/from-utf8)})]
     (.. web3
         -shh
         (sendGroupMessage
          message
          (handle-response success-event error-event))))))

(re-frame/reg-fx
 :shh/send-public-message
 (fn [post-calls]
   (doseq [{:keys [web3 payload src chat success-event error-event]
            :or   {error-event :protocol/send-status-message-error}} post-calls]
     (let [message (clj->js {:chat chat
                             :sig src
                             :payload (-> payload
                                          transit/serialize
                                          transport.utils/from-utf8)})]
       (.. web3
           -shh
           (sendPublicMessage
            message
            (handle-response success-event error-event)))))))

(re-frame/reg-fx
 :shh/post
 (fn [post-calls]
   (doseq [{:keys [web3 message success-event error-event]
            :or   {error-event :protocol/send-status-message-error}} post-calls]
     (post-message {:web3            web3
                    :whisper-message (update message :payload (comp transport.utils/from-utf8
                                                                    transit/serialize))
                    :on-success      (if success-event
                                       #(re-frame/dispatch (conj success-event %))
                                       #(log/debug :shh/post-success))
                    :on-error        #(re-frame/dispatch [error-event %])}))))

(defn add-sym-key
  [{:keys [web3 sym-key on-success on-error]}]
  (.. web3
      -shh
      (addSymKey sym-key (fn [err resp]
                           (if-not err
                             (on-success resp)
                             (on-error err))))))

(defn get-sym-key
  [{:keys [web3 sym-key-id on-success on-error]}]
  (.. web3
      -shh
      (getSymKey sym-key-id (fn [err resp]
                              (if-not err
                                (on-success resp)
                                (on-error err))))))

(defn new-sym-key
  [{:keys [web3 on-success on-error]}]
  (.. web3
      -shh
      (newSymKey (fn [err resp]
                   (if-not err
                     (on-success resp)
                     (on-error err))))))

(defn log-error [error]
  (log/error :shh/get-new-sym-key-error error))

;;TODO (yenda) remove once go implements persistence
(re-frame/reg-fx
 :shh/restore-sym-keys
 (fn [{:keys [web3 transport on-success]}]
   (doseq [[chat-id {:keys [sym-key]}] transport]
     (when sym-key
       (add-sym-key {:web3       web3
                     :sym-key    sym-key
                     :on-success (fn [sym-key-id]
                                   (on-success chat-id sym-key sym-key-id))
                     :on-error   log-error})))))

(defn add-new-sym-key [{:keys [web3 sym-key on-success]}]
  (add-sym-key {:web3       web3
                :sym-key    sym-key
                :on-success (fn [sym-key-id]
                              (on-success sym-key sym-key-id))
                :on-error   log-error}))

(re-frame/reg-fx
 :shh/add-new-sym-keys
 (fn [args]
   (doseq [add-new-sym-key-params args]
     (add-new-sym-key add-new-sym-key-params))))

(re-frame/reg-fx
 :shh/get-new-sym-keys
 (fn [args]
   (doseq [{:keys [web3 on-success]} args]
     (new-sym-key {:web3       web3
                   :on-success (fn [sym-key-id]
                                 (get-sym-key {:web3       web3
                                               :sym-key-id sym-key-id
                                               :on-success (fn [sym-key]
                                                             (on-success sym-key sym-key-id))
                                               :on-error   log-error}))
                   :on-error   log-error}))))

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
