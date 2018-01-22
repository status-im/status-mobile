(ns status-im.commands.events.jail
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.commands.utils :as commands-utils]
            [status-im.bots.events :as bots-events]
            [status-im.chat.events.animation :as animation-events]
            [status-im.chat.models :as chat-model]
            [status-im.chat.models.message :as message-model]
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

;;;; Handlers

(handlers/register-handler-fx
  :command-handler!
  message-model/send-interceptors
  (fn [{:keys [db] :as cofx}
       [chat-id
        {:keys [command] :as params}
        {:keys [result error] :as res}]]
    ;; TODO(alwx): restructure
    (log/debug "ALWX result >" res)
    (let [fx (cond
               (get-in result [:returned :error])
               (when-let [markup (get-in result [:returned :error :markup])]
                 {:db (chat-model/set-chat-ui-props db {:validation-messages markup})})

               result
               (let [command' (assoc command :handler-data (:returned result))
                     params'  (assoc params :command command')]
                 (message-model/send-command cofx nil chat-id params'))

               (not (or error (get-in result [:returned :error])))
               (message-model/send-command cofx nil chat-id params)

               :default
               nil)]
      (cond-> fx
        error
        (assoc ::print-jail-error-message ["Error on command handling" params])))))

(handlers/register-handler-fx
  :suggestions-handler
  (fn [{{:keys [chats] :as db} :db} [{:keys [chat-id bot-id default-db command parameter-index result error] :as params}]]
    (let [{:keys [markup height] :as returned} (get-in result [:result :returned])
          contains-markup? (contains? returned :markup)
          current-input    (get-in chats [chat-id :input-text])
          path             (if command
                             [:chats chat-id :parameter-boxes (:name command) parameter-index]
                             (when-not (string/blank? current-input)
                               [:chats chat-id :parameter-boxes :message]))
          new-height       (or (keyword height) :default)]
      (cond-> {:db (animation-events/choose-predefined-expandable-height db :parameter-box new-height)}

        (and contains-markup? path (not= (get-in db path) markup))
        (as-> fx'
          (log/debug "ALWX path" path returned)
          (cond-> (assoc-in fx' (into [:db] path) returned)

            default-db
            (update :db bots-events/update-bot-db {:bot bot-id
                                                   :db  default-db})))

        error
        (assoc ::print-jail-error-message ["Error on command handling" params])))))

;; TODO(alwx): rewrite
(handlers/register-handler-fx
  :suggestions-event!
  (fn [{:keys [bot-db] :as db} [bot-id [n & data :as ev] val]]
    (log/debug "Suggestion event: " n (first data) val)
    (case (keyword n)
      :set-command-argument
      (let [[index value move-to-next?] (first data)]
        (re-frame/dispatch [:set-command-argument [index value move-to-next?]]))
      :set-value
      (re-frame/dispatch [:set-chat-input-text (first data)])
      :set
      (let [opts {:bot   bot-id
                  :path  (mapv keyword data)
                  :value val}]
        (re-frame/dispatch [:set-in-bot-db opts]))
      :set-command-argument-from-db
      (let [[index arg move-to-next?] (first data)
            path  (keyword arg)
            value (str (get-in bot-db [bot-id path]))]
        (re-frame/dispatch [:set-command-argument [index value move-to-next?]]))
      :set-value-from-db
      (let [path  (keyword (first data))
            value (str (get-in bot-db [bot-id path]))]
        (re-frame/dispatch [:set-chat-input-text value]))
      :focus-input
      (re-frame/dispatch [:chat-input-focus :input-ref])
      nil)))

(handlers/register-handler-fx
  :show-suggestions-from-jail
  [re-frame/trim-v]
  (fn [_ [{:keys [chat-id markup]}]]
    (let [markup' (types/json->clj markup)
          result  (assoc-in {} [:result :returned :markup] markup')]
      (re-frame/dispatch [:suggestions-handler
                          {:result  result
                           :chat-id chat-id}]))))

(handlers/register-handler-fx
  :set-local-storage
  (fn [_ [{:keys [data chat-id]}]]
    (local-storage/set-data {:chat-id chat-id
                             :data    data})))
