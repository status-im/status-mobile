(ns status-im.protocol.discoveries
  (:require
    [taoensso.timbre :refer-macros [debug]]
    [status-im.protocol.web3.utils :as u]
    [status-im.protocol.web3.delivery :as d]
    [status-im.protocol.web3.filtering :as f]
    [status-im.protocol.listeners :as l]
    [cljs.spec :as s]
    [status-im.protocol.validation :refer-macros [valid?]]
    [status-im.utils.random :as random]))

(def discover-topic-prefix "status-discover-")
(def discover-hashtag-prefix "status-hashtag-")

(defn- make-discover-topic [identity]
  (str discover-topic-prefix identity))

(s/def :send-online/message
  (s/merge :protocol/message
           (s/keys :req-un [:message/keypair])))
(s/def :send-online/options
  (s/keys :req-un [:options/web3 :send-online/message]))

(defn send-online!
  [{:keys [web3 message] :as options}]
  {:pre [(valid? :send-online/options options)]}
  (debug :send-online)
  (let [message' (merge
                   message
                   {:requires-ack? false
                    :type          :online
                    :payload       {:timestamp (u/timestamp)}
                    :topics        [(make-discover-topic (:from message))]})]
    (d/add-pending-message! web3 message')))

(s/def ::identity :message/from)
(s/def :watch-user/options
  (s/keys :req-un [:options/web3 :message/keypair ::identity ::callback]))

(defn watch-user!
  [{:keys [web3 identity] :as options}]
  {:pre [(valid? :watch-user/options options)]}
  (f/add-filter!
    web3
    {:from   identity
     :topics [(make-discover-topic identity)]}
    (l/message-listener (dissoc options :identity))))

(s/def :contact-request/contact map?)

(s/def :contact-request/payload
  (s/merge :message/payload
           (s/keys :req-un [:contact-request/contact :message/keypair])))

(s/def :contact-request/message
  (s/merge :protocol/message
           (s/keys :req-un [:message/to :contact-request/payload])))

(defn contact-request!
  [{:keys [web3 message]}]
  {:pre [(valid? :contact-request/message message)]}
  (debug :send-command-request!)
  (d/add-pending-message!
    web3
    (assoc message :type :contact-request
                   :requires-ack? true
                   :topics [f/status-topic])))

(defonce watched-hashtag-topics (atom nil))

(s/def :discoveries/hashtags (s/every string? :kind-of set?))

(s/def ::callback fn?)
(s/def :watch-hashtags/options
  (s/keys :req-un [:options/web3 :discoveries/hashtags ::callback]))

(s/def ::status (s/nilable string?))
(s/def ::profile (s/keys :req-un [::status]))
(s/def :profile/payload
  (s/merge :message/payload (s/keys :req-un [::profile])))
(s/def :profile/message
  (s/merge :protocol/message (s/keys :req-un [:message/keypair
                                              :profile/payload])))
(s/def :broadcast-profile/options
  (s/keys :req-un [:profile/message :options/web3]))

(defn broadcast-profile!
  [{:keys [web3 message] :as options}]
  {:pre [(valid? :broadcast-profile/options options)]}
  (debug :broadcasting-status)
  (d/add-pending-message!
    web3
    (-> message
        (assoc :type :profile
               :topics [(make-discover-topic (:from message))])
        (assoc-in [:payload :timestamp] (u/timestamp))
        (assoc-in [:payload :content :profile]
                  (get-in message [:payload :profile]))
        (update :payload dissoc :profile))))

(s/def :status/payload
  (s/merge :message/payload (s/keys :req-un [::status])))
(s/def :status/message
  (s/merge :protocol/message (s/keys :req-un [:status/payload])))
(s/def :broadcast-hasthags/options
  (s/keys :req-un [:discoveries/hashtags :status/message :options/web3]))

(defn send-status!
  [{:keys [web3 message]}]
  (debug :broadcasting-status)
  (let [message (-> message
                    (assoc :type :discover
                           :topics [(make-discover-topic (:from message))]))]
    (d/add-pending-message! web3 message)))

(defn send-discoveries-request!
  [{:keys [web3 message]}]
  (debug :sending-discoveries-request)
  (d/add-pending-message!
    web3
    (-> message
        (assoc :type :discoveries-request
               :topics [(make-discover-topic (:from message))]))))

(defn send-discoveries-response!
  [{:keys [web3 discoveries message]}]
  (debug :sending-discoveries-response)
  (doseq [portion (->> (take 100 discoveries)
                       (partition 10 10 nil))]
    (d/add-pending-message!
      web3
      (-> message
          (assoc :type :discoveries-response
                 :topics [(make-discover-topic (:from message))]
                 :message-id (random/id)
                 :payload {:data (into [] portion)})))))
