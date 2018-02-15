(ns status-im.commands.events.jail
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.bots.events :as bots-events]
            [status-im.chat.events.input :as input.events]
            [status-im.chat.models :as chat-model]
            [status-im.chat.models.message :as message-model]
            [status-im.commands.utils :as commands-utils]
            [status-im.data-store.local-storage :as local-storage]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.utils :as utils]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

;;;; Effects

(re-frame/reg-fx
  ::print-jail-error-message
  (fn [[message params]]
    (utils/show-popup "Error" (string/join "\n" [message params]))
    (log/debug message params)))

;;;; Helper functions

(defn- handle-suggestions
  [{:keys [chats] :as db}
   {:keys [chat-id bot-id default-db command parameter-index result error] :as params}]
  (let [{:keys [markup height] :as returned} (get-in result [:result :returned])
        contains-markup? (contains? returned :markup)
        current-input    (get-in chats [chat-id :input-text])
        path             (if command
                           [:chats chat-id :parameter-boxes (:name command) parameter-index]
                           (when-not (string/blank? current-input)
                             [:chats chat-id :parameter-boxes :message]))]
    (cond-> {:db db}

      (and contains-markup? path (not= (get-in db path) markup))
      (as-> fx
        (cond-> (assoc-in fx (into [:db] path) returned)

          default-db
          (update :db bots-events/update-bot-db {:bot bot-id
                                                 :db  default-db})))

      error
      (assoc ::print-jail-error-message ["Error on command handling" params]))))

;;;; Handlers

(handlers/register-handler-fx
  :command-handler!
  message-model/send-interceptors
  (fn [{:keys [db] :as cofx} [chat-id {:keys [command] :as params} {:keys [result error]}]]
    (let [result-error (get-in result [:returned :error])]
      (cond-> {:db db}
        error
        (assoc ::print-jail-error-message ["Error on command handling" params])

        result-error
        (as-> fx
          (if-let [markup (get result-error :markup)]
            (assoc fx :db (chat-model/set-chat-ui-props db {:validation-messages markup}))
            fx))

        (and result (not result-error))
        (as-> fx
          (let [command' (assoc command :handler-data (:returned result))
                params'  (assoc params :command command')]
            (message-model/send-command (merge cofx fx) chat-id params')))))))

(handlers/register-handler-fx
  :suggestions-handler
  [re-frame/trim-v]
  (fn [{:keys [db]} [params]]
    (handle-suggestions db params)))

(handlers/register-handler-fx
  :suggestions-event!
  [re-frame/trim-v]
  (fn [{{:keys [bot-db] :as db} :db} [bot-id [n & data :as ev] val]]
    (log/debug "Suggestion event: " n (first data) val)
    (case (keyword n)

      :set-command-argument
      (let [[index value move-to-next?] (first data)]
        {:db (input.events/set-command-argument db index value move-to-next?)})

      :set-value
      (-> (input.events/set-chat-input-text db (first data))
          (input.events/call-on-message-input-change))

      :set
      (bots-events/set-in-bot-db db {:bot   bot-id
                                     :path  (mapv keyword data)
                                     :value val})

      :set-command-argument-from-db
      (let [[index arg move-to-next?] (first data)
            path  (keyword arg)
            value (str (get-in bot-db [bot-id path]))]
        (input.events/set-command-argument db index value move-to-next?))

      :set-value-from-db
      (let [path  (keyword (first data))
            value (str (get-in bot-db [bot-id path]))]
        (-> (input.events/set-chat-input-text db value)
            (input.events/call-on-message-input-change)))

      :focus-input
      (input.events/chat-input-focus db :input-ref)

      {:db db})))

(handlers/register-handler-fx
  :show-suggestions-from-jail
  [re-frame/trim-v]
  (fn [{:keys [db]} [{:keys [chat-id markup]}]]
    (let [result {:result {:returned {:markup (types/json->clj markup)}}}]
      (handle-suggestions db {:result  result
                              :chat-id chat-id}))))

(handlers/register-handler-fx
  :set-local-storage
  (fn [_ [{:keys [data chat-id]}]]
    (local-storage/set-data {:chat-id chat-id
                             :data    data})))

