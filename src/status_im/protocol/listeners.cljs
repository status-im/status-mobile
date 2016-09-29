(ns status-im.protocol.listeners
  (:require [cljs.reader :as r]
            [status-im.protocol.ack :as ack]
            [status-im.protocol.web3.utils :as u]
            [status-im.protocol.encryption :as e]
            [taoensso.timbre :refer-macros [debug] :as log]
            [status-im.utils.hex :as i]))

(defn- parse-payload [payload]
  (debug :parse-payload)
  (r/read-string (u/to-utf8 payload)))

(defn- decrypt [key content]
  (try
    (r/read-string (e/decrypt key content))
    (catch :default err
      (log/warn :decrypt-error err)
      nil)))

(defn- parse-content [key {:keys [content]} was-encrypted?]
  (debug :parse-content
         "Key exitsts:" (not (nil? key))
         "Content exists:" (not (nil? content)))
  (if (and (not was-encrypted?) key content)
    (decrypt key content)
    content))

(defn message-listener
  [{:keys [web3 identity callback keypair]}]
  (fn [error js-message]
    ;; todo handle error
    (when error
      (debug :listener-error error))
    (when-not error
      (debug :message-received)
      (let [{:keys [from payload to] :as message}
            (js->clj js-message :keywordize-keys true)]
        (when-not (= (i/normalize-hex identity)
                     (i/normalize-hex from))
          (let [{:keys [type ack?] :as payload'}
                (parse-payload payload)

                content (parse-content (:private keypair) payload' (not= "0x0" to))
                payload'' (assoc payload' :content content)

                message' (assoc message :payload payload'')]
            (callback (if ack? :ack type) message')
            (ack/check-ack! web3 from payload'' identity)))))))
