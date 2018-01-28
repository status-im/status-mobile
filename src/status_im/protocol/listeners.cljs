(ns status-im.protocol.listeners
  (:require [cljs.reader :as r]
            [re-frame.core :as re-frame]
            [status-im.protocol.ack :as ack]
            [status-im.protocol.web3.utils :as u]
            [status-im.protocol.encryption :as e]
            [taoensso.timbre :as log]
            [status-im.utils.hex :as i]))

(defn empty-public-key? [public-key]
  (or (= "0x0" public-key)
      (= "" public-key)
      (nil? public-key)))

(defn create-error [step description]
  (when (not= description :silent)
    (log/debug step description))
  {:error description})

(defn init-scope [js-error js-message options]
  (if js-error
    (create-error :init-scope-error (-> js-error js->clj str))
    {:message (js->clj js-message :keywordize-keys true)
     :options options}))

(defn parse-payload [{:keys [message error options] :as scope}]
  (log/debug :parse-payload)
  (if error
    scope
    (try
      ;; todo figure why we sometimes have to call to-utf8 twice and sometimes only once
      (let [payload    (:payload message)
            payload'   (u/to-utf8 payload)
            payload''  (r/read-string payload')
            payload''' (if (map? payload'')
                         payload''
                         (r/read-string (u/to-utf8 payload')))]
        (if (map? payload''')
          {:message (assoc message :payload payload''')
           :options options}
          (create-error :parse-payload-error (str "Invalid payload type " (type payload''')))))
      (catch :default err
        (create-error :parse-payload-error  err)))))

(defn filter-messages-from-same-user [{:keys [message error options] :as scope}]
  (if error
    scope
    (if (or (not= (i/normalize-hex (:identity options))
                  (i/normalize-hex (:sig message)))
            ;; allow user to receive his own discoveries
            (= type :discover))
      scope
      (create-error :filter-messages-error :silent))))

(defn parse-content [{:keys [message error options] :as scope}]
  (if error
    scope
    (try
      (let [to             (:recipientPublicKey message)
            from           (:sig message)
            key            (get-in options [:keypair :private])
            raw-content    (get-in message [:payload :content])
            encrypted?     (and (empty-public-key? to) key raw-content)
            content        (if encrypted?
                             (r/read-string (e/decrypt key raw-content))
                             raw-content)]
        (log/debug :parse-content
                   "Key exists:" (not (nil? key))
                   "Content exists:" (not (nil? raw-content)))
        {:message (-> message
                      (assoc-in [:payload :content] content)
                      (assoc :to to
                             :from from))
         :options options})
      (catch :default err
        (create-error :parse-content-error err)))))

(defn handle-message [{:keys [message error options] :as scope}]
  (if error
    scope
    (let [{:keys [web3 identity callback]} options
          {:keys [payload sig]}            message
          ack?                            (get-in message [:payload :ack?])]
      (log/debug :handle-message message)
      (callback (if ack? :ack (:type payload)) message)
      (ack/check-ack! web3 sig payload identity))))

(defn- handle-whisper-message [{:keys [error msg options]}]
  (-> (init-scope error msg options)
      parse-payload
      filter-messages-from-same-user
      parse-content
      handle-message))

(defn message-listener
  "Valid options are: web3, identity, callback, keypair"
  [options]
  (fn [js-error js-message]
    (re-frame/dispatch [:handle-whisper-message js-error js-message options])))

