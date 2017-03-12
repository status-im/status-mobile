(ns status-im.commands.handlers.jail
  (:require [re-frame.core :refer [after dispatch subscribe trim-v debug]]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.utils :refer [http-get show-popup]]
            [status-im.utils.types :refer [json->clj]]
            [status-im.commands.utils :refer [generate-hiccup reg-handler]]
            [clojure.string :as s]
            [status-im.components.react :as r]
            [status-im.models.commands :as cm]
            [status-im.constants :refer [console-chat-id]]
            [status-im.i18n :refer [get-contact-translated]]
            [taoensso.timbre :as log]))

(defn command-handler!
  [_ [chat-id
      {:keys [command-message] :as parameters}
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

(defn print-error-message! [message]
  (fn [_ params]
    (when (:error (last params))
      (show-popup "Error" (s/join "\n" [message params]))
      (log/debug message params))))

(reg-handler :command-handler!
  (after (print-error-message! "Error on command handling"))
  (handlers/side-effect! command-handler!))

(reg-handler
  :suggestions-handler
  [(after (print-error-message! "Error on param suggestions"))]
  (fn [{:keys [contacts chats] :as db} [{:keys [chat-id command parameter-index result]}]]
    (let [{:keys [markup]} (get-in result [:result :returned])
          {:keys [dapp? dapp-url]} (get contacts chat-id)
          hiccup       (generate-hiccup markup)
          path         (if command
                         [:chats chat-id :parameter-boxes (:name command) parameter-index]
                         [:chats chat-id :parameter-boxes :message])]
      (assoc-in db path (when hiccup
                          {:hiccup hiccup})))))

(reg-handler
  :suggestions-event!
  (handlers/side-effect!
    (fn [{:keys [current-chat-id] :as db} [[n arg]]]
      (let [{:keys [dapp?]} (get-in db [:contacts current-chat-id])]
        (case (keyword n)
          :set-command-argument (dispatch [:set-command-argument arg])
          :set-value (dispatch [:set-chat-input-text arg])
          nil)))))

(reg-handler :set-local-storage
  (fn [{:keys [current-chat-id] :as db} [{:keys [data] :as event}]]
    (log/debug "Got event: " event)
    (assoc-in db [:local-storage current-chat-id] data)))
