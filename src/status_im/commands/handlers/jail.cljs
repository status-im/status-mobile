(ns status-im.commands.handlers.jail
  (:require [re-frame.core :refer [register-handler after dispatch subscribe
                                   trim-v debug]]
            [status-im.utils.handlers :as u]
            [status-im.utils.utils :refer [http-get toast]]
            [status-im.components.jail :as j]

            [status-im.commands.utils :refer [json->cljs generate-hiccup
                                              reg-handler]]))

(defn init-render-command!
  [_ [chat-id command message-id data]]
  (j/call chat-id [command :render] data
          (fn [res]
            (dispatch [::render-command chat-id message-id (json->cljs res)]))))

(defn render-command
  [db [chat-id message-id markup]]
  (let [hiccup (generate-hiccup markup)]
    (assoc-in db [:rendered-commands chat-id message-id] hiccup)))

(def console-events
  {:save-password   #(dispatch [:save-password %])
   :sign-up         #(dispatch [:sign-up %])
   :confirm-sign-up #(dispatch [:sign-up-confirm %])})

(def regular-events {})

(defn print-error! [error]
  (toast error)
  (println error))

(defn command-hadler!
  [_ [{:keys [to] :as command} response]]
  (let [{:keys [error result]} (json->cljs response)]
    (if error
      (let [m (str "Error on command handling!\n" command error)]
        (print-error! m))
      (let [{:keys [event params]} result
            events (if (= "console" to)
                     (merge regular-events console-events)
                     regular-events)]
        (when-let [handler (events (keyword event))]
          (apply handler params))))))

(defn suggestions-handler!
  [db [{:keys [chat-id]} response-json]]
  (let [{:keys [error result]} (json->cljs response-json)]
    (when error
      (let [m (str "Error on param suggestions!\n" error)]
        (print-error! m)))
    (assoc-in db [:suggestions chat-id] (generate-hiccup result))))

(defn suggestions-events-handler!
  [db [[n data]]]
  (case (keyword n)
    :set-value (dispatch [:set-chat-command-content data])
    ;; todo show error?
    nil))

(defn command-preview
  [db [chat-id response-json]]
  (if-let [response (json->cljs response-json)]
    (let [path         [:chats chat-id :staged-commands]
          commands-cnt (count (get-in db path))]
      ;; todo (dec commands-cnt) looks like hack have to find better way to
      ;; do this
      (update-in db (conj path (dec commands-cnt)) assoc
                 :preview (generate-hiccup response)
                 :preview-string (str response)))
    db))

(reg-handler :init-render-command! init-render-command!)
(reg-handler ::render-command render-command)

(reg-handler :command-handler! (u/side-effect! command-hadler!))
(reg-handler :suggestions-handler
             (after #(dispatch [:animate-show-response]))
             suggestions-handler!)
(reg-handler :suggestions-event! (u/side-effect! suggestions-events-handler!))
(reg-handler :command-preview command-preview)
