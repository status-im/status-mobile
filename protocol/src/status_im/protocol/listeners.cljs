(ns status-im.protocol.listeners
  (:require [cljs.reader :as r]
            [status-im.protocol.ack :as ack]
            [status-im.protocol.web3.utils :as u]
            [status-im.protocol.encryption :as e]
            [taoensso.timbre :refer-macros [debug]]
            [status-im.utils.hex :as i]))

(defn- parse-payload [web3 payload]
  (debug :parse-payload)
  (try
    ;; todo figure why we have to call to-utf8 twice
    (let [read (comp r/read-string u/to-utf8 u/to-utf8)]
      {:payload (read payload)})
    (catch :default err
      (debug :parse-payload-error err)
      {:error err})))

(defn- decrypt [key content]
  (try
    {:content (r/read-string (e/decrypt key content))}
    (catch :default err
      (debug :decrypt-error err)
      {:error err})))

(defn- parse-content [key {:keys [content]} was-encrypted?]
  (debug :parse-content
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
      (debug :listener-error error))
    (when-not error
      (debug :message-received (js->clj js-message))
      (let [{:keys [sig payload recipientPublicKey] :as message}
            (js->clj js-message :keywordize-keys true)

            {{:keys [type ack?] :as payload'} :payload
             payload-error                    :error}
            (parse-payload web3 payload)]
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
              (debug :failed-to-handle-message error)
              (let [payload'' (assoc payload' :content content)
                    message'  (assoc message :payload payload''
                                             :to recipientPublicKey
                                             :from sig)]
                (callback (if ack? :ack type) message')
                (ack/check-ack! web3 sig payload'' identity)))))))))

