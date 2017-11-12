(ns status-im.chat.events.webview-bridge
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.nfc :as nfc]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

;;;; Effects

(re-frame/reg-fx
  ::send-to-bridge
  (fn [[webview-bridge data]]
    (when webview-bridge
      (.sendToBridge webview-bridge (types/clj->json data)))))

;; TODO(alwx): continue

(handlers/register-handler-db
  :chat-webview-bridge/set-ref
  (fn [db [_ ref]]
    (assoc db :webview-bridge ref)))

(handlers/register-handler
  :chat-webview-bridge/process-message
  (handlers/side-effect!
    (fn [_ [_ message-string]]
      (let [{:keys [event options] :as message} (types/json->clj message-string)
            event' (keyword event)
            params (:data options)]
        (log/debug (str "message from webview: " message))
        (case event'
          :nfc (re-frame/dispatch [:webview-nfc params])
          (log/error (str "Unknown event: " event')))))))

(handlers/register-handler :send-to-webview-bridge
  (handlers/side-effect!
    (fn [{:keys [webview-bridge]} [_ data]]
      (when webview-bridge
        (.sendToBridge webview-bridge (types/clj->json data))))))

(handlers/register-handler :webview-nfc
  (handlers/side-effect!
    (fn [_ [_ {:keys [event params]}]]
      (let [callback #(re-frame/dispatch [:send-to-webview-bridge {:params % :event "nfc"}])]
        (case (keyword event)
          :get-card-id (nfc/get-card-id #(callback {:event :get-card-id :card %})
                                        #(callback {:event :get-card-id :error %}))
          :read-tag    (let [{:keys [sectors]} params]
                         (nfc/read-tag sectors
                                       #(callback {:event :read-tag :card %})
                                       #(callback {:event :read-tag :error %})))
          :write-tag   (let [{:keys [sectors id]} params]
                         (nfc/write-tag sectors
                                        id
                                        #(callback {:event :write-tag :card %})
                                        #(callback {:event :write-tag :error %})))
          :default)))))
