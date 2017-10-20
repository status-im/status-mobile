(ns status-im.bots.events
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.chat.models.input :as input-model]
            [taoensso.timbre :as log]))

;;;; Helper fns

(defn- subscription-values [sub-params current-bot-db]
  (reduce (fn [sub-values [sub-key sub-path]]
            (assoc sub-values sub-key (get-in current-bot-db sub-path)))
          {}
          sub-params))

;; TODO(janherich): do this properly instead of the ugly hack with hardcoded bot-db key
;; und uneffective lookup of the owner of selected-chat-command
(defn- check-subscriptions-fx
  [{:keys [bot-db bot-subscriptions chats current-chat-id] :as app-db} path]
  (let [owner-id (some-> app-db
                         (input-model/selected-chat-command current-chat-id (get-in app-db [:chats current-chat-id :input-text]))
                         :command
                         :owner-id)]
    (when-let [subscriptions (and owner-id (get-in bot-subscriptions (concat [owner-id] [path])))]
      {:call-jail-function-n
       (for [[sub-name sub-params] subscriptions]
         {:chat-id  owner-id
          :function :subscription
          :parameters {:name          sub-name
                       :subscriptions (subscription-values sub-params
                                                           (get bot-db current-chat-id))}
          :callback-events-creator (fn [jail-response]
                                     [[::calculated-subscription
                                       {:bot    current-chat-id
                                        :path   [sub-name]
                                        :result jail-response}]])})})))

(defn set-in-bot-db
  [{:keys [current-chat-id] :as app-db} {:keys [bot path value] :as params}]
  (let [bot    (or bot current-chat-id)
        new-db (assoc-in app-db (concat [:bot-db bot] path) value)]
    (merge {:db new-db}
           (check-subscriptions-fx new-db path))))

(defn update-bot-db
  [{:keys [current-chat-id] :as app-db} {:keys [bot db]}]
  (let [bot (or bot current-chat-id)]
    (update-in app-db [:bot-db bot] merge db)))

(defn clear-bot-db
  [{:keys [current-chat-id] :as app-db}]
  (assoc-in app-db [:bot-db current-chat-id] nil))

(def ^:private keywordize-vector (partial mapv keyword))

;; TODO(janherich): do something with this horrible triple nested reduce so it's more readable
(defn add-active-bot-subscriptions
  [app-db bot-identities]
  (let [relevant-bots (select-keys (:contacts/contacts app-db) bot-identities)
        active-subscriptions (reduce (fn [acc [bot-id {:keys [subscriptions]}]]
                                       (reduce (fn [acc [sub-key {:keys [subscriptions]}]]
                                                 (reduce (fn [acc [sub-param-key sub-param-path]]
                                                           (update-in acc [bot-id (keywordize-vector sub-param-path)]
                                                                      assoc sub-key (into {}
                                                                                          (map (fn [[k v]]
                                                                                                 [k (keywordize-vector v)]))
                                                                                          subscriptions)))
                                                         acc
                                                         subscriptions))
                                               acc
                                               subscriptions))
                                     {}
                                     relevant-bots)]
    (assoc app-db :bot-subscriptions active-subscriptions)))

;;;; Handlers

(handlers/register-handler-fx
  :set-in-bot-db
  [re-frame/trim-v]
  (fn [{:keys [db]} [params]]
    (set-in-bot-db db params)))

(handlers/register-handler-db
  :update-bot-db
  [re-frame/trim-v]
  (fn [db [params]]
    (update-bot-db db params)))

(handlers/register-handler-db
  ::calculated-subscription
  [re-frame/trim-v]
  (fn [db [{:keys                  [bot path]
            {:keys [error result]} :result}]]
    (if error
      db
      (assoc-in db (concat [:bot-db bot] path) (:returned result)))))
