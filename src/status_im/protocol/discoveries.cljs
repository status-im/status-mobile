(ns status-im.protocol.discoveries
  (:require
   [taoensso.timbre :refer-macros [debug]]
   [status-im.protocol.web3.utils :as u]
   [status-im.protocol.web3.delivery :as d]
   [status-im.protocol.web3.filtering :as f]
   [status-im.protocol.listeners :as l]
   [cljs.spec.alpha :as s]
   [status-im.protocol.validation :refer-macros [valid?]]
   [status-im.utils.random :as random]
   [status-im.protocol.web3.keys :as shh-keys]))

(def discover-topic-prefix "status-discover-")
(def discover-topic "0xbeefdead")

(defn- make-discover-topic [identity]
  (str discover-topic-prefix identity))

(s/def :send-online/message
  (s/merge :protocol/message
           (s/keys :req-un [:message/keypair])))
(s/def :send-online/options
  (s/keys :req-un [:options/web3 :send-online/message]))

(def discovery-key-password "status-discovery")

(defn send-online!
  [{:keys [web3 message] :as options}]
  {:pre [(valid? :send-online/options options)]}
  (debug :send-online)
  (let [message' (merge
                  message
                  {:requires-ack? false
                   :type          :online
                   :key-password  discovery-key-password
                   :payload       {:content {:timestamp (u/timestamp)}}
                   :topics        [f/status-topic]})]
    (d/add-pending-message! web3 message')))

(s/def ::identity :message/from)
(s/def :watch-user/options
  (s/keys :req-un [:options/web3 :message/keypair ::identity ::callback]))

(defn watch-user!
  [{:keys [web3 identity] :as options}]
  {:pre [(valid? :watch-user/options options)]}
  (shh-keys/get-sym-key
   web3
   discovery-key-password
   (fn [key-id]
     (f/add-filter!
      web3
      {:sig    identity
       :topics [f/status-topic]
       :key    key-id
       :type   :sym}
      (l/message-listener (dissoc options :identity))))))

(defn stop-watching-user!
  [{:keys [web3 identity]}]
  (shh-keys/get-sym-key
   web3
   discovery-key-password
   (fn [key-id]
     (f/remove-filter!
      web3
      {:sig    identity
       :topics [f/status-topic]
       :key    key-id
       :type   :sym}))))

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
   (assoc message
          :type :contact-request
          :requires-ack? true
          :topics [f/status-topic])))

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
              :topics [f/status-topic]
              :key-password discovery-key-password)
       (assoc-in [:payload :timestamp] (u/timestamp))
       (assoc-in [:payload :content :profile]
                 (get-in message [:payload :profile]))
       (update :payload dissoc :profile))))

(s/def ::public string?)
(s/def ::private string?)
(s/def ::keypair (s/keys :req-un [::public ::private]))
(s/def :update-keys/payload
  (s/keys :req-un [::keypair]))
(s/def :update-keys/message
  (s/merge :protocol/message (s/keys :req-un [:update-keys/payload])))
(s/def :update-keys/options
  (s/keys :req-un [:update-keys/message :options/web3]))

(defn update-keys!
  [{:keys [web3 message] :as options}]
  {:pre [(valid? :update-keys/options options)]}
  (let [message (-> message
                    (assoc :type :update-keys
                           :requires-ack? false
                           :key-password discovery-key-password
                           :topics [f/status-topic])
                    (assoc-in [:payload :timestamp] (u/timestamp)))]
    (d/add-pending-message! web3 message)))

(s/def :status/payload
  (s/merge :message/payload (s/keys :req-un [::status])))
(s/def :status/message
  (s/merge :protocol/message (s/keys :req-un [:status/payload])))
(s/def :broadcast-hasthags/options
  (s/keys :req-un [:discoveries/hashtags :status/message :options/web3]))

(defn send-status!
  [{:keys [web3 message]}]
  (debug :broadcasting-status)
  (let [message (assoc message :type :discover
                       :key-password discovery-key-password
                       :topics [f/status-topic])]
    (d/add-pending-message! web3 message)))

(defn send-discoveries-request!
  [{:keys [web3 message]}]
  (debug :sending-discoveries-request)
  (d/add-pending-message!
   web3
   (assoc message :type :discoveries-request
          :key-password discovery-key-password
          :topics [f/status-topic])))

(defn send-discoveries-response!
  [{:keys [web3 discoveries message]}]
  (debug :sending-discoveries-response)
  (doseq [portion (->> discoveries
                       (take 100)
                       (partition 10 10 nil))]
    (d/add-pending-message!
     web3
     (assoc message :type :discoveries-response
            :key-password discovery-key-password
            :topics [f/status-topic]
            :message-id (random/id)
            :payload {:data (into [] portion)}))))
