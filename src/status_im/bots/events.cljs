(ns status-im.bots.events
  (:require [re-frame.core :refer [trim-v]]
            [status-im.utils.handlers :refer [register-handler-db register-handler-fx]]))

;;;; Helper fns

(defn- chats-with-bot [chats bot]
  (reduce (fn [acc [_ {:keys [chat-id contacts]}]]
            (let [contacts (map :identity contacts)]
              (if (some #{bot} contacts)
                (conj acc chat-id)
                acc)))
          []
          chats))

;; TODO: optime this, for sure we don't need to re-calculate all bot subscriptions every time something in bot db changes
(defn- check-subscriptions-fx
  [db {:keys [bot key path value]}]
  (let [{:keys [bot-db chats]} db
        path'                  (or path [key])
        subscriptions          (get-in db [:bot-subscriptions path'])
        current-bot-db         (get bot-db bot)]
    {:call-jail-function-n
     (for [{:keys [bot subscriptions name]} subscriptions
           :let [subs-values (reduce
                              (fn [acc [sub-name sub-path]]
                                (assoc acc sub-name (get-in current-bot-db sub-path)))
                              {}
                              subscriptions)]]
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

(register-handler-fx
  :set-in-bot-db
  [trim-v]
  (fn [{:keys [db]} [params]]
    (set-in-bot-db db params)))

(register-handler-db
  :update-bot-db
  [trim-v]
  (fn [db [params]]
    (update-bot-db db params)))

(register-handler-db
  :register-bot-subscription
  [trim-v]
  (fn [db [{:keys [bot subscriptions] :as opts}]]
    (reduce
     (fn [db [sub-name sub-path]]
       (let [sub-path'  (if (coll? sub-path) sub-path [sub-path])
             sub-path'' (mapv keyword sub-path')]
         (update-in db [:bot-subscriptions sub-path''] conj
                    (assoc-in opts [:subscriptions sub-name] sub-path''))))
     db
     subscriptions)))

(register-handler-fx
  ::calculated-subscription
  [trim-v]
  (fn [{:keys [db]} [{:keys                  [bot path]
                      {:keys [error result]} :result}]]
    (when-not error
      (let [returned (:returned result)
            opts {:bot   bot
                  :path  path
                  :value returned}]
        (set-in-bot-db db opts)))))
