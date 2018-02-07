(ns status-im.bots.events
  (:require [re-frame.core :as re-frame]
            [status-im.utils.core :as utils]
            [status-im.utils.handlers :as handlers]
            [status-im.chat.models.input :as input-model]
            [taoensso.timbre :as log]))

;;;; Helper fns

(defn- subscription-values [sub-params current-bot-db]
  (reduce (fn [sub-values [sub-key sub-path]]
            (assoc sub-values sub-key (get-in current-bot-db sub-path)))
          {}
          sub-params))

(defn- check-subscriptions-fx
  [{:keys [bot-db] :contacts/keys [contacts] :as app-db} {:keys [bot path]}]
  (when-let [subscriptions (and bot (get-in contacts (concat [bot :subscriptions] [path])))]
    {:call-jail-function-n
     (for [[sub-name sub-params] subscriptions]
       {:chat-id  bot
        :function :subscription
        :parameters {:name          sub-name
                     :subscriptions (subscription-values sub-params (get bot-db bot))}
        :callback-event-creator (fn [jail-response]
                                  [::calculated-subscription
                                   {:bot    bot
                                    :path   [sub-name]
                                    :result jail-response}])})}))

(defn set-in-bot-db
  "Associates value at specified path in bot-db and checks if there are any subscriptions
  dependent on that path, if yes, adds effects for calling jail-functions to recalculate
  relevant subscriptions."
  [app-db {:keys [bot path value] :as params}]
  (let [new-db (assoc-in app-db (concat [:bot-db bot] path) value)]
    (merge {:db new-db}
           (check-subscriptions-fx new-db params))))

(defn update-bot-db
  [app-db {:keys [bot db]}]
  (update-in app-db [:bot-db bot] merge db))

(defn clear-bot-db
  [app-db bot-id]
  (assoc-in app-db [:bot-db bot-id] nil))

(def ^:private keywordize-vector (partial mapv keyword))

(defn transform-bot-subscriptions
  "Transforms bot subscriptions as returned from jail in the following format:

  `{:calculatedFee {:subscriptions {:value [\"sliderValue\"]
                                    :tx [\"transaction\"]}}
    :feeExplanation {:subscriptions {:value [\"sliderValue\"]}}}`

  into data-structure better suited for subscription lookups based on changes
  in `:bot-db`:

  `{[:sliderValue] {:calculatedFee {:value [:sliderValue]
                                    :tx [:transaction]}
                    :feeExplanation {:value [:sliderValue]}}
    [:transaction] {:calculatedFee {:value [:sliderValue]
                                    :tx [:transaction]}}}`

  In the resulting data-structure, the top level keys are the (keywordized) paths
  in the `:bot-db` data structure, so it's quick and easy to look-up all the
  subscriptions which must be recalculated when something in their path changes."
  [bot-subscriptions]
  (reduce-kv (fn [acc sub-key {:keys [subscriptions]}]
               (reduce-kv (fn [acc sub-param-key sub-param-path]
                            (update acc
                                    (keywordize-vector sub-param-path)
                                    assoc sub-key
                                    (utils/map-values subscriptions keywordize-vector)))
                          acc
                          subscriptions))
             {}
             bot-subscriptions))

(defn calculated-subscription
  [db {:keys                  [bot path]
       {:keys [error result]} :result}]
  (if error
    db
    (assoc-in db (concat [:bot-db bot] path) (:returned result))))

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
  (fn [db [params]]
    (calculated-subscription db params)))
