(ns status-im.commands.handlers.jail
  (:require [re-frame.core :refer [after dispatch subscribe trim-v debug]]
            [status-im.utils.handlers :as u]
            [status-im.utils.utils :refer [http-get show-popup]]
            [status-im.components.status :as status]
            [status-im.utils.types :refer [json->clj]]
            [status-im.commands.utils :refer [generate-hiccup reg-handler]]
            [clojure.string :as s]
            [status-im.components.react :as r]
            [status-im.constants :refer [console-chat-id]]
            [taoensso.timbre :as log]))

(defn render-command
  [db [chat-id message-id markup]]
  (let [hiccup (generate-hiccup markup)]
    (assoc-in db [:rendered-commands chat-id message-id] hiccup)))

(defn command-hadler!
  [_ [chat-id
      {:keys [staged-command] :as parameters}
      {:keys [result error]}]]
  (let [{:keys [context returned]} result
        {handler-error :error} returned]
    (cond
      handler-error
      (log/debug :error-from-handler handler-error
                 :chat-id chat-id
                 :command staged-command)

      result
      (let [command'    (assoc staged-command :handler-data returned)
            parameters' (assoc parameters :command command')]
        (if (:eth_sendTransaction context)
          (dispatch [:wait-for-transaction (:id staged-command) parameters'])
          (dispatch [:prepare-command! parameters'])))

      (not (or error handler-error))
      (dispatch [:prepare-command! parameters])

      :else nil)))

(defn suggestions-handler!
  [db [{:keys [chat-id]} {:keys [result]}]]
  (let [{:keys [markup webViewUrl]} (:returned result)
        hiccup (generate-hiccup markup)]
    (-> db
        (assoc-in [:suggestions chat-id] (generate-hiccup markup))
        (assoc-in [:web-view-url chat-id] webViewUrl)
        (assoc-in [:has-suggestions? chat-id] (or hiccup webViewUrl)))))

(defn suggestions-events-handler!
  [db [[n data]]]
  (case (keyword n)
    :set-value (dispatch [:set-chat-command-content data])
    ;; todo show error?
    nil))

(defn command-preview
  [db [chat-id command-id {:keys [result]}]]
  (let [result' (:returned result)]
    (if result'
      (let [path [:chats chat-id :staged-commands command-id]]
        (update-in db path assoc
                   :preview (generate-hiccup result')
                   :preview-string (str result')))
      db)))

(defn print-error-message! [message]
  (fn [_ params]
    (when (:error (last params))
      (show-popup "Error" (s/join "\n" [message params]))
      (log/debug message params))))

(reg-handler ::render-command render-command)

(reg-handler :command-handler!
  (after (print-error-message! "Error on command handling"))
  (u/side-effect! command-hadler!))

(reg-handler :suggestions-handler
  [(after #(dispatch [:animate-show-response]))
   (after (print-error-message! "Error on param suggestions"))
   (after (fn [_ [{:keys [command]}]]
            (when (= :on-send (keyword (:suggestions-trigger command)))
              #_(when (:webViewUrl (:returned result))
                (dispatch [:set-soft-input-mode :pan]))
              (r/dismiss-keyboard!))))]
  suggestions-handler!)
(reg-handler :suggestions-event! (u/side-effect! suggestions-events-handler!))

(reg-handler :command-preview
  (after (print-error-message! "Error on command preview"))
  command-preview)
