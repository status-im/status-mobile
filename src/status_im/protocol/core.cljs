(ns status-im.protocol.core
  (:require status-im.protocol.message
            [status-im.protocol.web3.utils :as u]
            [status-im.protocol.web3.filtering :as f]
            [status-im.protocol.web3.delivery :as d]
            [status-im.protocol.web3.inbox :as inbox]
            [taoensso.timbre :refer-macros [debug] :as log]
            [status-im.protocol.validation :refer-macros [valid?]]
            [status-im.protocol.web3.utils :as u]
            [status-im.protocol.chat :as chat]
            [status-im.protocol.group :as group]
            [status-im.protocol.listeners :as l]
            [status-im.protocol.discoveries :as discoveries]
            [cljs.spec.alpha :as s]
            [status-im.utils.config :as config]
            [status-im.utils.random :as random]
            [re-frame.core :as re-frame]
            status-im.protocol.web3.shh))

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
(def new-keypair! nil)

(def watch-user! discoveries/watch-user!)
(def stop-watching-user! discoveries/stop-watching-user!)
(def contact-request! discoveries/contact-request!)
(def broadcast-profile! discoveries/broadcast-profile!)
(def send-status! discoveries/send-status!)
(def send-discoveries-request! discoveries/send-discoveries-request!)
(def send-discoveries-response! discoveries/send-discoveries-response!)
(def update-keys! discoveries/update-keys!)

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

(re-frame/reg-fx
  :whisper/stop
  (fn []
    (f/remove-all-filters!)))

(re-frame/reg-fx
  :whisper/start-listening-groups
  (fn []
    (doseq [group groups]
      (let [options (merge listener-options group)]
        (group/start-watching-group! options)))))

(re-frame/reg-fx
  :whisper/start-listening-inbox
  (fn []
    (if config/offline-inbox-enabled?
      (do (log/info "offline inbox: flag enabled")
          (f/add-filter!
           web3
           {:key      identity
            :allowP2P true
            :topics  (f/get-topics identity)}
           (l/message-listener listener-options))
          (inbox/initialize! web3)))))

(re-frame/reg-fx
  :whisper/start-listening-chats
  (fn []
    (f/add-filter!
     web3
     {:key    identity
      :topics (f/get-topics identity)}
     (l/message-listener listener-options))))

(re-frame/reg-fx
  :whisper/start-listening-profiles
  (fn []
    (doseq [{:keys [identity keypair]} contacts]
      (watch-user! {:web3     web3
                    :identity identity
                    :keypair  keypair
                    :callback callback}))))

(handlers/register-handler-fx
  :whisper/send-online-message
  (fn []
    ))
