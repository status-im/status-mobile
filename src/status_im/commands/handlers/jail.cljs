(ns status-im.commands.handlers.jail
  (:require [re-frame.core :refer [after dispatch subscribe trim-v debug]]
            [status-im.utils.handlers :as u]
            [status-im.utils.utils :refer [http-get show-popup]]
            [status-im.utils.types :refer [json->clj]]
            [status-im.commands.utils :refer [generate-hiccup reg-handler]]
            [clojure.string :as s]
            [status-im.components.react :as r]
            [status-im.models.commands :as cm]
            [status-im.constants :refer [console-chat-id]]
            [status-im.i18n :refer [get-contact-translated]]
            [taoensso.timbre :as log]))

(defn render-command
  [db [chat-id message-id markup]]
  (let [hiccup (generate-hiccup markup)]
    (assoc-in db [:rendered-commands chat-id message-id] hiccup)))

(defn command-handler!
  [_ [{:keys [chat-id command-message] :as parameters}
      {:keys [result error]}]]
  (let [{:keys [context returned]} result
        {handler-error :error} returned]
    (log/debug "command handler: " result error parameters)
    (cond
      handler-error
      (log/debug :error-from-handler handler-error
                 :chat-id chat-id
                 :command command-message)

      result
      (let [command'    (assoc command-message :handler-data returned)
            parameters' (assoc parameters :command command')]
        (if (:eth_sendTransaction context)
          (dispatch [:wait-for-transaction (:id command-message) parameters'])
          (dispatch [:prepare-command! chat-id parameters'])))

      (not (or error handler-error))
      (dispatch [:prepare-command! chat-id parameters])

      :else nil)))

(defn suggestions-handler!
  [{:keys [contacts chats raw-suggestions] :as db}
   [{:keys [chat-id default-db]} suggestions]]
  (when-not (= (get raw-suggestions chat-id) suggestions)
    (dispatch [:set-in [:raw-suggestions chat-id] suggestions])
    (when default-db
      (dispatch [:update-bot-db {:bot chat-id
                                 :db  default-db}]))))

(defn suggestions-events-handler!
  [{:keys [current-chat-id bot-db] :as db} [[n & data :as ev] val]]
  (log/debug "Suggestion event: " ev val)
  (let [{:keys [dapp?]} (get-in db [:contacts current-chat-id])
        command? (= :command (:type (cm/get-chat-command db)))]
    (case (keyword n)
      :set-value (if command?
                   (dispatch [:fill-chat-command-content (first data)])
                   (when dapp?
                     (dispatch [:set-chat-input-text data])))
      :set (let [opts {:bot   current-chat-id
                       :path  (mapv keyword data)
                       :value val}]
             (dispatch [:set-in-bot-db opts]))
      :set-value-from-db
      (let [path  (keyword (first data))
            value (str (get-in bot-db [current-chat-id path]))]
        (if command?
          (dispatch [:fill-chat-command-content value])
          (dispatch [:set-chat-input-text value])))
      ;; todo show error?
      nil)))

(defn print-error-message! [message]
  (fn [_ params]
    (when (:error (last params))
      (show-popup "Error" (s/join "\n" [message params]))
      (log/debug message params))))

(reg-handler ::render-command render-command)

(reg-handler :command-handler!
  (after (print-error-message! "Error on command handling"))
  (u/side-effect! command-handler!))

(reg-handler :suggestions-handler
  [(after (print-error-message! "Error on param suggestions"))]
  (u/side-effect! suggestions-handler!))

(reg-handler :suggestions-event! (u/side-effect! suggestions-events-handler!))

(reg-handler :set-local-storage
  (fn [{:keys [current-chat-id] :as db} [{:keys [data] :as event}]]
    (log/debug "Got event: " event)
    (assoc-in db [:local-storage current-chat-id] data)))
