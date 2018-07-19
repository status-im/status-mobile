(ns status-im.chat.models.group-chat
  (:require [clojure.set :as set]
            [clojure.string :as string]
            [status-im.i18n :as i18n]
            [status-im.transport.utils :as transport.utils]
            [status-im.transport.message.core :as transport]
            [status-im.transport.message.v1.core :as transport.message]
            [status-im.ui.screens.group.core :as group]
            [status-im.group-chats.core :as group-chat]
            [status-im.chat.models :as models.chat]
            [status-im.transport.message.core :as message]
            [status-im.chat.models.message :as models.message]
            [status-im.utils.fx :as fx]))

(defn- participants-diff [existing-participants-set new-participants-set]
  {:removed (set/difference existing-participants-set new-participants-set)
   :added   (set/difference new-participants-set existing-participants-set)})

(defn- prepare-system-message [admin-name added-participants removed-participants contacts]
  (let [added-participants-names   (map #(get-in contacts [% :name] %) added-participants)
        removed-participants-names (map #(get-in contacts [% :name] %) removed-participants)]
    (cond
      (and (seq added-participants) (seq removed-participants))
      (str admin-name " "
           (i18n/label :t/invited) " " (apply str (interpose ", " added-participants-names))
           " and "
           (i18n/label :t/removed) " " (apply str (interpose ", " removed-participants-names)))

      (seq added-participants)
      (str admin-name " " (i18n/label :t/invited) " " (apply str (interpose ", " added-participants-names)))

      (seq removed-participants)
      (str admin-name " " (i18n/label :t/removed) " " (apply str (interpose ", " removed-participants-names))))))

(fx/defn handle-group-leave
  [{:keys [db random-id-generator now] :as cofx} chat-id signature]
  (let [me                       (:current-public-key db)
        system-message-id        (random-id-generator)
        participant-leaving-name (or (get-in db [:contacts/contacts signature :name])
                                     signature)]
    (when (and
           (not= signature me)
           (get-in db [:chats chat-id])) ;; chat is present

      (fx/merge cofx
                #_(models.message/receive
                   (models.message/system-message chat-id random-id now
                                                  (str participant-leaving-name " " (i18n/label :t/left))))
                (group/participants-removed chat-id #{signature})))))

(defn- group-name-from-contacts [selected-contacts all-contacts username]
  (->> selected-contacts
       (map (comp :name (partial get all-contacts)))
       (cons username)
       (string/join ", ")))
