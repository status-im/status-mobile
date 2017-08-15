(ns status-im.bots.events
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]))

;;;; Helper fns

(defn- chats-with-bot [chats bot]
  (reduce (fn [acc [_ {:keys [chat-id contacts]}]]
            (let [contacts (map :identity contacts)]
              (if (some #{bot} contacts)
                (conj acc chat-id)
                acc)))
          []
          chats))

(defn- subscription-values [subscriptions current-bot-db]
  (reduce (fn [sub-values [sub-name sub-path]]
            (assoc sub-values sub-name (get-in current-bot-db sub-path)))
          {}
          subscriptions))

;; TODO(janherich): optimze this, for sure we don't need to re-calculate all bot subscriptions every time something in bot db changes
(defn- check-subscriptions-fx
  [db {:keys [bot path value]}]
  (let [{:keys [bot-db chats]} db
        subscriptions          (get-in db [:bot-subscriptions path])]
    {:call-jail-function-n
     (for [{:keys [bot subscriptions name]} subscriptions
           :let [subs-values (subscription-values subscriptions (get bot-db bot))]]
       {:chat-id                 bot
        :function                :subscription
        :parameters              {:name          name
                                  :subscriptions subs-values}
        :callback-events-creator (fn [jail-response]
                                   (into [[::calculated-subscription
                                           {:bot    bot
                                            :path   [name]
                                            :result jail-response}]]
                                         (map (fn [chat-id]
                                                [::calculated-subscription
                                                 {:bot    chat-id
                                                  :path   [name]
                                                  :result jail-response}])
                                              (chats-with-bot chats bot))))})}))

(defn set-in-bot-db
  [{:keys [current-chat-id] :as app-db} {:keys [bot path value] :as params}]
  (let [bot    (or bot current-chat-id)
        new-db (assoc-in app-db (concat [:bot-db bot] path) value)]
    (merge {:db new-db}
           (check-subscriptions-fx new-db params))))

(defn update-bot-db
  [{:keys [current-chat-id] :as app-db} {:keys [bot db]}]
  (let [bot (or bot current-chat-id)]
    (update-in app-db [:bot-db bot] merge db)))

(defn clear-bot-db
  [{:keys [current-chat-id] :as app-db}]
  (assoc-in app-db [:bot-db current-chat-id] nil))

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
  :register-bot-subscription
  [re-frame/trim-v]
  (fn [db [{:keys [bot subscriptions] :as opts}]]
    (reduce
     (fn [db [sub-name sub-path]]
       (let [keywordized-sub-path (mapv keyword
                                        (if (coll? sub-path)
                                          sub-path
                                          [sub-path]))]
         (update-in db [:bot-subscriptions keywordized-sub-path] conj
                    (assoc-in opts [:subscriptions sub-name] keywordized-sub-path))))
     db
     subscriptions)))

(handlers/register-handler-db
  ::calculated-subscription
  [re-frame/trim-v]
  (fn [db [{:keys                  [bot path]
            {:keys [error result]} :result}]]
    (if error
      db
      (assoc-in db (concat [:bot-db bot] path) (:returned result)))))
