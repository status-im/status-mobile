(ns status-im.models.notifications
  (:require [status-im.js-dependencies :as dependencies]
            [status-im.utils.platform :as platform]))

(def ^:private pn-message-id-hash-length 10)
(def pn-pubkey-hash-length 10)
(def pn-pubkey-length 132)

(when-not platform/desktop?
  (defn- hash->contact [hash-or-pubkey accounts]
    (let [hash (anonymize-pubkey hash-or-pubkey)]
      (->> accounts
           (filter #(= (anonymize-pubkey (:public-key %)) hash))
           first)))

  (defn- hash->pubkey [hash accounts]
    (:public-key (hash->contact hash accounts))))

  (defn- get-contact-name [{:keys [db] :as cofx} from]
    (if (accounts.db/logged-in? cofx)
      (:name (hash->contact from (-> db :contacts/contacts vals)))
      (anonymize-pubkey from)))

(defn encode-notification-payload
  [{:keys [from to id] :as payload}]
  (if (valid-notification-payload? payload)
    {:msg-v2 (js/JSON.stringify #js {:from (anonymize-pubkey from)
                                     :to   (anonymize-pubkey to)
                                     :id   (apply str (take pn-message-id-hash-length id))})}
    (throw (str "Invalid push notification payload" payload))))

(defn sha3 [s]
  (.sha3 dependencies/Web3.prototype s))

(defn anonymize-pubkey
  [pubkey]
  "Anonymize a public key, if needed, by hashing it and taking the first 4 bytes"
  (if (= (count pubkey) pn-pubkey-hash-length)
    pubkey
    (apply str (take pn-pubkey-hash-length (sha3 pubkey)))))

(defn valid-notification-payload?
  [{:keys [from to]}]
  (and from to
       (or
        ;; is it full pubkey?
        (and (= (.-length from) pn-pubkey-length)
             (= (.-length to) pn-pubkey-length))
        ;; partially deanonymized
        (and (= (.-length from) pn-pubkey-hash-length)
             (= (.-length to) pn-pubkey-length))
        ;; or is it an anonymized pubkey hash (v2 payload)?
        (and (= (.-length from) pn-pubkey-hash-length)
             (= (.-length to) pn-pubkey-hash-length)))))
