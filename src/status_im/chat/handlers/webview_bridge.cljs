(ns status-im.chat.handlers.webview-bridge
  (:require [re-frame.core :refer [dispatch]]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]
            [status-im.ui.components.nfc :as nfc]))

(handlers/register-handler :set-webview-bridge
  (fn [db [_ bridge]]
    (assoc db :webview-bridge bridge)))

(handlers/register-handler :webview-bridge-message
  (handlers/side-effect!
    (fn [_ [_ message-string]]
      (let [{:keys [event options] :as message} (types/json->clj message-string)
            event' (keyword event)
            params (:data options)]
        (log/debug (str "message from webview: " message))
        (case event'
          :nfc (dispatch [:webview-nfc params])
          (log/error (str "Unknown event: " event')))))))

(handlers/register-handler :send-to-webview-bridge
  (handlers/side-effect!
    (fn [{:keys [webview-bridge]} [_ data]]
      (when webview-bridge
        (.sendToBridge webview-bridge (types/clj->json data))))))

(handlers/register-handler :webview-nfc
  (handlers/side-effect!
    (fn [_ [_ {:keys [event params]}]]
      (let [callback #(dispatch [:send-to-webview-bridge {:params % :event "nfc"}])]
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
