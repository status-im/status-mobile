(ns status-im.commands.handlers.jail
  (:require [clojure.string :as string]
            [re-frame.core :refer [after dispatch subscribe trim-v debug]]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.utils :refer [show-popup]]
            [status-im.utils.types :as types]
            [status-im.commands.utils :refer [reg-handler]]
            [status-im.ui.components.react :as r]
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
        (dispatch [:chat-send-message/send-command chat-id params']))

      (not (or error handler-error))
      (dispatch [:chat-send-message/send-command chat-id params])

      :else nil)))

(defn suggestions-handler!
  [{:keys [chats] :as db}
   [{:keys [chat-id bot-id default-db command parameter-index result]}]]
  (let [{:keys [markup height] :as returned} (get-in result [:result :returned])
        contains-markup? (contains? returned :markup)
        current-input (get-in chats [chat-id :input-text])
        path (if command
               [:chats chat-id :parameter-boxes (:name command) parameter-index]
               (when-not (string/blank? current-input)
                 [:chats chat-id :parameter-boxes :message]))]
    (dispatch [:choose-predefined-expandable-height :parameter-box (or (keyword height) :default)])
    (when (and contains-markup? path (not= (get-in db path) markup))
      (dispatch [:set-in path returned])
      (when default-db
        (dispatch [:update-bot-db {:bot bot-id
                                   :db  default-db}])))))

(defn suggestions-events-handler!
  [{:keys [bot-db] :as db} [bot-id [n & data :as ev] val]]
  (log/debug "Suggestion event: " n (first data) val) 
  (case (keyword n)
    :set-command-argument
    (let [[index value move-to-next?] (first data)]
      (dispatch [:set-command-argument [index value move-to-next?]]))
    :set-value
    (dispatch [:set-chat-input-text (first data)])
    :set
    (let [opts {:bot   bot-id
                :path  (mapv keyword data)
                :value val}]
      (dispatch [:set-in-bot-db opts]))
    :set-command-argument-from-db
    (let [[index arg move-to-next?] (first data)
          path  (keyword arg)
          value (str (get-in bot-db [bot-id path]))]
      (dispatch [:set-command-argument [index value move-to-next?]]))
    :set-value-from-db
    (let [path  (keyword (first data))
          value (str (get-in bot-db [bot-id path]))]
      (dispatch [:set-chat-input-text value]))
    :focus-input
    (dispatch [:chat-input-focus :input-ref])
    nil))

(defn print-error-message! [message]
  (fn [_ params]
    (when (:error (last params))
      (show-popup "Error" (string/join "\n" [message params]))
      (log/debug message params))))

;; TODO(alwx): rewrite
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

(reg-handler
  :show-suggestions-from-jail
  (handlers/side-effect!
    (fn [_ [_ {:keys [chat-id markup]}]]
      (let [markup' (types/json->clj markup)
            result  (assoc-in {} [:result :returned :markup] markup')]
        (dispatch [:suggestions-handler
                   {:result  result
                    :chat-id chat-id}])))))

(reg-handler :set-local-storage
  (handlers/side-effect!
    (fn [_ [{:keys [data chat-id]}]]
      (local-storage/set-data {:chat-id chat-id
                               :data    data}))))
