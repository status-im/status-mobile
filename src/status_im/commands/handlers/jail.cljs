(ns status-im.commands.handlers.jail
  (:require [re-frame.core :refer [register-handler after dispatch subscribe
                                   trim-v debug]]
            [status-im.utils.handlers :as u]
            [status-im.utils.utils :refer [http-get toast]]
            [status-im.components.jail :as j]
            [status-im.utils.types :refer [json->clj]]
            [status-im.commands.utils :refer [generate-hiccup reg-handler]]
            [clojure.string :as s]))

(defn init-render-command!
  [_ [chat-id command message-id data]]
  (j/call chat-id [command :render] data
          #(dispatch [::render-command chat-id message-id %])))

(defn render-command
  [db [chat-id message-id markup]]
  (let [hiccup (generate-hiccup markup)]
    (assoc-in db [:rendered-commands chat-id message-id] hiccup)))

(def console-events
  {:save-password   #(dispatch [:save-password %])
   :sign-up         #(dispatch [:sign-up %])
   :confirm-sign-up #(dispatch [:sign-up-confirm %])})

(def regular-events {})

(defn command-hadler!
  [_ [{:keys [to]} {:keys [result]} ]]
  (when result
    (let [{:keys [event params]} result
          events (if (= "console" to)
                   (merge regular-events console-events)
                   regular-events)]
      (when-let [handler (events (keyword event))]
        (apply handler params)))))

(defn suggestions-handler!
  [db [{:keys [chat-id]} {:keys [result]} ]]
  (assoc-in db [:suggestions chat-id] (generate-hiccup result)))

(defn suggestions-events-handler!
  [db [[n data]]]
  (case (keyword n)
    :set-value (dispatch [:set-chat-command-content data])
    ;; todo show error?
    nil))

(defn command-preview
  [db [chat-id {:keys [result]}]]
  (if result
    (let [path         [:chats chat-id :staged-commands]
          commands-cnt (count (get-in db path))]
      ;; todo (dec commands-cnt) looks like hack have to find better way to
      ;; do this
      (update-in db (conj path (dec commands-cnt)) assoc
                 :preview (generate-hiccup result)
                 :preview-string (str result)))
    db))

(defn print-error-message! [message]
  (fn [_ params]
    (when (:error (last params))
      (toast (s/join "\n" [message params]))
      (println message params))))

(reg-handler :init-render-command! init-render-command!)
(reg-handler ::render-command render-command)

(reg-handler :command-handler!
             (after (print-error-message! "Error on command handling"))
             (u/side-effect! command-hadler!))
(reg-handler :suggestions-handler
             [(after #(dispatch [:animate-show-response]))
              (after (print-error-message! "Error on param suggestions"))]
             suggestions-handler!)
(reg-handler :suggestions-event! (u/side-effect! suggestions-events-handler!))
(reg-handler :command-preview
             (after (print-error-message! "Error on command preview"))
             command-preview)
