(ns status-im.bots.handlers
  (:require [re-frame.core :as re-frame]
            [status-im.components.status :as status]
            [status-im.utils.handlers :as u]))

(defn check-subscriptions
  [{:keys [bot-db] :as db} [handler {:keys [path key bot]}]]
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
           :callback   #(re-frame/dispatch
                          [::calculated-subscription {:bot    bot
                                                      :path   [name]
                                                      :result %}])})))))

(u/register-handler
  :set-bot-db
  (re-frame/after check-subscriptions)
  (fn [db [_ {:keys [bot key value]}]]
    (assoc-in db [:bot-db bot key] value)))

(u/register-handler
  :set-in-bot-db
  (re-frame/after check-subscriptions)
  (fn [db [_ {:keys [bot path value]}]]
    (assoc-in db (concat [:bot-db bot] path) value)))

(u/register-handler
  :register-bot-subscription
  (fn [db [_ {:keys [bot subscriptions] :as opts}]]
    (reduce
      (fn [db [sub-name sub-path]]
        (let [sub-path'  (if (coll? sub-path) sub-path [sub-path])
              sub-path'' (mapv keyword sub-path')]
          (update-in db [:bot-subscriptions sub-path''] conj
                     (assoc-in opts [:subscriptions sub-name] sub-path''))))
      db
      subscriptions)))

(u/register-handler
  ::calculated-subscription
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

(u/register-handler
  :update-bot-db
  (fn [app-db [_ {:keys [bot db]}]]
    (update-in app-db [:bot-db bot] merge db)))
