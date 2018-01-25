(ns status-im.protocol.core
  (:require status-im.protocol.message
            [status-im.protocol.web3.utils :as u]
            [status-im.protocol.web3.filtering :as f]
            [status-im.protocol.web3.delivery :as d]
            [status-im.protocol.web3.inbox :as inbox]
            [taoensso.timbre :refer-macros [debug] :as log]
            [status-im.protocol.validation :refer-macros [valid?]]
            [status-im.protocol.web3.utils :as u]
            [status-im.protocol.web3.keys :as shh-keys]
            [status-im.protocol.chat :as chat]
            [status-im.protocol.group :as group]
            [status-im.protocol.listeners :as l]
            [status-im.protocol.encryption :as e]
            [status-im.protocol.discoveries :as discoveries]
            [cljs.spec.alpha :as s]
            [status-im.utils.config :as config]
            [status-im.utils.random :as random]))

;; user
(def send-message! chat/send!)
(def send-seen! chat/send-seen!)
(def reset-pending-messages! d/reset-pending-messages!)

;; group
(def start-watching-group! group/start-watching-group!)
(def stop-watching-group! group/stop-watching-group!)
(def send-group-message! group/send!)
(def send-public-group-message! group/send-to-public-group!)
(def invite-to-group! group/invite!)
(def update-group! group/update-group!)
(def remove-from-group! group/remove-identity!)
(def add-to-group! group/add-identity!)
(def leave-group-chat! group/leave!)

;; encryption
;; todo move somewhere, encryption functions shouldn't be there
(def new-keypair! e/new-keypair!)

;; discoveries
(def watch-user! discoveries/watch-user!)
(def stop-watching-user! discoveries/stop-watching-user!)
(def contact-request! discoveries/contact-request!)
(def broadcast-profile! discoveries/broadcast-profile!)
(def send-status! discoveries/send-status!)
(def send-discoveries-request! discoveries/send-discoveries-request!)
(def send-discoveries-response! discoveries/send-discoveries-response!)
(def update-keys! discoveries/update-keys!)

(def message-pending? d/message-pending?)

;; initialization
(s/def ::identity string?)
(s/def :message/chat-id string?)
(s/def ::public? (s/and boolean? true?))
(s/def ::group-id :message/chat-id)
(s/def ::group (s/or
                 :group (s/keys :req-un [::group-id :message/keypair])
                 :public-group (s/keys :req-un [::group-id ::public?])))
(s/def ::groups (s/* ::group))
(s/def ::callback fn?)
(s/def ::contact (s/keys :req-un [::identity :message/keypair]))
(s/def ::contacts (s/* ::contact))
(s/def ::profile-keypair :message/keypair)
(s/def ::options
  (s/merge
    (s/keys :req-un [::identity ::groups ::profile-keypair
                     ::callback :discoveries/hashtags ::contacts])
    ::d/delivery-options))

(def stop-watching-all! f/remove-all-filters!)
(def reset-all-pending-messages! d/reset-all-pending-messages!)
(def reset-keys! shh-keys/reset-keys!)

(defn stop-whisper! []
  (stop-watching-all!)
  (reset-all-pending-messages!)
  (reset-keys!))

(defn init-whisper!
  [{:keys [identity groups callback web3
           contacts profile-keypair pending-messages]
    :as   options}]
  {:pre [(valid? ::options options)]}
  (debug :init-whisper)
  (stop-whisper!)
  (let [listener-options {:web3     web3
                          :identity identity
                          :callback callback}]
    ;; start listening to groups
    #_(doseq [group groups]
        (let [options (merge listener-options group)]
          (group/start-watching-group! options)))
    ;; start listening to user's inbox
    (if config/offline-inbox-enabled?
      (do (log/info "offline inbox: flag enabled")
          (f/add-filter!
           web3
           {:key      identity
            :allowP2P true
            :topics  (f/get-topics identity)}
           (l/message-listener listener-options))
          (inbox/initialize! web3))
      (f/add-filter!
       web3
       {:key    identity
        :topics (f/get-topics identity)}
       (l/message-listener listener-options)))

    ;; start listening to profiles
    #_(doseq [{:keys [identity keypair]} contacts]
        (watch-user! {:web3     web3
                      :identity identity
                      :keypair  keypair
                      :callback callback}))
    (d/set-pending-mesage-callback! callback)
    (let [online-message #(discoveries/send-online!
                           {:web3    web3
                            :message {:from       identity
                                      :message-id (random/id)
                                      :keypair    profile-keypair}})]
      (d/run-delivery-loop!
       web3
       (assoc options :online-message online-message)))
    (doseq [pending-message pending-messages]
      (d/add-prepared-pending-message! web3 pending-message))))
