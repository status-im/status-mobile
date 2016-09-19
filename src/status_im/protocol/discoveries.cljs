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

(def discovery-topic "status-discovery")
(def discovery-topic-prefix "status-discovery-")
(def discovery-hashtag-prefix "status-hashtag-")

(defn- add-hashtag-prefix [hashtag]
  (str discovery-hashtag-prefix hashtag))

(defn- make-discovery-topic [identity]
  (str discovery-topic-prefix identity))

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
                    :topics        [(make-discovery-topic (:from message))]})]
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
     :topics [(make-discovery-topic identity)]}
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

(defn- hashtags->topics
  "Create topics from hashtags."
  [hashtags]
  (->> hashtags
       (map (fn [tag]
              [tag [(add-hashtag-prefix tag) discovery-topic]]))
       (into {})))

(s/def :discoveries/hashtags (s/every string? :kind-of set?))

(defn stop-watching-hashtags!
  [web3]
  (doseq [topics @watched-hashtag-topics]
    (f/remove-filter! web3 topics)))

(s/def ::callback fn?)
(s/def :watch-hashtags/options
  (s/keys :req-un [:options/web3 :discoveries/hashtags ::callback]))

(defn watch-hashtags!
  [{:keys [web3 hashtags] :as options}]
  {:pre [(valid? :watch-hashtags/options options)]}
  (debug :watch-hashtags hashtags)
  (stop-watching-hashtags! web3)
  (let [hashtag-topics (vals (hashtags->topics hashtags))]
    (reset! watched-hashtag-topics hashtag-topics)
    (doseq [topics hashtag-topics]
      (f/add-filter! web3 {:topics topics} (l/message-listener options)))))

(s/def ::status (s/nilable string?))
(s/def ::profile (s/keys :req-un [::status]))
(s/def :profile/payload
  (s/merge :message/payload (s/keys :req-un [::profile])))
(s/def :profile/message
  (s/merge :protocol/message (s/keys :req-un [:message/keypair
                                              :profile/payload])))
(s/def :broadcast-profile/options
  (s/keys :req-un [:profile/message :options/web3]))

(defn broadcats-profile!
  [{:keys [web3 message] :as options}]
  {:pre [(valid? :broadcast-profile/options options)]}
  (debug :broadcasting-status)
  (d/add-pending-message!
    web3
    (-> message
        (assoc :type :profile
               :topics [(make-discovery-topic (:from message))])
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

(defn broadcats-discoveries!
  [{:keys [web3 hashtags message] :as options}]
  {:pre [(valid? :broadcast-hasthags/options options)]}
  (debug :broadcasting-status)
  (let [discovery-id (random/id)]
    (doseq [[tag hashtag-topics] (hashtags->topics hashtags)]
      (d/add-pending-message!
        web3
        (-> message
            (assoc :type :discovery
                   :topics hashtag-topics)
            (assoc-in [:payload :tag] tag)
            (assoc-in [:payload :hashtags] (vec hashtags))
            (assoc-in [:payload :discovery-id] discovery-id)
            (update :message-id str tag))))))
