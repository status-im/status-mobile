(ns status-im.chat.handlers.webview-bridge
  (:require [re-frame.core :refer [after dispatch enrich]]
            [status-im.utils.handlers :refer [register-handler]]
            [status-im.utils.handlers :as u]
            [status-im.utils.types :as t]
            [taoensso.timbre :as log]))

(register-handler :set-webview-bridge
  (fn [db [_ bridge]]
    (assoc db :webview-bridge bridge)))

(defn contacts-click-handler [whisper-identity]
  #(dispatch [:chat-with-command whisper-identity :send]))

(defn chat-with-command
  [_ [_ whisper-identity command]]
  (dispatch [:start-chat whisper-identity {} :navigate-back])
  (dispatch [:remove-contacts-click-handler])
  (let [callback #(dispatch [:set-chat-command command])]
    (dispatch [:add-commands-loading-callback whisper-identity callback])))

(register-handler :chat-with-command
  (u/side-effect! chat-with-command))

(register-handler :webview-bridge-message
  (u/side-effect!
    (fn [_ [_ message-string]]
      (let [message (t/json->clj message-string)
            event   (keyword (:event message))]
        (log/debug (str "message from webview: " message))
        (case event
          :webview-send-transaction (dispatch [:navigate-to :contact-list contacts-click-handler])
          (log/error (str "Unknown event: " event)))))))

(register-handler :send-to-webview-bridge
  (u/side-effect!
    (fn [{:keys [webview-bridge]} [_ data]]
      (when webview-bridge
        (.sendToBridge webview-bridge (t/clj->json data))))))
