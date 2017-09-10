(ns status-im.protocol.listeners
  (:require [cljs.reader :as r]
            [status-im.protocol.ack :as ack]
            [status-im.protocol.web3.utils :as u]
            [status-im.protocol.encryption :as e]
            [taoensso.timbre :as log]
            [status-im.utils.hex :as i]))

(defn create-error [description]
  (log/debug :parse-payload-error description)
  {:error description})

(defn- parse-payload [payload]
  (log/debug :parse-payload)
  (try
    ;; todo figure why we have to call to-utf8 twice
    (let [payload'   (u/to-utf8 payload)
          payload''  (r/read-string payload')
          payload''' (if (map? payload'')
                       payload''
                       (r/read-string (u/to-utf8 payload')))]
      (if (map? payload''')
        {:payload payload'''}
        (create-error (str "Invalid payload type " (type payload''')))))
    (catch :default err
      (create-error err))))

(defn- decrypt [key content]
  (try
    {:content (r/read-string (e/decrypt key content))}
    (catch :default err
      (log/debug :decrypt-error err)
      {:error err})))

(defn- parse-content [key {:keys [content]} was-encrypted?]
  (log/debug :parse-content
         "Key exists:" (not (nil? key))
         "Content exists:" (not (nil? content)))
  (if (and (not was-encrypted?) key content)
    (decrypt key content)
    {:content content}))

(defn message-listener
  [{:keys [web3 identity callback keypair]}]
  (fn [error js-message]
    ;; todo handle error
    (when error
      (log/debug :listener-error error))
    (when-not error
      (log/debug :message-received (js->clj js-message))
      (let [{:keys [sig payload recipientPublicKey] :as message}
            (js->clj js-message :keywordize-keys true)

            {{:keys [type ack?] :as payload'} :payload
             payload-error                    :error}
            (parse-payload payload)]
        (when (and (not payload-error)
                   (or (not= (i/normalize-hex identity)
                             (i/normalize-hex sig))
                       ;; allow user to receive his own discoveries
                       (= type :discover)))
          (let [{:keys [content error]} (parse-content (:private keypair)
                                                       payload'
                                                       (and (not= "0x0" recipientPublicKey)
                                                            (not= "" recipientPublicKey)
                                                            (not (nil? recipientPublicKey))))]
            (if error
              (log/debug :failed-to-handle-message error)
              (let [payload'' (assoc payload' :content content)
                    message'  (assoc message :payload payload''
                                             :to recipientPublicKey
                                             :from sig)]
                (callback (if ack? :ack type) message')
                (ack/check-ack! web3 sig payload'' identity)))))))))

