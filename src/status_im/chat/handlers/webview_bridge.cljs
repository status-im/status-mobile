(ns status-im.chat.handlers.webview-bridge
  (:require [re-frame.core :refer [after dispatch enrich]]
            [status-im.utils.handlers :refer [register-handler]]
            [status-im.utils.handlers :as u]
            [status-im.utils.types :as t]
            [status-im.i18n :refer [label]]
            [taoensso.timbre :as log]
            [status-im.commands.utils :as cu]
            [status-im.native-module.core :as s]
            [status-im.components.nfc :as nfc]
            [status-im.constants :as c]
            [cljs.reader :refer [read-string]]
            [status-im.ui.screens.navigation :as nav]
            [cljs.spec.alpha :as spec]))

(defn by-public-key [public-key contacts]
  (when-let [{:keys [address]} (contacts public-key)]
    (when address {:address address})))

(defn wrap-hex [s]
  (if (js/isNaN (.parseInt js/Number s))
    s
    (str "\"" s "\"")))

(defn scan-qr-handler
  [{:contacts/keys [contacts]} [_ _ data]]
  (let [data'  (try (read-string (wrap-hex data))
                    (catch :default e data))
        data'' (cond
                 (map? data') data'
                 (spec/valid? :global/address data') {:address data'}
                 (string? data') (by-public-key data' contacts)
                 :else nil)]
    (when data''
      (dispatch [:send-to-webview-bridge
                 {:params data''
                  :event  (name :webview-send-transaction)}]))
    (dispatch [:navigate-back])))

(register-handler :webview-address-from-qr
  (u/side-effect! scan-qr-handler))

(register-handler :set-webview-bridge
  (fn [db [_ bridge]]
    (assoc db :webview-bridge bridge)))

(defn contacts-click-handler
  [{:keys [whisper-identity] :as contact} action params]
  (dispatch [:navigate-back])
  (when action
    (if (= contact :qr-scan)
      (if (= action :send)
        (dispatch [:show-scan-qr :webview-address-from-qr])
        (dispatch [:navigate-to-modal :qr-code-view {:qr-source :whisper-identity
                                                     :amount?   true}]))
      (dispatch [:chat-with-command whisper-identity action
                 (assoc params :contact contact)]))))


(register-handler ::send-command
  (u/side-effect!
    (fn [{:keys [current-chat-id]
          :contacts/keys [contacts]}
         [_ command-key {:keys [contact amount]}]]
      (let [command (get-in contacts [current-chat-id :commands command-key])]
        (dispatch [:set-in [:bot-db current-chat-id :public :recipient] contact])
        (dispatch [:proceed-command
                   {:command  command,
                    :metadata nil,
                    :args     [(get contact :name) amount]}])))))

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
          :webview-send-transaction
          (dispatch [:navigate-to-modal
                     :contact-list-modal
                     {:handler contacts-click-handler
                      :action  :send
                      :params  params}])
          :webview-receive-transaction
          (dispatch [:navigate-to-modal
                     :contact-list-modal
                     {:handler contacts-click-handler
                      :action  :request
                      :params  params}])
          :webview-scan-qr (dispatch [:show-scan-qr :webview-address-from-qr])
          :webview-send-eth (dispatch [:webview-send-eth! params])
          :nfc (dispatch [:webview-nfc params])
          (log/error (str "Unknown event: " event')))))))

(defmethod nav/preload-data! :contact-list-modal
  [db [_ _ {:keys [handler action params]}]]
  (assoc db :contacts/click-handler handler
            :contacts/click-action action
            :contacts/click-params params))

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
    (fn [{:accounts/keys [current-account-id]} [_ {:keys [amount address]}]]
      (let [context    {:from current-account-id}
            path       [:functions :send]
            parameters {:context    context
                        :parameters {:amount  amount
                                     :address address}}]
        (s/call-jail
          {:jail-id  c/wallet-chat-id
           :path     path
           :params   parameters
           :callback (fn [data]
                       (log/debug :webview-send-eth-callback data))})))))

(register-handler :webview-nfc
  (u/side-effect!
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
