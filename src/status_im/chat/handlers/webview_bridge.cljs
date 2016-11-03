(ns status-im.chat.handlers.webview-bridge
  (:require [re-frame.core :refer [after dispatch enrich]]
            [status-im.utils.handlers :refer [register-handler]]
            [status-im.utils.handlers :as u]
            [status-im.utils.types :as t]
            [status-im.i18n :refer [label]]
            [taoensso.timbre :as log]
            [status-im.models.commands :as commands]
            [status-im.commands.utils :as cu]
            [status-im.components.status :as s]
            [status-im.constants :as c]
            [cljs.reader :refer [read-string]]))

(def web3 (js/require "web3"))

(defn by-public-key [public-key contacts]
  (when-let [{:keys [address]} (contacts public-key)]
    (when address {:address address})))

(defn scan-qr-handler
  [{:keys [contacts]} [_ _ data]]
  (let [data'  (read-string data)
        data'' (cond
                 (map? data') data'
                 (.isAddress web3.prototype data') {:address data'}
                 (string? data') (by-public-key data' contacts)
                 :else nil)]
    (when data''
      (dispatch [:send-to-webview-bridge
                 {:params data''
                  :event  (name :webview-send-transaction)}]))))

(register-handler :webview-address-from-qr
  (u/side-effect! scan-qr-handler))

(register-handler :set-webview-bridge
  (fn [db [_ bridge]]
    (assoc db :webview-bridge bridge)))

(defn contacts-click-handler [whisper-identity action params]
  (dispatch [:navigate-back])
  (when action
    (if (= whisper-identity :qr-scan)
      (if (= action :send)
        (dispatch [:show-scan-qr :webview-address-from-qr])
        (dispatch [:navigate-to-modal :wallet-qr-code]))
      (dispatch [:chat-with-command whisper-identity action params]))))

(register-handler ::send-command
  (u/side-effect!
    (fn [db [_ command-key params]]
      (let [command       (commands/get-response-or-command :commands db command-key)
            command-input {:content       (str cu/command-prefix "0")
                           :command       command
                           :parameter-idx 0
                           :params        {"amount" (:amount params)}
                           :to-message-id nil}]
        (dispatch [:stage-command command-input command])))))


(defn chat-with-command
  [_ [_ whisper-identity command-key params]]
  (dispatch [:remove-contacts-click-handler])
  (dispatch [:add-chat-loaded-callback whisper-identity
             #(dispatch [::send-command command-key params])])
  (dispatch [:start-chat whisper-identity]))

(register-handler :chat-with-command
  (u/side-effect! chat-with-command))

(register-handler :webview-bridge-message
  (u/side-effect!
    (fn [_ [_ message-string]]
      (let [{:keys [event options] :as message} (t/json->clj message-string)
            event' (keyword event)
            params (:data options)]
        (log/debug (str "message from webview: " message))
        (case event'
          :webview-send-transaction (dispatch [:show-contacts-menu contacts-click-handler :send params])
          :webview-receive-transaction (dispatch [:show-contacts-menu contacts-click-handler :request params])
          :webview-scan-qr (dispatch [:show-scan-qr :webview-address-from-qr])
          :webview-send-eth (dispatch [:webview-send-eth! params])
          (log/error (str "Unknown event: " event')))))))

(register-handler :show-contacts-menu
  (after #(dispatch [:navigate-to-modal :contact-list-modal]))
  (fn [db [_ click-handler action params]]
    (assoc db :contacts-click-handler click-handler
              :contacts-click-action action
              :contacts-click-params params)))

(def qr-context {:toolbar-title (label :t/address)})

(register-handler :show-scan-qr
  (after #(dispatch [:navigate-to-modal :qr-scanner qr-context]))
  (fn [db [_ click-handler]]
    (assoc-in db [:qr-codes qr-context] click-handler)))

(register-handler :send-to-webview-bridge
  (u/side-effect!
    (fn [{:keys [webview-bridge]} [_ data]]
      (when webview-bridge
        (.sendToBridge webview-bridge (t/clj->json data))))))

(register-handler :webview-send-eth!
  (u/side-effect!
    (fn [{:keys [current-account-id]} [_ {:keys [amount address]}]]
      (let [context    {:from current-account-id}
            path       [:functions :send]
            parameters {:context    context
                        :parameters {:amount  amount
                                     :address address}}]
        (s/call-jail c/wallet-chat-id
                     path
                     parameters
                     (fn [data]
                       (log/debug :webview-send-eth-callback data)))))))
