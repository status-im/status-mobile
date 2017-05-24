(ns status-im.debug.handlers
  (:require [re-frame.core :refer [after dispatch]]
            [status-im.utils.handlers :refer [register-handler] :as u]
            [status-im.components.react :refer [http-bridge]]
            [status-im.data-store.accounts :as accounts]
            [taoensso.timbre :as log]))

(def debug-server-port 5561)

(register-handler :init-debug-mode
  (u/side-effect!
    (fn [_ [_ address]]
      (let [{:keys [debug?]} (accounts/get-by-address address)]
        (when debug?
          (dispatch [:debug-server-start]))))))

(register-handler :debug-server-start
  (u/side-effect!
    (fn [_]
      (.start http-bridge
              debug-server-port
              (fn [req]
                (try
                  (let [{:keys [postData url]} (js->clj req :keywordize-keys true)
                        postData (if (string? postData)
                                   (-> (.parse js/JSON postData)
                                       (js->clj :keywordize-keys true))
                                   postData)]
                    (dispatch [:debug-request {:url url :postData postData}]))
                  (catch js/Error e
                    (log/debug "Error: " e))))))))

(register-handler :debug-server-stop
  (u/side-effect!
    (fn [_]
      (.stop http-bridge))))

(register-handler :debug-request
  (u/side-effect!
    (fn [{:keys [web3]} [_ {url               :url
                            {:keys [encoded]} :postData}]]
      (try
        (let [json (->> (.toAscii web3 encoded)
                        (.parse js/JSON))
              obj  (js->clj json :keywordize-keys true)]
          (case url
            "/add-dapp" (dispatch [:debug-add-contact obj])
            "/remove-dapp" (dispatch [:debug-remove-contact obj])
            "/dapp-changed" (dispatch [:debug-contact-changed obj])
            "/switch-node" (dispatch [:debug-switch-node obj])
            :default))
        (catch js/Error e
          (log/debug "Error: " e))))))

(register-handler :debug-add-contact
  (u/side-effect!
    (fn [{:keys [contacts]} [_ {:keys [name whisper-identity dapp-url bot-url] :as dapp-data}]]
      (when (and name
                 whisper-identity
                 (or dapp-url bot-url)
                 (or (not (get contacts whisper-identity))
                     (get-in contacts [whisper-identity :debug?])))
        (let [dapp (merge dapp-data {:dapp?  true
                                     :debug? true})]
          (dispatch [:upsert-chat! {:chat-id whisper-identity
                                    :name    name
                                    :debug?  true}])
          (if (get contacts whisper-identity)
            (dispatch [:update-contact! dapp])
            (do (dispatch [:add-contacts [dapp]])
                (dispatch [:open-chat-with-contact dapp]))))))))

(register-handler :debug-remove-contact
  (u/side-effect!
    (fn [{:keys [chats]} [_ {:keys [whisper-identity]}]]
      (when (get-in chats [whisper-identity :debug?])
        (dispatch [:remove-chat whisper-identity]))
      (dispatch [:remove-contact whisper-identity #(and (:dapp? %) (:debug? %))]))))

(register-handler :debug-contact-changed
  (u/side-effect!
    (fn [{:keys [webview-bridge current-chat-id contacts]} [_ {:keys [whisper-identity] :as dapp-data}]]
      (when (get-in contacts [whisper-identity :debug?])
        (when (and (= current-chat-id whisper-identity)
                   webview-bridge)
          (.reload webview-bridge))
        (when (get-in contacts [whisper-identity :bot-url])
          (dispatch [:load-commands! whisper-identity]))))))

(register-handler :debug-switch-node
  (u/side-effect!
    (fn [{:keys [current-account-id]} [_ {:keys [url]}]]
      (dispatch [:initialize-protocol current-account-id url]))))

