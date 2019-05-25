(ns status-im.contact-code.core
  "This namespace is used to listen for and publish contact-codes. We want to listen
  to contact codes once we engage in the conversation with someone, or once someone is
  in our contacts."
  (:require
   [taoensso.timbre :as log]
   [status-im.contact.db :as contact.db]
   [status-im.utils.fx :as fx]
   [status-im.transport.shh :as shh]
   [status-im.transport.message.public-chat :as transport.public-chat]
   [status-im.data-store.accounts :as data-store.accounts]
   [status-im.transport.chat.core :as transport.chat]
   [status-im.accounts.db :as accounts.db]
   [status-im.mailserver.core :as mailserver]))

(defn topic [pk]
  (str pk "-contact-code"))

(fx/defn listen [cofx chat-id]
  (transport.public-chat/join-public-chat
   cofx
   (topic chat-id)))

(fx/defn listen-to-chat
  "For a one-to-one chat, listen to the pk of the user, for a group chat
  listen for any member"
  [cofx chat-id]
  (let [{:keys [members
                public?
                is-active
                group-chat]} (get-in cofx [:db :chats chat-id])]
    (when is-active
      (cond
        (and group-chat
             (not public?))
        (apply fx/merge cofx
               (map listen members))
        (not public?)
        (listen cofx chat-id)))))

(fx/defn stop-listening
  "We can stop listening to contact codes when we don't have any active chat
  with the user (one-to-one or group-chat), and it is not in our contacts"
  [{:keys [db] :as cofx} their-public-key]
  (let [my-public-key (accounts.db/current-public-key cofx)
        active-group-chats (filter (fn [{:keys [is-active members members-joined]}]
                                     (and is-active
                                          (contains? members-joined my-public-key)
                                          (contains? members their-public-key)))
                                   (vals (:chats db)))
        their-topic (topic their-public-key)]
    (when (and (not (contact.db/active? db their-public-key))
               (not= my-public-key their-public-key)
               (not (get-in db [:chats their-public-key :is-active]))
               (empty? active-group-chats))
      (fx/merge
       cofx
       (mailserver/remove-gaps their-topic)
       (mailserver/remove-range their-topic)
       (transport.chat/unsubscribe-from-chat their-topic)))))

;; Publish contact code every 12hrs
(def publish-contact-code-interval (* 12 60 60 1000))

(fx/defn init [cofx]
  (log/debug "initializing contact-code")
  (let [current-public-key (accounts.db/current-public-key cofx)]
    (listen cofx current-public-key)))

(defn publish! [{:keys [web3 now] :as cofx}]
  (let [current-public-key (accounts.db/current-public-key cofx)
        chat-id (topic current-public-key)
        peers-count (:peers-count @re-frame.db/app-db)
        last-published (get-in
                        @re-frame.db/app-db
                        [:account/account :last-published-contact-code])]
    (when (and (pos? peers-count)
               (< publish-contact-code-interval
                  (- now last-published)))

      (let [message {:chat chat-id
                     :sig  current-public-key
                     :payload ""}]
        (shh/send-public-message!
         web3
         message
         [:contact-code.callback/contact-code-published]
         :contact-code.callback/contact-code-publishing-failed)))))

(fx/defn published [{:keys [now db] :as cofx}]
  (let [new-account (assoc (:account/account db)
                           :last-published-contact-code
                           now)]
    {:db (assoc db :account/account new-account)
     :data-store/base-tx [(data-store.accounts/save-account-tx new-account)]}))

(fx/defn publishing-failed [cofx]
  (log/warn "failed to publish contact-code"))
