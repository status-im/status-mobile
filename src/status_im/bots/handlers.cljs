(ns status-im.bots.handlers
  (:require [re-frame.core :as re-frame]
            [status-im.native-module.core :as status]
            [status-im.utils.handlers :as u]))

(defn chats-with-bot [chats bot]
  (reduce (fn [acc [_ {:keys [chat-id contacts]}]]
            (let [contacts (map :identity contacts)]
              (if (some #{bot} contacts)
                (conj acc chat-id)
                acc)))
          []
          chats))

(defn check-subscriptions
  [{:keys [bot-db chats] :as db} [handler {:keys [path key bot]}]]
  (let [path'          (or path [key])
        subscriptions  (get-in db [:bot-subscriptions path'])
        current-bot-db (get bot-db bot)]
    (doseq [{:keys [bot subscriptions name]} subscriptions]
      (let [subs-values (reduce (fn [res [sub-name sub-path]]
                                  (assoc res sub-name (get-in current-bot-db sub-path)))
                                {} subscriptions)]
        (status/call-function!
          {:chat-id    bot
           :function   :subscription
           :parameters {:name          name
                        :subscriptions subs-values}
           :callback   #(do
                          (re-frame/dispatch
                            [::calculated-subscription {:bot    bot
                                                        :path   [name]
                                                        :result %}])
                          (doseq [chat-id (chats-with-bot chats bot)]
                            (re-frame/dispatch
                              [::calculated-subscription {:bot    chat-id
                                                          :path   [name]
                                                          :result %}])))})))))

(u/register-handler :set-in-bot-db
  (re-frame/after check-subscriptions)
  (fn [{:keys [current-chat-id] :as db} [_ {:keys [bot path value]}]]
    (let [bot (or bot current-chat-id)]
      (assoc-in db (concat [:bot-db bot] path) value))))

(u/register-handler :register-bot-subscription
  (fn [db [_ {:keys [bot subscriptions] :as opts}]]
    (reduce
      (fn [db [sub-name sub-path]]
        (let [sub-path'  (if (coll? sub-path) sub-path [sub-path])
              sub-path'' (mapv keyword sub-path')]
          (update-in db [:bot-subscriptions sub-path''] conj
                     (assoc-in opts [:subscriptions sub-name] sub-path''))))
      db
      subscriptions)))

(u/register-handler ::calculated-subscription
  (u/side-effect!
    (fn [_ [_ {:keys                  [bot path]
               {:keys [error result]} :result
               :as                    data}]]
      (when-not error
        (let [returned (:returned result)
              opts {:bot   bot
                    :path  path
                    :value returned}]
          (re-frame/dispatch [:set-in-bot-db opts]))))))

(u/register-handler :update-bot-db
  (fn [{:keys [current-chat-id] :as app-db} [_ {:keys [bot db]}]]
    (let [bot (or bot current-chat-id)]
      (update-in app-db [:bot-db bot] merge db))))

(u/register-handler :clear-bot-db
  (fn [{:keys [current-chat-id] :as app-db} [_ {:keys [bot]}]]
    (let [bot (or bot current-chat-id)]
      (assoc-in app-db [:bot-db bot] nil))))
