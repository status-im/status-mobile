(ns status-im.group-chats.db
  (:require [status-im.chat.models :as models.chat]
            [status-im.multiaccounts.core :as multiaccounts]))

(defn unwrap-events
  "Flatten all events, denormalizing from field"
  [all-updates]
  (mapcat
   (fn [{:keys [events from]}]
     (map #(assoc % :from from) events))
   all-updates))

(defn joined?
  [public-key {:keys [members-joined] :as chat}]
  (contains? members-joined public-key))

(defn invited?
  [my-public-key {:keys [contacts]}]
  (contains? contacts my-public-key))

(defn get-inviter-pk
  [my-public-key {:keys [membership-updates]}]
  (->> membership-updates
       unwrap-events
       (keep (fn [{:keys [from type members]}]
               (when (and (= type "members-added")
                          ((set members) my-public-key))
                 from)))
       last))

(defn get-pending-invite-inviter-name
  "when the chat is a private group chat in which the user has been
  invited and didn't accept the invitation yet, return inviter-name"
  [contacts chat my-public-key]
  (when (and (models.chat/group-chat? chat)
             (invited? my-public-key chat)
             (not (joined? my-public-key chat)))
    (let [inviter-pk (get-inviter-pk my-public-key chat)]
      (multiaccounts/displayed-name (or (get contacts inviter-pk) {:public-key inviter-pk})))))

(defn get-inviter-name
  "when the chat is a private group chat in which the user has been
  invited and didn't accept the invitation yet, return inviter-name"
  [contacts chat my-public-key]
  (when (models.chat/group-chat? chat)
    (let [inviter-pk (get-inviter-pk my-public-key chat)]
      (multiaccounts/displayed-name (or (get contacts inviter-pk) {:public-key inviter-pk})))))
