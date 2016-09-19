(ns status-im.protocol.core
  (:require status-im.protocol.message
            [status-im.protocol.web3.utils :as u]
            [status-im.protocol.web3.filtering :as f]
            [status-im.protocol.web3.delivery :as d]
            [taoensso.timbre :refer-macros [debug]]
            [status-im.protocol.validation :refer-macros [valid?]]
            [status-im.protocol.web3.utils :as u]
            [status-im.protocol.chat :as chat]
            [status-im.protocol.group :as group]
            [status-im.protocol.listeners :as l]
            [status-im.protocol.encryption :as e]
            [status-im.protocol.discoveries :as discoveries]
            [cljs.spec :as s]
            [status-im.utils.random :as random]))

;; user
(def send-message! chat/send!)
(def send-seen! chat/send-seen!)
(def reset-pending-messages! d/reset-pending-messages!)

;; group
(def start-watching-group! group/start-watching-group!)
(def stop-watching-group! group/stop-watching-group!)
(def send-group-message! group/send!)
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
(def contact-request! discoveries/contact-request!)
(def watch-hashtags! discoveries/watch-hashtags!)
(def broadcats-profile! discoveries/broadcats-profile!)
(def broadcats-discoveries! discoveries/broadcats-discoveries!)

;; initialization
(s/def ::rpc-url string?)
(s/def ::identity string?)
(s/def :message/chat-id string?)
(s/def ::group (s/keys :req-un [:message/chat-id :message/keypair]))
(s/def ::groups (s/* ::group))
(s/def ::callback fn?)
(s/def ::contact (s/keys :req-un [::identity :message/keypair]))
(s/def ::contacts (s/* ::contact))
(s/def ::profile-keypair :message/keypair)
(s/def ::options
  (s/merge
    (s/keys :req-un [::rpc-url ::identity ::groups ::profile-keypair
                     ::callback :discoveries/hashtags ::contacts])
    ::d/delivery-options))

(def stop-watching-all! f/remove-all-filters!)

(defn init-whisper!
  [{:keys [rpc-url identity groups callback
           hashtags contacts profile-keypair pending-messages]
    :as   options}]
  {:pre [(valid? ::options options)]}
  (debug :init-whisper)
  (stop-watching-all!)
  (d/reset-all-pending-messages!)
  (let [web3 (u/make-web3 rpc-url)
        listener-options {:web3     web3
                          :identity identity}]
    ;; start listening to user's inbox
    (f/add-filter!
      web3
      {:to     identity
       :topics [f/status-topic]}
      (l/message-listener (assoc listener-options :callback callback)))
    ;; start listening to groups
    (doseq [{:keys [chat-id keypair]} groups]
      (f/add-filter!
        web3
        {:topics [chat-id]}
        (l/message-listener (assoc listener-options :callback callback
                                                    :keypair keypair))))
    ;; start listening to discoveries
    (watch-hashtags! {:web3     web3
                      :hashtags hashtags
                      :callback callback})
    ;; start listening to profiles
    (doseq [{:keys [identity keypair]} contacts]
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
      (d/add-prepeared-pending-message! web3 pending-message))
    web3))
