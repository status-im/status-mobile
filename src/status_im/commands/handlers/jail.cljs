(ns status-im.commands.handlers.jail
  (:require [re-frame.core :refer [after dispatch subscribe trim-v debug]]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.utils :refer [show-popup]]
            [status-im.utils.types :refer [json->clj]]
            [status-im.commands.utils :refer [generate-hiccup reg-handler]]
            [clojure.string :as s]
            [status-im.components.react :as r]
            [status-im.constants :refer [console-chat-id]]
            [status-im.i18n :refer [get-contact-translated]]
            [taoensso.timbre :as log] 
            [status-im.data-store.local-storage :as local-storage]))

(defn command-handler!
  [_ [chat-id
      {:keys [command] :as params}
      {:keys [result error]}]]
  (let [{:keys [returned]} result
        {handler-error :error} returned]
    (cond
      handler-error
      (when-let [markup (:markup handler-error)]
        (dispatch [:set-chat-ui-props {:validation-messages markup}]))

      result
      (let [command' (assoc command :handler-data returned)
            params'  (assoc params :command command')] 
        (dispatch [:prepare-command! chat-id params']))

      (not (or error handler-error))
      (dispatch [:prepare-command! chat-id params])

      :else nil)))

(defn suggestions-handler!
  [{:keys [chats] :as db}
   [{:keys [chat-id default-db command parameter-index result]}]]
  (let [{:keys [markup height] :as returned} (get-in result [:result :returned])
        contains-markup? (contains? returned :markup)
        path (if command
               [:chats chat-id :parameter-boxes (:name command) parameter-index]
               [:chats chat-id :parameter-boxes :message])]
    (dispatch [:choose-predefined-expandable-height :parameter-box (or (keyword height) :default)])
    (when (and contains-markup? (not= (get-in db path) markup))
      (dispatch [:set-in path returned])
      (when default-db
        (dispatch [:update-bot-db {:bot chat-id
                                   :db  default-db}])))))

(defn suggestions-events-handler!
  [{:keys [current-chat-id bot-db] :as db} [[n & data :as ev] val]]
  (log/debug "Suggestion event: " n (first data) val)
  (let [{:keys [dapp?]} (get-in db [:contacts/contacts current-chat-id])]
    (case (keyword n)
      :set-command-argument
      (let [[index value move-to-next?] (first data)]
        (dispatch [:set-command-argument [index value move-to-next?]]))
      :set-value
      (dispatch [:set-chat-input-text (first data)])
      :set
      (let [opts {:bot   current-chat-id
                  :path  (mapv keyword data)
                  :value val}]
        (dispatch [:set-in-bot-db opts]))
      :set-command-argument-from-db
      (let [[index arg move-to-next?] (first data)
            path  (keyword arg)
            value (str (get-in bot-db [current-chat-id path]))]
        (dispatch [:set-command-argument [index value move-to-next?]]))
      :set-value-from-db
      (let [path  (keyword (first data))
            value (str (get-in bot-db [current-chat-id path]))]
        (dispatch [:set-chat-input-text value]))
      :focus-input
      (dispatch [:chat-input-focus :input-ref])
      nil)))

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
  (handlers/side-effect! suggestions-handler!))

(reg-handler
  :suggestions-event!
  (handlers/side-effect! suggestions-events-handler!))

(reg-handler :set-local-storage
  (handlers/side-effect!
    (fn [_ [{:keys [data chat-id]}]]
      (local-storage/set-data {:chat-id chat-id
                               :data    data}))))
