(ns status-im.protocol.group
  (:require
    [status-im.protocol.message :as m]
    [status-im.protocol.web3.delivery :as d]
    [status-im.protocol.web3.utils :as u]
    [cljs.spec :as s]
    [taoensso.timbre :refer-macros [debug]]
    [status-im.protocol.validation :refer-macros [valid?]]
    [status-im.protocol.web3.filtering :as f]
    [status-im.protocol.listeners :as l]))

(defn prepare-mesage
  [{:keys [message group-id keypair new-keypair type]}]
  (let [message' (-> message
                     (update :payload assoc
                             :group-id group-id
                             :type type
                             :timestamp (u/timestamp))
                     (assoc :topics [group-id]
                            :requires-ack? true
                            :keypair keypair
                            :type type))]
    (if new-keypair
      (assoc message' :new-keypair keypair)
      message')))

(defn- send-group-message!
  [{:keys [web3] :as opts} type]
  (let [message (-> opts
                    (assoc :type type)
                    (prepare-mesage))]
    (debug :send-group-message message)
    (d/add-pending-message! web3 message)))

(s/def ::group-message
  (s/merge :protocol/message (s/keys :req-un [:chat-message/payload])))

(defn send!
  [{:keys [keypair message] :as options}]
  {:pre [(valid? :message/keypair keypair)
         (valid? ::group-message message)]}
  (send-group-message! options :group-message))

(defn leave!
  [options]
  (send-group-message! options :leave-group))

(defn add-identity!
  [{:keys [identity] :as options}]
  {:pre [(valid? :message/to identity)]}
  (let [options' (assoc-in options
                           [:message :payload :identity]
                           identity)]
    (send-group-message! options' :add-group-identity)))

(defn remove-identity!
  [{:keys [identity] :as options}]
  {:pre [(valid? :message/to identity)]}
  (let [options' (assoc-in options
                           [:message :payload :identity]
                           identity)]
    (send-group-message! options' :remove-group-identity)))

(s/def :group/admin :message/from)
(s/def ::identities (s/* string?))

(s/def :group/name string?)
(s/def :group/id string?)
(s/def :group/contacts (s/* string?))
(s/def ::group
  (s/keys :req-un [:group/name :group/id :group/contacts :message/keypair]))
(s/def :invite/options
  (s/keys :req-un [:options/web3 :protocol/message ::group ::identities]))

(defn- notify-about-group!
  [type {:keys [web3 message identities group]
         :as   options}]
  {:pre [(valid? :invite/options options)]}
  (let [{:keys [id admin name keypair contacts]} group
        message' (-> message
                     (assoc :topics [f/status-topic]
                            :requires-ack? true
                            :type type)
                     (update :payload assoc
                             :timestamp (u/timestamp)
                             :group-id id
                             :group-admin admin
                             :group-name name
                             :keypair keypair
                             :contacts contacts
                             :type type))]
    (doseq [identity identities]
      (d/add-pending-message! web3 (assoc message' :to identity)))))

(defn invite!
  [options]
  (notify-about-group! :group-invitation options))

;; todo notify users about keypair change when someone leaves group (from admin)
(defn update-group!
  [options]
  (notify-about-group! :update-group options))

(defn stop-watching-group!
  [{:keys [web3 group-id]}]
  {:pre [(valid? :message/chat-id group-id)]}
  (f/remove-filter! web3 {:topics [group-id]}))

(defn start-watching-group!
  [{:keys [web3 group-id keypair callback identity]}]
  (f/add-filter!
    web3
    {:topics [group-id]}
    (l/message-listener {:web3     web3
                         :identity identity
                         :callback callback
                         :keypair  keypair})))
